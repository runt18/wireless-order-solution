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

import org.apache.http.client.ClientProtocolException;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.api.Button;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;
import org.marker.weixin.session.WxSession;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

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
							menu.set1stButton(new Button.ClickBuilder("餐厅导航", WxHandleMessage.EventKey.NAVI_EVENT_KEY.getKey()).build());
							menu.set2ndButton(new Button.ClickBuilder("优惠活动", WxHandleMessage.EventKey.PROMOTION_EVENT_KEY.getKey()).build());
							
							menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
											.addChild(new Button.ClickBuilder("我的订单", WxHandleMessage.EventKey.ORDER_EVENT_KEY.getKey()))
											.addChild(new Button.ClickBuilder("我的会员卡", WxHandleMessage.EventKey.MEMBER_EVENT_KEY.getKey()))
											.build());
							if(menu.create(token).isOk()){
								//Record the app id & secret.
								WxRestaurantDao.update(StaffDao.getAdminByRestaurant(RestaurantDao.getByAccount(account).getId()), 
														   new WxRestaurant.UpdateBuilder().setWeixinAppId(appId).setWeixinAppSecret(appSecret));
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
		final InputStream is = request.getInputStream();
		final OutputStream os = response.getOutputStream();
		final WxSession session = WxSession.newInstance();
		try{
			session.addOnHandleMessageListener(new WxHandleMessage(getServlet().getInitParameter("wxServer"), request, session));
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
				WxRestaurantDao.verify(account, signature, timestamp, nonce);
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
	
	public static void main(String[] args) throws ClientProtocolException, IOException{
		Menu menu = new Menu();
		Token token = Token.newInstance("wx99cd7d58d4e03702", "30f318b5655f47aca0afd12b6b5922a5");
		Menu.delete(token);
		menu.set1stButton(new Button.ClickBuilder("餐厅导航", WxHandleMessage.EventKey.NAVI_EVENT_KEY.getKey()).build());
		menu.set2ndButton(new Button.ClickBuilder("扫码支付", WxHandleMessage.EventKey.SCAN_EVENT_KEY.getKey()).build());
		
		menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
						.addChild(new Button.ClickBuilder("我的订单", WxHandleMessage.EventKey.ORDER_EVENT_KEY.getKey()))
						.addChild(new Button.ClickBuilder("我的会员卡", WxHandleMessage.EventKey.MEMBER_EVENT_KEY.getKey()))
						.addChild(new Button.ClickBuilder("优惠活动", WxHandleMessage.EventKey.PROMOTION_EVENT_KEY.getKey()))
						//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
						.build());
		
		menu.create(token);
	}

}
