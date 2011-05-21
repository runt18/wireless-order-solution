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
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.ReqOrderPackage;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;


public class InsertOrderAction extends Action implements PinGen {
	
	private int _pin = 0;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			

			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			_pin = Integer.parseInt(pin, 16);
			
			Order orderToInsert = new Order();
			orderToInsert.table_id = Short.parseShort(request.getParameter("tableID"));
			orderToInsert.custom_num = Integer.parseInt(request.getParameter("customNum"));
			orderToInsert.originalTableID = Short.parseShort(request.getParameter("tableID"));
			orderToInsert.foods = toFoodArray(request.getParameter("foods"));
			
			ReqOrderPackage.setGen(this);
			byte printType = Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(orderToInsert, 
																					 Type.INSERT_ORDER,
																					 printType));
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				jsonResp = jsonResp.replace("$(value)", orderToInsert.table_id + "号餐台下单成功");
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
					jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){					
					jsonResp = jsonResp.replace("$(value)", orderToInsert.table_id + "号餐台信息不存在，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.TABLE_BUSY){
					jsonResp = jsonResp.replace("$(value)", orderToInsert.table_id + "号餐台正在就餐，可能已下单，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.PRINT_FAIL){
					jsonResp = jsonResp.replace("$(value)", orderToInsert.table_id + "号餐台下单成功，但未能成功打印，请立刻补打下单并与相关人员确认");
					
				}else{
					jsonResp = jsonResp.replace("$(value)", orderToInsert.table_id + "号餐台下单失败，请重新确认");
				}
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderToInsert.table_id + "餐台下单不成功，请重新确认");
			}
			
		}catch(IOException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "服务器请求不成功，请重新检查网络是否连通");
			
		}catch(NumberFormatException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "菜品提交的数量不正确，请检查后重新提交");
			
		}finally{
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}

	/**
	 * Convert the foods string submitted by terminal into the array of class food.
	 * @param submitFoods the submitted string looks like below.<br>
	 * 			{[菜品1编号,菜品1数量,口味1编号,厨房1编号]，[菜品2编号,菜品2数量,口味2编号,厨房2编号]，...}
	 * @return the class food array
	 */
	private Food[] toFoodArray(String submitFoods) throws NumberFormatException{
		//remove the "{}"
		submitFoods = submitFoods.substring(1, submitFoods.length() - 1);
		//extract each food item string
		String[] foodItems = submitFoods.split("，");
		Food[] foods = new Food[foodItems.length];
		for(int i = 0; i < foodItems.length; i++){
			//remove the "[]"
			String foodItem = foodItems[i].substring(1, foodItems[i].length() - 1);
			foods[i] = new Food();
			//extract each food detail information string			
			String[] values = foodItem.split(",");		
			//extract the food alias id
			foods[i].alias_id = Integer.parseInt(values[0]);
			//extract the amount to order food
			foods[i].setCount(Float.parseFloat(values[1]));
			//extract the taste alias id
			foods[i].taste.alias_id = Short.parseShort(values[2]);
			//extract the kitchen number
			foods[i].kitchen = Short.parseShort(values[3]);
		}
		return foods;
	}
	
	@Override
	public int getDeviceId() {
		// TODO Auto-generated method stub
		return _pin;
	}

	@Override
	public short getDeviceType() {
		// TODO Auto-generated method stub
		return Terminal.MODEL_STAFF;
	}
}
