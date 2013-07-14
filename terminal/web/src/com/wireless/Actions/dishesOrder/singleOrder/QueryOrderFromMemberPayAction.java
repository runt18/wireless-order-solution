package com.wireless.Actions.dishesOrder.singleOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.protocol.Terminal;
import com.wireless.util.WebParams;

public class QueryOrderFromMemberPayAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String pin = request.getParameter("pin");
			String orderID = request.getParameter("orderID");
			String st = request.getParameter("st");
			String sv = request.getParameter("sv");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), com.wireless.protocol.Terminal.MODEL_STAFF);
			Member m = null;
			if(st.trim().equals("mobile")){
				m = MemberDao.getMemberByMobile(term, sv);
			}else if(st.trim().equals("card")){
				m = MemberDao.getMemberByCard(term, sv);				
			}
			
			com.wireless.pojo.dishesOrder.Order no = new com.wireless.pojo.dishesOrder.Order();
			no.setId(Integer.valueOf(orderID));
			no.setDiscount(new Discount(Integer.valueOf(m.getMemberType().getDiscount().getId())));
			no = PayOrder.calcByID(term, no);
			
			m.getMemberType().setDiscount(no.getDiscount());
			jobject.getOther().put("member", m);
			jobject.getOther().put("newOrder", no);
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
