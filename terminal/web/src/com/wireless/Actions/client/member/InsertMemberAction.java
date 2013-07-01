package com.wireless.Actions.client.member;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.system.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertMemberAction extends Action {

	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			MemberDao.insert(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), makeInsertBuilder(request.getParameter("params")));
			jobject.initTip(true, "操作成功, 新会员资料添加成功.");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Member.InsertBuilder makeInsertBuilder(String params){
		//FIXME
		Member m = new Member();
		m.fromJsonMap(JSONObject.fromObject(params));
		Member.InsertBuilder builder = new Member.InsertBuilder(m.getRestaurantId(), m.getName(), m.getMobile(), m.getMemberType().getTypeID())
												 .setBirthday(m.getBirthday())
												 .setCompany(m.getCompany())
												 .setContactAddr(m.getContactAddress())
												 .setIdCard(m.getIdCard())
												 .setMemberCard(m.getMemberCard())
												 .setSex(m.getSex())
												 .setTaboo(m.getTaboo())
												 .setTastePref(m.getTastePref())
												 .setTele(m.getTele());
		return builder;
	}
}
