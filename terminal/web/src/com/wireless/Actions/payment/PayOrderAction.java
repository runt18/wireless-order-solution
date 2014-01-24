package com.wireless.Actions.payment;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PrintOption;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.sccon.ServerConnector;

public class PayOrderAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		String tempPay;
		try {
			// 解决后台中文传到前台乱码
			
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
			
			Order orderToPay = new Order();
			
			if(request.getParameter("eraseQuota") != null)
				orderToPay.setErasePrice(Integer.parseInt(request.getParameter("eraseQuota")));
			
//			orderToPay.destTbl.aliasID = Integer.parseInt(request.getParameter("tableID"));
			orderToPay.setId(Integer.parseInt(request.getParameter("orderID")));
			
			if(request.getParameter("payType") != null){
				orderToPay.setSettleType(Integer.parseInt(request.getParameter("payType")));				
			}else{
				orderToPay.setSettleType(Order.SettleType.NORMAL);
			}
			
			/**
			 * Get the member id if the pay type is "会员"
			 */
			if(orderToPay.getSettleType() == Order.SettleType.MEMBER){
				orderToPay.setMember(new Member(Integer.valueOf(request.getParameter("memberID"))));
			}
			
			if(request.getParameter("discountID") != null && !request.getParameter("discountID").equals("-1")){
				orderToPay.setDiscount(new Discount(Integer.parseInt(request.getParameter("discountID"))));				
			}else{
				orderToPay.setDiscount(new Discount());
			}
			
			if(request.getParameter("payManner") != null){
				orderToPay.setPaymentType(Integer.parseInt(request.getParameter("payManner")));
			}else{
				orderToPay.setPaymentType(Order.PayType.CASH);
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
			if(request.getParameter("customNum") != null){
				orderToPay.setCustomNum(Integer.valueOf(request.getParameter("customNum")));
			}
			
			/**
			 * Get the temporary pay flag.
			 * If pay order temporary, just print the receipt.
			 * Otherwise perform to pay order and print the receipt.
			 */
			byte payCate = Type.PAY_ORDER;
			tempPay = request.getParameter("tempPay");
			if(tempPay != null){
				if(Boolean.parseBoolean(tempPay)){
					payCate = Type.PAY_TEMP_ORDER;
				}else{
					payCate = Type.PAY_ORDER;
				}				
			}
			
			/**
			 * 
			 */
			PrintOption po = PrintOption.DO_PRINT;
			String isPrint = request.getParameter("isPrint");
			if(isPrint != null && !isPrint.trim().isEmpty() && !Boolean.valueOf(isPrint.trim()))
				po = PrintOption.DO_NOT_PRINT;
				
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqPayOrder(staff, orderToPay, payCate, po));
			
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
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				if(errCode.equals(ProtocolError.ORDER_NOT_EXIST)){					
					jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单信息不存在，请重新确认");
					
				}else if(errCode.equals(ProtocolError.ORDER_BE_REPEAT_PAID)){
					jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单已结帐，请重新确认");
					
				}else{
					jsonResp = jsonResp.replace("$(value)", errCode.getDesc());
				}
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderToPay.getId() + "号账单结帐不成功，请重新确认");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getDesc());
			
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
