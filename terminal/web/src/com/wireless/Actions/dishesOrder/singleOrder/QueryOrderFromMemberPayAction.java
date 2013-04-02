package com.wireless.Actions.dishesOrder.singleOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.protocol.PDiscount;
import com.wireless.util.DataType;
import com.wireless.util.JObject;
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
			String restaurantID = request.getParameter("restaurantID");
			String memberCard = request.getParameter("memberCard");
			String orderID = request.getParameter("orderID");
			
			Member m = MemberDao.getMemberByCard(Integer.valueOf(restaurantID), memberCard);
			com.wireless.protocol.Order oo = QueryOrderDao.execByID(Integer.valueOf(orderID), DataType.TODAY.getValue());
			
			oo.setDiscount(new PDiscount(Integer.valueOf(m.getMemberType().getDiscount().getId())));
			com.wireless.protocol.Order no = PayOrder.calcByID(VerifyPin.exec(Long.valueOf(pin), com.wireless.protocol.Terminal.MODEL_STAFF), oo);
			
			jobject.getOther().put("member", m);
			jobject.getOther().put("newOrder", new Order(no));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}

}
