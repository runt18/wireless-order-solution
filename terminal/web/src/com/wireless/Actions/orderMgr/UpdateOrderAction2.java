package com.wireless.Actions.orderMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.PayOrder;
import com.wireless.db.UpdateOrder;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;
import com.wireless.util.Util;

public class UpdateOrderAction2 extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		int orderID = 0;
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & orderID=40 & category=1 & customNum=2 & payType=1 & 
			 * 		discountType=1 & payManner=1 & serviceRate=0 & 
			 * 		foods="{[1102,2,2,4]}"
			 * 
			 * pin : the pin the this terminal
			 * 
			 * orderID : the id to this order
			 * 
			 * category : "1" means "一般"
			 * 			  "2" means "外卖"
			 * 			  "3" means "并台"
			 * 			  "4" means "拼台"
			 * 
			 * customNum : the custom number to this order, ranges from 1 through 255
			 * 
			 * payType : "1" means pay in "一般", 
			 * 			 "2" means pay in "会员" 
			 * 
			 * discountType : "1" means to pay using discount 1
			 * 				  "2" means to pay using discount 2
			 * 				  "3" means to pay using discount 3
			 * 
			 * payManner : "1" means "现金"
			 * 			   "2" means "刷卡"
			 * 			   "3" means "会员卡"
			 * 			   "4" means "挂账"
			 * 			   "5" means "签单"
			 * 
			 * serviceRate : the service percent rate to this order,
			 * 				 this parameter is optional.
			 * 
			 * memberID : the id to member, 
			 * 			  this parameter is optional, only takes effect while the pay type is "会员" 
			 * 
			 * comment : the comment to this order
			 *           this parameter is optional,
			 *           No need to pass this parameter if no comment input. 
			 *           
			 * foods : the food string
			 */
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			
			Order orderToUpdate = new Order();
			//get the id to this order
			orderID = Integer.parseInt(request.getParameter("orderID"));
			orderToUpdate.id = orderID;
			//get the category to this order
			orderToUpdate.category = Short.parseShort(request.getParameter("category"));
			//get the custom number to this order
			orderToUpdate.custom_num = Integer.parseInt(request.getParameter("customNum"));
			//get the pay type to this order
			orderToUpdate.pay_type = Integer.parseInt(request.getParameter("payType"));	
			//get the discount type to this order
			orderToUpdate.discount_type = Integer.parseInt(request.getParameter("discountType"));
			//get the pay manner to this order
			orderToUpdate.pay_manner = Integer.parseInt(request.getParameter("payManner"));
			//get the service rate to this order
			orderToUpdate.service_rate = Byte.parseByte(request.getParameter("serviceRate"));
			/**
			 * Get the member id if the pay type is "会员"
			 */
			if(orderToUpdate.pay_type == Order.PAY_MEMBER){
				orderToUpdate.member_id = request.getParameter("memberID");
			}
			/**
			 * Get the first 20 characters of the comment
			 */
			String comment = request.getParameter("comment");
			if(comment != null){
				orderToUpdate.comment = comment.substring(0, comment.length() < 20 ? comment.length() : 20);
			}
			//get the food string to this order
			orderToUpdate.foods = Util.toFoodArray(request.getParameter("foods"));
			
			UpdateOrder.execByID(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF, orderToUpdate, true);
			PayOrder.execByID(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF, orderToUpdate);
			
			jsonResp = jsonResp.replace("$(result)", "true");	
			jsonResp = jsonResp.replace("$(value)", orderID + "号账单修改成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");		
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "登录人员不存在，请重新确认");	
				
			}else if(e.errCode == ErrorCode.TERMINAL_EXPIRED){
				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");	
				
			}else if(e.errCode == ErrorCode.ORDER_NOT_EXIST){
				jsonResp = jsonResp.replace("$(value)", orderID + "号账单不存在，请重新确认");	
				
			}else{
				jsonResp = jsonResp.replace("$(value)", orderID + "号账单修改失败，请重新确认");	
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "账单修改失败，请重新确认");
			
		}finally{
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
		
	}
}
