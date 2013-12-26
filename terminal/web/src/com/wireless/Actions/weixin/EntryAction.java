package com.wireless.Actions.weixin;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.DefaultSession;

import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;

public class EntryAction extends Action{
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String method = request.getMethod();
		if(method.toLowerCase().equals("get")){
			verify(request, response);
		}else{
			reply(request, response);
		}
		return null;
	}
	
	/**
	 * 回复信息
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void reply(HttpServletRequest request, HttpServletResponse response) throws Exception {
		InputStream is = request.getInputStream();
		OutputStream os = response.getOutputStream();
		DefaultSession session = DefaultSession.newInstance();
		try{
			String account = request.getParameter("account");
			session.addOnHandleMessageListener(new WeiXinRestaurantHandleMessageAdapter(session, account));
		}finally{
			session.process(is, os);
			session.close();
			session = null;
			if(is != null){
				is.close();
				is = null;
			}
			if(os != null){
				os.flush();
				os.close();
				os = null;
			}
		}
	}
	
	/**
	 * 验证
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void verify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Writer out = response.getWriter();
		String account = request.getParameter("account");
		String result = "";
		try{
			if(account != null && !account.trim().isEmpty()){
				String signature = request.getParameter("signature");	// 微信加密签名
				String timestamp = request.getParameter("timestamp");	// 时间戳
				String nonce = request.getParameter("nonce");			// 随机数
				String echostr = request.getParameter("echostr");		// 随机字符串
				WeixinRestaurantDao.verify(account, signature, timestamp, nonce);
				result = echostr;
			}
		}catch(Exception e){
			e.printStackTrace();
			result = "";
		}finally{
			out.write(result);
			out.flush();
			out.close();
		}
	}

}
