package com.wireless.Actions.weixin.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.session.WxAuthSession;

import com.wireless.Actions.weixin.WxHandleMessage;
import com.wireless.Actions.weixin.WxAccessHandleMessage;

public class WxRecEventAction extends Action {
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String timestamp = request.getParameter("timestamp");
		final String nonce = request.getParameter("nonce");
		final String msgSignature = request.getParameter("msg_signature");
		
		WxAuthSession session = new WxAuthSession(timestamp, nonce, msgSignature, AuthParam.TOKEN, AuthParam.ENCRYPT_AES_KEY, AuthParam.APP_ID);
		try{
			session.addOnHandleMessageListener(new WxAccessHandleMessage(session));
			session.addOnHandleMessageListener(new WxHandleMessage(session, "http://" + request.getLocalAddr() + "/wx-term"));
			session.process(request.getInputStream(), response.getOutputStream());
		}finally{
			session.close();
		}
		
		return null;
	}

}
