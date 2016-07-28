package com.wireless.Actions.weixin.redirect;


import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.Actions.weixin.WxHandleMessage;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class QueryWxRediectAction extends DispatchAction {
	public ActionForward getUrlJumpByKey(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String restaurantId = request.getParameter("restaurantId");
		final String key = request.getParameter("key");
		final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
		final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
		final String callback = request.getParameter("callback");
		final JObject jObject = new JObject();
		try {
			
//			https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx49b3278a8728ff76&redirect_uri=http%3a%2f%2fwx.e-tones.net%2fwx-term%2fweixin%2forder%2fredirect.html%3ffid%3dgh_6a79ab99c6b9%26href%3dmember.html&response_type=code&scope=snsapi_base&state=123#wechat_redirect
			final String serverName;
			if(getServlet().getInitParameter("wxServer") != null){
				serverName = getServlet().getInitParameter("wxServer");
			}else{
				serverName = "wx.e-tones.net"; 
			}
			final String url;
			final String path1 = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wxRestaurant.getWeixinAppId() + "&redirect_uri="; 
			final String path2 = "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
			final String encoderUrl = "http://" + serverName + "/wx-term/weixin/order/redirect.html?fid=" + wxRestaurant.getWeixinSerial();
			if(WxHandleMessage.EventKey.SELF_ORDER_EVENT_KEY.getKey().equals(key)){
				url = path1 + URLEncoder.encode(encoderUrl + "&href=food.html", "utf-8") + path2;
			}else if(WxHandleMessage.EventKey.SELF_BOOK_EVENT_KEY.getKey().equals(key)){
				url = path1 + URLEncoder.encode(encoderUrl + "&href=book.html", "utf-8") + path2;
			}else if(WxHandleMessage.EventKey.MEMBER_EVENT_KEY.getKey().equals(key)){
				url = path1 + URLEncoder.encode(encoderUrl + "&href=member.html", "utf-8") + path2;
			}else if(WxHandleMessage.EventKey.ORDER_EVENT_KEY.getKey().equals(key)){
				url = path1 + URLEncoder.encode(encoderUrl + "&href=orderList.html", "utf-8") + path2;
			}else if(WxHandleMessage.EventKey.PROMOTION_EVENT_KEY.getKey().equals(key)){
				url = path1 + URLEncoder.encode(encoderUrl, "&href=sales.html") + path2;
			}else if(WxHandleMessage.EventKey.I_WANT_REPRESENT.getKey().equals(key)){
				url = path1 + URLEncoder.encode(encoderUrl, "&href=representCard.html") + path2;
			}else{
				throw new BusinessException("该功能不支持直接跳转..");
			}
			
			jObject.setRoot(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("url", url);
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		return null;
	}
}
