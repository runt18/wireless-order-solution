package com.wireless.Actions.weixin.operate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;
import org.marker.weixin.js.JsApiSign;
import org.marker.weixin.js.JsApiTicket;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.finance.WeixinFinanceDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.listener.SessionListener;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxOperateInterfaceAction extends DispatchAction{
	
	public static class AccessToken implements Jsonable{
		private String access_token;
		private String refresh_token;
		private String openid;


		public String getAccess_token() {
			return access_token;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public String getRefresh_token() {
			return refresh_token;
		}

		public void setRefresh_token(String refresh_token) {
			this.refresh_token = refresh_token;
		}

		public String getOpenid() {
			return openid;
		}

		public void setOpenid(String openid) {
			this.openid = openid;
		}

		public static Jsonable.Creator<AccessToken> getJSON_CREATOR() {
			return JSON_CREATOR;
		}

		public static void setJSON_CREATOR(Jsonable.Creator<AccessToken> jSON_CREATOR) {
			JSON_CREATOR = jSON_CREATOR;
		}

		public static Jsonable.Creator<AccessToken> JSON_CREATOR = new Jsonable.Creator<AccessToken>() {
			@Override
			public AccessToken newInstance() {
				return new AccessToken();
			}
		};		
		
		@Override
		public JsonMap toJsonMap(int flag) {
			return null;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			setAccess_token(jsonMap.getString("access_token"));
			setRefresh_token(jsonMap.getString("refresh_token"));
			setOpenid(jsonMap.getString("openid"));
		}
		
	}		
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward jsApiSign(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String url = request.getParameter("url");
		final String sessionId = request.getParameter("sessionId");
		final JObject jObject = new JObject();
		try{
			String fromId;
			if(sessionId != null && !sessionId.isEmpty()){
				final HttpSession session = SessionListener.sessions.get(sessionId);
				fromId = (String)session.getAttribute("fid");
			}else{
				fromId = request.getParameter("fid");
			}
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			WxRestaurant wxRes = WxRestaurantDao.get(staff);
			
			//System.out.println("wxAppId=" + wxRes.getWeixinAppId() + ", token=" + wxRes.getRefreshToken());
			
			AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRes.getWeixinAppId(), wxRes.getRefreshToken());
			
			//System.out.println(authorizerToken.getRefreshToken());
			
			WxRestaurantDao.update(staff, new WxRestaurant.UpdateBuilder().setRefreshToken(authorizerToken.getRefreshToken()));
			
	        jObject.setExtra(JsApiSign.newInstance(wxRes.getWeixinAppId(), JsApiTicket.newInstance(authorizerToken), url));
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
	/**
	 * 获取OpenId
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getOpenid(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String APP_ID = "wx6fde9cd2c7fc791e";
		final String APP_SECRET = "0a360a43b80e3a334e5e52da706a3134";
		
		String code = request.getParameter("code");
		AccessToken token = null;
		int rid = 0;
		try {
			String json = HttpRequest("https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ APP_ID +"&secret="+ APP_SECRET +"&code="+ code +"&grant_type=authorization_code");

			token = JObject.parse(AccessToken.JSON_CREATOR, 0, json);
			
			rid = WeixinFinanceDao.getRestaurantIdByWeixin(token.getOpenid());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			String path = "http://" + request.getLocalAddr() + "/wx-term/weixin/order/generalReport.html?m=" + token.getOpenid() + "&rid=" + rid +"&time=" + new Date().getTime();
			response.getWriter().print(path);
		}
		return null;
	}	

	/**
	 * 获取餐厅信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getRestaurant(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String rid = request.getParameter("rid");
		JObject jobject = new JObject();
		try {
			
			Restaurant r = RestaurantDao.getById(Integer.parseInt(rid));
			jobject.setRoot(r);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}		
	

	private String HttpRequest(String requestUrl) {
        StringBuilder sb = new StringBuilder();
        InputStream ips = getInputStream(requestUrl);
        InputStreamReader isreader = null;
        try {
            isreader = new InputStreamReader(ips, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(isreader);
        String temp = null;
        try {
            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp);
            }
            bufferedReader.close();
            isreader.close();
            ips.close();
            ips = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
	
    private InputStream getInputStream(String requestUrl) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.connect();
 
            in = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }		

}
