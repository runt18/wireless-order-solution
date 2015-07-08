package com.wireless.Actions.weixin.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.auth.AuthParam;

public class WxAuthLoginAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid=$(app_id)&pre_auth_code=$(pre_auth_code)&redirect_uri=$(redirect_uri)";
		path = path.replace("$(app_id)", AuthParam.APP_ID);
		path = path.replace("$(pre_auth_code)", AuthParam.PRE_AUTH_CODE.getCode());
//		path = path.replace("$(redirect_uri)", "http://" + request.getLocalAddr() + "/wx-term/WxAuth.do?rid=" + request.getParameter("rid"));
		path = path.replace("$(redirect_uri)", "http://" + request.getLocalAddr() + "/wx-term/weixin/order/authResponse.html?rid=" + request.getParameter("rid"));
		response.getWriter().write(path);
		return null;
	}
}
