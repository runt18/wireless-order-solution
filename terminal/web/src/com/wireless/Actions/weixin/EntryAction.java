package com.wireless.Actions.weixin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.DefaultSession;
import org.marker.weixin.api.Button;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.weixin.restaurant.WeixinRestaurant;

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
	public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		String method = request.getMethod();
		if(method.equalsIgnoreCase("get")){
			verify(request, response);
			final String account = request.getParameter("account");
			final String appId = request.getParameter("appid");
			final String appSecret = request.getParameter("secret");
			//System.out.println("appId:" + appId + ", appSecret:" + appSecret);
			if(appId != null && appSecret != null){
				Executors.newScheduledThreadPool(1).schedule(new Runnable(){

					@Override
					public void run() {
						try{
							Menu menu = new Menu();
							Token token = Token.newInstance(appId, appSecret);
							Menu.delete(token);
							menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
							menu.set2ndButton(new Button.ClickBuilder("最新优惠", WeiXinHandleMessage.PROMOTION_EVENT_KEY).build());
							menu.set3rdButton(new Button.ClickBuilder("会员信息", WeiXinHandleMessage.MEMBER_EVENT_KEY).build());
							if(menu.create(token).isOk()){
								//Record the app id & secret.
								WeixinRestaurantDao.update(StaffDao.getAdminByRestaurant(RestaurantDao.getByAccount(account).getId()), 
														   new WeixinRestaurant.UpdateBuilder().setWeixinAppId(appId).setWeixinAppSecret(appSecret));
							}
						} catch (IOException | SQLException | BusinessException e) {
							e.printStackTrace();
						}
					}
					
				}, 5, TimeUnit.SECONDS);
			}
		}else{
			reply(request, response);
		}
		return null;
	}
	
	/**
	 * 回复信息
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void reply(HttpServletRequest request, HttpServletResponse response) throws IOException {
		InputStream is = request.getInputStream();
		OutputStream os = response.getOutputStream();
		DefaultSession session = DefaultSession.newInstance();
		try{
			String account = request.getParameter("account");
			session.addOnHandleMessageListener(new WeiXinHandleMessage(session, account));
			session.process(is, os);
		}finally{
			session.close();
		}
	}
	
	/**
	 * 验证
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void verify(HttpServletRequest request, HttpServletResponse response) throws IOException  {
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
