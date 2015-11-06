package com.wireless.Actions.payment;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class PayOrderAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String jsonResp = "{\"success\":$(result), \"data\":\"$(value)\"}";
		try {
			
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			final int orderId = Integer.parseInt(request.getParameter("orderID"));

			final String settleParam = request.getParameter("payType");
			final String payMannerParam = request.getParameter("payManner");
			final String cashIncome = request.getParameter("cashIncome");
			final String eraseQuota = request.getParameter("eraseQuota");
			final String comment = request.getParameter("comment");
			final String customNum = request.getParameter("customNum");
			final String tempPay = request.getParameter("tempPay");
			final String isPrint = request.getParameter("isPrint");
			final String orientedPrinter = request.getParameter("orientedPrinter");
			
			final Order.SettleType settleType;
			if(settleParam != null && !settleParam.isEmpty()){
				settleType = Order.SettleType.valueOf(Integer.parseInt(settleParam));
			}else{
				settleType = Order.SettleType.NORMAL;
			}

			final PayType payType;
			if(payMannerParam != null && !payMannerParam.isEmpty()){
				payType = new PayType(Integer.parseInt(payMannerParam));
			}else{
				payType = PayType.CASH;
			}
			
			final Order.PayBuilder payBuilder;
			if(settleType == Order.SettleType.MEMBER){
				payBuilder = Order.PayBuilder.build4Member(orderId, payType, Boolean.parseBoolean(request.getParameter("sendSms")));
			}else{
				payBuilder = Order.PayBuilder.build4Normal(orderId, payType);
			}
			
			//Get the cash income if the pay manner is "现金"
			if(payType.equals(PayType.CASH) && cashIncome != null && !cashIncome.isEmpty()){
				payBuilder.setReceivedCash(Float.parseFloat(cashIncome));
				
			}else if(payType.equals(PayType.MIXED)){
				final String payTypeCashs = request.getParameter("payTypeCash");
				
				for (String pt : payTypeCashs.split("&")) {
					String payTypeCash[] = pt.split(",");
					payBuilder.addPayment(new PayType(Integer.parseInt(payTypeCash[0])), Float.parseFloat(payTypeCash[1]));
				}
			}
			
			if(eraseQuota != null && !eraseQuota.isEmpty()){
				payBuilder.setErasePrice(Integer.parseInt(eraseQuota));
			}
			
			//Get the first 20 characters of the comment
			if(comment != null && !comment.isEmpty()){
				payBuilder.setComment(comment.substring(0, comment.length() < 20 ? comment.length() : 20));
			}	

			//Get the custom number.
			if(customNum != null && !customNum.isEmpty()){
				payBuilder.setCustomNum(Integer.valueOf(customNum));
			}
			
			/**
			 * Get the temporary pay flag.
			 * If pay order temporary, just print the receipt.
			 * Otherwise perform to pay order and print the receipt.
			 */
			if(tempPay != null && !tempPay.isEmpty()){
				payBuilder.setTemp(Boolean.parseBoolean(tempPay));
			}
			
			/**
			 * 是否打印
			 */
			if(isPrint != null && !isPrint.trim().isEmpty()){
				if(Boolean.parseBoolean(isPrint)){
					payBuilder.setPrintOption(PrintOption.DO_PRINT);
				}else{
					payBuilder.setPrintOption(PrintOption.DO_NOT_PRINT);
				}
			}
			
			if(orientedPrinter != null && !orientedPrinter.isEmpty()){
				for(String printerId : orientedPrinter.split(",")){
					payBuilder.addPrinter(Integer.parseInt(printerId));
				}
			}
			
			if(payType.equals(PayType.WX)){
				//打印微信支付单
				ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildWxReceipt(staff, payBuilder).build());
				if(resp.header.type == Type.ACK){
					jsonResp = jsonResp.replace("$(result)", "true");
					jsonResp = jsonResp.replace("$(value)", "微信支付成功");
				}else{
					throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
				}
			}else{
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
			
		}catch(Exception e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getMessage());
			
		}finally{
			response.getWriter().print(jsonResp);
		}

		return null;
	}
	
}
