package com.wireless.Actions.payment;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPackage;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.protocol.Order;
import com.wireless.protocol.PDiscount;
import com.wireless.protocol.PMember;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.NumericUtil;

public class PayOrderAction extends Action implements PinGen{
	
	private long _pin = 0;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		String tempPay;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
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
			
			String pin = request.getParameter("pin");
			
			_pin = Long.parseLong(pin);
			
			Order orderToPay = new Order();
			
			if(request.getParameter("eraseQuota") != null)
				orderToPay.setErasePrice(Integer.parseInt(request.getParameter("eraseQuota")));
			
//			orderToPay.destTbl.aliasID = Integer.parseInt(request.getParameter("tableID"));
			orderToPay.setId(Integer.parseInt(request.getParameter("orderID")));
			
			if(request.getParameter("payType") != null){
				orderToPay.setSettleType(Integer.parseInt(request.getParameter("payType")));				
			}else{
				orderToPay.setSettleType(Order.SETTLE_BY_NORMAL);
			}
			
			/**
			 * Get the member id if the pay type is "会员"
			 */
			if(orderToPay.getSettleType() == Order.SETTLE_BY_MEMBER){
				orderToPay.setMember(new PMember(Integer.valueOf(request.getParameter("memberID"))));
			}
			
			if(request.getParameter("discountID") != null && !request.getParameter("discountID").equals("-1")){
				orderToPay.setDiscount(new PDiscount(Integer.parseInt(request.getParameter("discountID"))));				
			}else{
				orderToPay.setDiscount(new PDiscount());
			}
			
			if(request.getParameter("payManner") != null){
				orderToPay.setPaymentType(Integer.parseInt(request.getParameter("payManner")));
			}else{
				orderToPay.setPaymentType(Order.PAYMENT_CASH);
			}
			
			if(request.getParameter("serviceRate") != null){
				orderToPay.setServiceRate(NumericUtil.int2Float(Integer.parseInt(request.getParameter("serviceRate"))));
			}else{
				orderToPay.setServiceRate(new Float(0));
			}
			
			/**
			 * Get the cash income if the pay manner is "现金"
			 */
			if(orderToPay.isPayByCash()){
				orderToPay.setReceivedCash(Float.parseFloat(request.getParameter("cashIncome")));
			}
			
			String comment = request.getParameter("comment");
			/**
			 * Get the first 20 characters of the comment
			 */
			if(comment != null){
				orderToPay.setComment(comment.substring(0, comment.length() < 20 ? comment.length() : 20));
			}			
			/**
			 * 
			 */
			if(request.getParameter("pricePlanID") != null){
				orderToPay.setPricePlan(new PricePlan(Integer.valueOf(request.getParameter("pricePlanID"))));
			}
			/**
			 * 
			 */
			if(request.getParameter("customNum") != null){
				orderToPay.setCustomNum(Integer.valueOf(request.getParameter("customNum")));
			}
			
			ReqPackage.setGen(this);
			
			/**
			 * Get the temporary pay flag.
			 * If pay order temporary, just print the receipt.
			 * Otherwise perform to pay order and print the receipt.
			 */
			byte payCate = ReqPayOrder.PAY_CATE_NORMAL;;
			tempPay = request.getParameter("tempPay");
			if(tempPay != null){
				if(Boolean.parseBoolean(tempPay)){
					payCate = ReqPayOrder.PAY_CATE_TEMP;
				}else{
					payCate = ReqPayOrder.PAY_CATE_NORMAL;
				}				
			}
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqPayOrder(orderToPay, payCate));
			
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				if(tempPay != null){
					if(Boolean.parseBoolean(tempPay)){
						jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单暂结成功");
					}else{
						jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单结帐成功");
					}
				}else{
					jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单结帐成功");
				}
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
					jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){					
					jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单信息不存在，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.ORDER_BE_REPEAT_PAID){
					jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单已结帐，请重新确认");
					
				}else{
					jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单结帐失败，请重新确认");
				}
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单结帐不成功，请重新确认");
			}
			
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
