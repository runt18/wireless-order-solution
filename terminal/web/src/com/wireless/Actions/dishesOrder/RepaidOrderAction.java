package com.wireless.Actions.dishesOrder;

import java.io.PrintWriter;

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
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		int orderId = Integer.parseInt(request.getParameter("orderId"));
		String payType_money = request.getParameter("payType_money");
		String sType = request.getParameter("settleType");
		String pricePlanId = request.getParameter("pricePlanId");
		
		try {
			out = response.getWriter();
			
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.RE_PAYMENT);
			
			//结账类型
			Order.SettleType settleType = Order.SettleType.valueOf(Integer.parseInt(sType));
			
			//付款类型
			PayType payType4Pay = new PayType(Integer.parseInt(request.getParameter("payType")));
			
			Order.PayBuilder payBuilder;
			//get the pay manner to this order
			if(settleType == Order.SettleType.MEMBER){
//				Member member = MemberDao.getById(staff, Integer.valueOf(request.getParameter("memberID")));
//				
//				if(member.getMemberType().getAttribute() == MemberType.Attribute.CHARGE){
//					payBuilder = Order.PayBuilder.build4ChargeMember(orderId, member, Integer.parseInt(request.getParameter("discountID")), false);
//				}else{
//					payBuilder = Order.PayBuilder.build4PointMember(orderId, member,payType4Pay, Integer.parseInt(request.getParameter("discountID")), false);
//				}
				
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
			if(comment != null){
				payBuilder.setComment(comment.substring(0, comment.length() < 20 ? comment.length() : 20));
			}
			
			if(PayType.MIXED.getId() == Integer.parseInt(request.getParameter("payType"))){
				String payTypes[] = payType_money.split("&");
				for (String p : payTypes) {
					String payType[] = p.split(",");
					payBuilder.addPayment(new PayType(Integer.parseInt(payType[0])), Float.parseFloat(payType[1]));
				}
			}
			
			Order.RepaidBuilder repaidBuilder = new Order.RepaidBuilder(JObject.parse(Order.UpdateBuilder.JSON_CREATOR, 0, request.getParameter("commitOrderData")), payBuilder);

			//Set the discount.
			String discount = request.getParameter("discountID");			
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
			if(request.getParameter("servicePlan") != null && !request.getParameter("servicePlan").isEmpty()){
				repaidBuilder.setServiceBuilder(new Order.ServiceBuilder(orderId, Integer.parseInt(request.getParameter("servicePlan"))));
			}
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqRepayOrder(staff, repaidBuilder, PrintOption.DO_PRINT));
			
			
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");	
				jsonResp = jsonResp.replace("$(value)", orderId + "号账单修改成功");
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");		
				jsonResp = jsonResp.replace("$(value)", new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());	
			}
			
		}finally{
			out.write(jsonResp);
		}
		
		return null;
		
	}
}
