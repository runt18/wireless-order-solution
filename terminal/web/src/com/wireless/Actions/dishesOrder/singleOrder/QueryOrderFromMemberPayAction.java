package com.wireless.Actions.dishesOrder.singleOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.pojo.client.Member;
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
			System.out.println("clientName: "+m.getClient().getName());
			
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}

}
