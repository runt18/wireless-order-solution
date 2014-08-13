package com.wireless.Actions.payment;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class PayOrderAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			
			out = response.getWriter();			

			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & tempPay=false & tableID=201 & payType=1 & discountType=1 & 
			 * 		payManner=1 & cashIncome=120 giftPrice=0 & serviceRate=5 & memberID=13693834750
			 * 
			 * pin : the pin the this terminal
			 * 
			 * tempPay : indicates whether to pay order temporary
			 * 
			 * tableID : the table id to be paid order
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
			 * 			   "4" means "签单"
			 * 			   "5" means "挂账"
			 * 
			 * cashIncome : the cash that client pay for this order,
			 * 				this parameter is optional, only takes effect while the pay manner is "现金"
			 * 
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
			 */
			
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			Order.PayBuilder payBuilder;
			
			int orderId = Integer.parseInt(request.getParameter("orderID"));
			
			Order.SettleType settleType;
			if(request.getParameter("payType") != null){
				settleType = Order.SettleType.valueOf(Integer.parseInt(request.getParameter("payType")));
			}else{
				settleType = Order.SettleType.NORMAL;
			}
			
			Order.PayType payType;
			if(request.getParameter("payManner") != null){
				payType = Order.PayType.valueOf(Integer.parseInt(request.getParameter("payManner")));
			}else{
				payType = Order.PayType.CASH;
			}
			
			Member member = null;
			if(settleType == Order.SettleType.MEMBER){
				boolean sendSMS = false;
				//Send SMS if paid by charge member.
				for(Cookie cookie : request.getCookies()){
				    if(cookie.getName().equals((request.getServerName() + "_consumeSms"))){
				    	if(cookie.getValue().equals("true")){
				    		sendSMS = true;
				    		break;
				    	}
				    }
				}
				member = MemberDao.getById(staff, Integer.valueOf(request.getParameter("memberID")));
				payBuilder = Order.PayBuilder.build4Member(orderId, member, payType, sendSMS);
			}else{
				payBuilder = Order.PayBuilder.build(orderId, payType);
			}
			
			//Get the cash income if the pay manner is "现金"
			if(payType == Order.PayType.CASH){
				payBuilder.setReceivedCash(Float.parseFloat(request.getParameter("cashIncome")));
			}
			
			if(request.getParameter("discountID") != null && !request.getParameter("discountID").isEmpty() && !request.getParameter("discountID").equals("-1")){
				payBuilder.setDiscountId(Integer.parseInt(request.getParameter("discountID")));
			}
			
			if(request.getParameter("couponID") != null && !request.getParameter("couponID").isEmpty() && !request.getParameter("couponID").equals("-1")){
				payBuilder.setCouponId(Integer.parseInt(request.getParameter("couponID")));
			}
			
			if(request.getParameter("servicePlan") != null && !request.getParameter("servicePlan").isEmpty()){
				payBuilder.setServicePlan(Integer.parseInt(request.getParameter("servicePlan")));
			}
			
			if(request.getParameter("eraseQuota") != null){
				payBuilder.setErasePrice(Integer.parseInt(request.getParameter("eraseQuota")));
			}
			
			//Get the first 20 characters of the comment
			String comment = request.getParameter("comment");
			if(comment != null && !comment.isEmpty()){
				payBuilder.setComment(comment.substring(0, comment.length() < 20 ? comment.length() : 20));
			}	

			//Get the custom number.
			if(request.getParameter("customNum") != null){
				payBuilder.setCustomNum(Integer.valueOf(request.getParameter("customNum")));
			}
			
			/**
			 * Get the temporary pay flag.
			 * If pay order temporary, just print the receipt.
			 * Otherwise perform to pay order and print the receipt.
			 */
			String tempPay = request.getParameter("tempPay");
			if(tempPay != null){
				payBuilder.setTemp(Boolean.parseBoolean(tempPay));
			}
			
			/**
			 * 
			 */
			String isPrint = request.getParameter("isPrint");
			if(isPrint != null && !isPrint.trim().isEmpty() && !Boolean.valueOf(isPrint.trim())){
				payBuilder.setPrintOption(PrintOption.DO_NOT_PRINT);
			}
				
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqPayOrder(staff, payBuilder));
			
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				if(payBuilder.isTemp()){
					jsonResp = jsonResp.replace("$(value)", payBuilder.getOrderId() + "号账单暂结成功");
				}else{
					jsonResp = jsonResp.replace("$(value)", payBuilder.getOrderId() + "号账单结帐成功");
		    	}
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", payBuilder.getOrderId() + "号账单结帐不成功，请重新确认");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getMessage());
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "服务器请求不成功，请重新检查网络是否连通");
			
		}catch(NumberFormatException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "实收金额格式不正确，请检查后重新提交");
			
		}finally{
			//just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
			out.flush();
			out.close();
		}

		return null;
	}
	
}
