package com.wireless.Actions.dishesOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqRepayOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class RepaidOrderAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		final int orderId = Integer.parseInt(request.getParameter("orderId"));
		final String payTypeMoney = request.getParameter("payType_money");
		final String sType = request.getParameter("settleType");
		final String pricePlanId = request.getParameter("pricePlanId");
		final String printers = request.getParameter("orientedPrinter");
		
		try {
			
			final String pin = (String)request.getAttribute("pin");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.RE_PAYMENT);
			
			//结账类型
			final Order.SettleType settleType = Order.SettleType.valueOf(Integer.parseInt(sType));
			
			//付款类型
			final PayType payType4Pay = new PayType(Integer.parseInt(request.getParameter("payType")));
			
			final Order.PayBuilder payBuilder;
			//get the pay manner to this order
			if(settleType == Order.SettleType.MEMBER){
				payBuilder = Order.PayBuilder.build4Member(orderId, payType4Pay, false);
			}else{
				payBuilder = Order.PayBuilder.build4Normal(orderId, payType4Pay);
			}
			
			//get the custom number to this order
			payBuilder.setCustomNum(Integer.parseInt(request.getParameter("customNum")));
			
			//get the erasePrice rate to this order
			payBuilder.setErasePrice(Integer.valueOf(request.getParameter("erasePrice")));
			
			//Get the first 20 characters of the comment
			String comment = request.getParameter("comment");
			if(comment != null && !comment.isEmpty()){
				payBuilder.setComment(comment.substring(0, comment.length() < 20 ? comment.length() : 20));
			}
			
			if(PayType.MIXED.getId() == Integer.parseInt(request.getParameter("payType"))){
				String payTypes[] = payTypeMoney.split("&");
				for (String p : payTypes) {
					String payType[] = p.split(",");
					payBuilder.addPayment(new PayType(Integer.parseInt(payType[0])), Float.parseFloat(payType[1]));
				}
			}
			
			Order.RepaidBuilder repaidBuilder = new Order.RepaidBuilder(JObject.parse(Order.UpdateBuilder.JSON_CREATOR, 0, request.getParameter("commitOrderData")), payBuilder);

			//Set the discount.
			final String discount = request.getParameter("discountID");			
			if(settleType == Order.SettleType.MEMBER){
				int pricePlan = 0;
				if(pricePlanId != null && !pricePlanId.isEmpty()){
					pricePlan = Integer.parseInt(pricePlanId);
				}
				
				if(discount != null && !discount.isEmpty()){
					repaidBuilder.setDiscountBuilder(Order.DiscountBuilder.build4Member(orderId, new Member(Integer.valueOf(request.getParameter("memberID"))), 
																						Integer.parseInt(discount), pricePlan));
				}
			}else{
				if(discount != null && !discount.isEmpty()){
					repaidBuilder.setDiscountBuilder(Order.DiscountBuilder.build4Normal(orderId, Integer.parseInt(discount)));
				}
			}
			
			//Set the service plan.
			final String servicePlan = request.getParameter("servicePlan");
			if(servicePlan != null && !servicePlan.isEmpty()){
				repaidBuilder.setServiceBuilder(new Order.ServiceBuilder(orderId, Integer.parseInt(servicePlan)));
			}
			
			//Set the coupons
			final String coupons = request.getParameter("coupons");
			if(coupons != null && !coupons.equals("-1")){
				final Order.CouponBuilder builder = new Order.CouponBuilder(orderId);
				if(!coupons.isEmpty()){
					for(String couponId : coupons.split(",")){
						builder.addCoupon(Integer.parseInt(couponId));
					}
				}
				repaidBuilder.setCouponBuilder(builder);
			}
			
			//Set the oriented printers.
			if(printers != null && !printers.isEmpty()){
				for(String printerId : printers.split(",")){
					repaidBuilder.addPrinter(Integer.parseInt(printerId));
				}
			}
			
			final ProtocolPackage resp = ServerConnector.instance().ask(new ReqRepayOrder(staff, repaidBuilder, PrintOption.DO_PRINT));
			
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");	
				jsonResp = jsonResp.replace("$(value)", orderId + "号账单修改成功");
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");		
				jsonResp = jsonResp.replace("$(value)", new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());	
			}
			
		}finally{
			response.getWriter().write(jsonResp);
		}
		
		return null;
		
	}
}
