package com.wireless.Actions.dishesOrder;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqPackage;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.JObject;
import com.wireless.util.Util;
import com.wireless.util.WebParams;


public class InsertOrderAction extends Action implements PinGen {
	
	private long _pin = 0;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			
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
			 * orderDate : last modified date to this order
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
			orderToInsert.getDestTbl().setAliasId(tableAlias);
		
			orderToInsert.setCategory(Short.parseShort(request.getParameter("category")));
			orderToInsert.setCustomNum(Integer.parseInt(request.getParameter("customNum")));
			if(request.getParameter("orderDate") != null && request.getParameter("orderDate").trim().length() > 0){
				orderToInsert.setOrderDate(Long.parseLong(request.getParameter("orderDate")));				
			}
			int type = Integer.parseInt(request.getParameter("type"));
			String orderType = null;
			
			if(type == 1){
				orderType = "下单";
			}else{
				orderType = "改单";
			}
			orderToInsert.setOrderFoods(Util.toFoodArray(request.getParameter("foods")));
			
			ReqPackage.setGen(this);
			ProtocolPackage resp = ServerConnector.instance().ask(
					new ReqInsertOrder(orderToInsert, (type == 1) ? Type.INSERT_ORDER : Type.UPDATE_ORDER));
			
			if(resp.header.type == Type.ACK){
				if(orderToInsert.isNormal()){
					jobject.initTip(true, (orderToInsert.getDestTbl().getAliasId() + "号餐台" + orderType + "成功."));
				}else if(orderToInsert.isTakeout()){
					jobject.initTip(true, ("外卖" + orderType + "成功."));
				}else if(orderToInsert.isMerged()){
					//jobject.initTip(true, (orderToInsert.destTbl.aliasID + "号台拼" + orderToInsert.destTbl2.aliasID + "号台" + orderType + "成功."));
				}else if(orderToInsert.isJoined()){
					jobject.initTip(true, ("并" + orderToInsert.getDestTbl().getAliasId() + "号" + orderType + "成功."));
				}
				
			}else if(resp.header.type == Type.NAK){
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
					jobject.initTip(false, ErrorCode.TERMINAL_NOT_ATTACHED, "没有获取到餐厅信息，请重新确认.");
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){					
					jobject.initTip(false, ErrorCode.TABLE_NOT_EXIST, (orderToInsert.getDestTbl().getAliasId() + "号餐台信息不存在，请重新确认."));
				}else if(resp.header.reserved == ErrorCode.TABLE_BUSY){
					jobject.initTip(false, ErrorCode.TABLE_BUSY, (orderToInsert.getDestTbl().getAliasId() + "号餐台正在就餐，可能已下单，请重新确认."));
				}else if(resp.header.reserved == ErrorCode.PRINT_FAIL){
					jobject.initTip(false, ErrorCode.PRINT_FAIL, (orderToInsert.getDestTbl().getAliasId() + "号餐台" + orderType + "成功，但未能成功打印，请立刻补打下单并与相关人员确认."));
				}else if(resp.header.reserved == ErrorCode.EXCEED_GIFT_QUOTA){
					jobject.initTip(false, ErrorCode.EXCEED_GIFT_QUOTA, "赠送菜品金额已超过赠送额度，请与餐厅负责人确认.");
				}else if(resp.header.reserved == ErrorCode.ORDER_EXPIRED){
					jobject.initTip(false, ErrorCode.ORDER_EXPIRED, "账单信息已更新,请重新刷新或返回.");
				}else if(resp.header.reserved == ErrorCode.TABLE_IDLE){
					jobject.initTip(false, ErrorCode.TABLE_IDLE, "该账单已结账或已删除.");
				}else{
					jobject.initTip(false, (orderToInsert.getDestTbl().getAliasId() + "号餐台" + orderType + "失败，请重新确认."));
				}
			}else{
				jobject.initTip(false, (orderToInsert.getDestTbl().getAliasId() + "号餐台" + orderType + "不成功，请重新确认."));
			}
		}catch(IOException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9997, "服务器请求不成功，请重新检查网络是否连通.");
			e.printStackTrace();
		}catch(NumberFormatException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9998, "菜品提交的数量不正确，请检查后重新提交.");
			e.printStackTrace();
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json);
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
