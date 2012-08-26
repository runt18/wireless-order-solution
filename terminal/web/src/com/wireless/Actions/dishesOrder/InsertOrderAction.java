package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.Util;


public class InsertOrderAction extends Action implements PinGen {
	
	private long _pin = 0;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			

			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & tableID=201 & tableID_2=0 & 
			 * 		category=1 & customNum=2 & type=1 & 
			 * 		foods={[1102,2,2,4,1]，[1103,2,2,4,0.9]，...}
			 * 
			 * pin : the pin the this terminal
			 * 
			 * tableID : the table id to insert order
			 * 
			 * tableID_2 : the 2nd table id, 
			 * 			   this parameter is optional and only takes effect in the case of "拼台"
			 * 
			 * category : "1" means "一般"
			 * 			  "2" means "外卖"
			 * 			  "3" means "并台"
			 * 			  "4" means "拼台"
			 * 
			 * customNum : the custom number to this order, ranges from 1 through 255
			 * 
			 * type : "1" means insert order
			 * 		  "2" means update order
			 * 
			 * originalTableID : the original table id, this parameter is optional.
			 * 					 if the type is insert order, NOT need this parameter.
			 * 			     	 if the type is update order, need this parameter to indicate the original table.
			 * 			  		 if you want to transfer to another table, using this parameter like below.
			 * 					 e.g. transfer table from 100 to 101, "tableID=101 & originalTableID=100"
			 * 
			 * foods : the food string whose format looks like below.
			 *			{[是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号,是否临时口味,临时口味,临时口味价钱,临时口味编号,叫起状态]，
	         *	 		 [是否临时菜(false),菜品2编号,菜品2数量,口味2编号,厨房2编号,菜品2折扣,2nd口味1编号,3rd口味1编号,是否临时口味,临时口味,临时口味价钱,临时口味编号,叫起状态]，...
	         * 	         [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价,叫起状态]，
	         * 			 [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价,叫起状态]...}
			 */
			String pin = request.getParameter("pin");
			
			_pin = Long.parseLong(pin);
			
			Order orderToInsert = new Order();
			int tableAlias = request.getParameter("tableID") != null ? Integer.parseInt(request.getParameter("tableID")) : 0;
			orderToInsert.destTbl.aliasID = tableAlias;
			String table2ID = request.getParameter("tableID_2");
			if(table2ID != null){
				orderToInsert.destTbl2.aliasID = Integer.parseInt(table2ID);
			}			
			orderToInsert.category = Short.parseShort(request.getParameter("category"));
			orderToInsert.custom_num = Integer.parseInt(request.getParameter("customNum"));
			int type = Integer.parseInt(request.getParameter("type"));
			String orderType = null;
			short printType = Reserved.DEFAULT_CONF;
			if(type == 1){
				orderType = "下单";
				orderToInsert.srcTbl.aliasID = tableAlias;				
				printType = Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
			}else{
				orderType = "改单";
				String oriTableID = request.getParameter("originalTableID");
				if(oriTableID == null){
					orderToInsert.srcTbl.aliasID = orderToInsert.destTbl.aliasID;
				}else{
					orderToInsert.srcTbl.aliasID = Integer.parseInt(oriTableID);
				}
				printType |= Reserved.PRINT_EXTRA_FOOD_2 | 
							 Reserved.PRINT_CANCELLED_FOOD_2 | 
							 Reserved.PRINT_TRANSFER_TABLE_2 |
						     Reserved.PRINT_ALL_CANCELLED_FOOD_2 | 
						     Reserved.PRINT_ALL_EXTRA_FOOD_2;
			}
			orderToInsert.foods = Util.toFoodArray(request.getParameter("foods"));
			
			ReqPackage.setGen(this);
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(orderToInsert, 
																					 (type == 1) ? Type.INSERT_ORDER : Type.UPDATE_ORDER, 
																					 printType));
			
		
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				if(orderToInsert.category == Order.CATE_NORMAL){
					if(orderToInsert.destTbl.aliasID == orderToInsert.srcTbl.aliasID){
						jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号餐台" + orderType + "成功");
					}else{
						jsonResp = jsonResp.replace("$(value)", orderToInsert.srcTbl.aliasID + "号台转至" + orderToInsert.destTbl.aliasID + "号台，改单成功。");
					}					
					
				}else if(orderToInsert.category == Order.CATE_TAKE_OUT){
					jsonResp = jsonResp.replace("$(value)", "外卖" + orderType + "成功");
					
				}else if(orderToInsert.category == Order.CATE_MERGER_TABLE){
					jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号台拼" + orderToInsert.destTbl2.aliasID + "号台" + orderType + "成功");
					
				}else if(orderToInsert.category == Order.CATE_JOIN_TABLE){
					jsonResp = jsonResp.replace("$(value)", "并" + orderToInsert.destTbl.aliasID + "号" + orderType + "成功");
				}
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
					jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){					
					jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号餐台信息不存在，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.TABLE_BUSY){
					jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号餐台正在就餐，可能已下单，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.PRINT_FAIL){
					jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号餐台" + orderType + "成功，但未能成功打印，请立刻补打下单并与相关人员确认");
					
				}else if(resp.header.reserved == ErrorCode.EXCEED_GIFT_QUOTA){
					jsonResp = jsonResp.replace("$(value)", "赠送菜品金额已超过赠送额度，请与餐厅负责人确认");
					
				}else{
					jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号餐台" + orderType + "失败，请重新确认");
				}
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderToInsert.destTbl.aliasID + "号餐台" + orderType + "不成功，请重新确认");
			}
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "服务器请求不成功，请重新检查网络是否连通");
			
		}catch(NumberFormatException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "菜品提交的数量不正确，请检查后重新提交");
			
		}finally{
			//just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}
	
	@Override
	public long getDeviceId() {
		return _pin;
	}

	@Override
	public short getDeviceType() {
		return Terminal.MODEL_STAFF;
	}
}
