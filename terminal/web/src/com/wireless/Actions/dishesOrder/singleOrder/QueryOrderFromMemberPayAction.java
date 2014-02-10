package com.wireless.Actions.dishesOrder.singleOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JObject;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class QueryOrderFromMemberPayAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			String orderID = request.getParameter("orderID");
			String discountId = request.getParameter("discountId");
			String st = request.getParameter("st");
			String sv = request.getParameter("sv");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Member m;
			if(st != null && st.trim().equals("mobile")){
				m = MemberDao.getMemberByMobile(staff, sv);
			}else if(st != null && st.trim().equals("card")){
				m = MemberDao.getMemberByCard(staff, sv);				
			}else{
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}
			
			Order.PayBuilder payBuilder = Order.PayBuilder.build4Member(Integer.valueOf(orderID), m, Order.PayType.CASH);
			if(discountId != null && !discountId.trim().isEmpty()){
				payBuilder.setDiscountId(Integer.valueOf(discountId));
			}else{
				payBuilder.setDiscountId(m.getMemberType().getDefaultDiscount().getId());
			}
			Order order = PayOrder.calc(staff, payBuilder);
			
			jobject.getOther().put("member", m);
			jobject.getOther().put("newOrder", order);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
