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
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class RepaidOrderAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		int orderId = Integer.parseInt(request.getParameter("orderId"));
		String payType_money = request.getParameter("payType_money");
		try {
			// 解决后台中文传到前台乱码
			
			out = response.getWriter();
			
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.RE_PAYMENT);
			
			//get the pay manner to this order
			Order.PayBuilder payBuilder = Order.PayBuilder.build(orderId, new PayType(Integer.parseInt(request.getParameter("payType"))));

			//get the custom number to this order
			payBuilder.setCustomNum(Integer.parseInt(request.getParameter("customNum")));
			
			//get the discount type to this order
			payBuilder.setDiscountId(Integer.parseInt(request.getParameter("discountID")));
			
			//get the service rate to this order
			payBuilder.setServicePlan(Integer.parseInt(request.getParameter("servicePlan")));

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
			
			ProtocolPackage resp = ServerConnector.instance().ask(
					new ReqRepayOrder(staff, 
							new Order.RepaidBuilder(JObject.parse(Order.UpdateBuilder.JSON_CREATOR, 0, request.getParameter("commitOrderData")), payBuilder), 
									PrintOption.DO_PRINT));
			
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
