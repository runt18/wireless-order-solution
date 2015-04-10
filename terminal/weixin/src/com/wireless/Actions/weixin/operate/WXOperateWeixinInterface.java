package com.wireless.Actions.weixin.operate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.maker.weixin.auth.AuthorizerToken;
import org.marker.weixin.api.Token;

import com.wireless.Actions.weixin.auth.AuthParam;
import com.wireless.db.DBCon;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WXOperateWeixinInterface extends DispatchAction{
	public static class Ticket implements Jsonable{
		private int errcode;
		private String errmsg;
		private String ticket;
		private int expiresTime;

		public int getErrcode() {
			return errcode;
		}

		public void setErrcode(int errcode) {
			this.errcode = errcode;
		}

		public String getErrmsg() {
			return errmsg;
		}

		public void setErrmsg(String errmsg) {
			this.errmsg = errmsg;
		}

		public String getTicket() {
			return ticket;
		}

		public void setTicket(String ticket) {
			this.ticket = ticket;
		}

		public int getExpiresTime() {
			return expiresTime;
		}

		public void setExpiresTime(int expiresTime) {
			this.expiresTime = expiresTime;
		}

		public static Jsonable.Creator<Ticket> JSON_CREATOR = new Jsonable.Creator<Ticket>() {
			@Override
			public Ticket newInstance() {
				return new Ticket();
			}
		};		
		
		@Override
		public JsonMap toJsonMap(int flag) {
			return null;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			setErrcode(jsonMap.getInt("errcode"));
			setErrmsg(jsonMap.getString("errmsg"));
			setTicket(jsonMap.getString("ticket"));
			setExpiresTime(jsonMap.getInt("expires_in"));
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
	public ActionForward getToken(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
        
		Token token = Token.newInstance("wx99cd7d58d4e03702", "30f318b5655f47aca0afd12b6b5922a5");
		
        response.getWriter().print(token.getAccessToken());
        
		return null;
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
	public ActionForward getConfig(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = request.getParameter("url");
		String fromId = request.getParameter("fid");
//		String token = request.getParameter("token");
		DBCon dbCon = new DBCon();
		
		JObject jobject = new JObject();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
//			String appId = "wx49b3278a8728ff76";
//			
//			Token token = Token.newInstance(appId, "0ba130d87e14a1a37e20c78a2b0ee3ba");
			
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fromId);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			WxRestaurant wxRes = WxRestaurantDao.get(staff);
			
			System.out.println("wxAppId="+wxRes.getWeixinAppId() + ", token=" + wxRes.getRefreshToken());
			
			AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRes.getWeixinAppId(), wxRes.getRefreshToken());
			
			WxRestaurantDao.update(staff, new WxRestaurant.UpdateBuilder().setRefreshToken(authorizerToken.getRefreshToken()));
			
			System.out.println("acToken="+authorizerToken.getAccessToken());
			
			String ticketJson = HttpRequest("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+ authorizerToken.getAccessToken() +"&type=jsapi");

			System.out.println("ticket=" + ticketJson);
			
			Ticket ticket = JObject.parse(Ticket.JSON_CREATOR, 0, ticketJson);			

	        // 注意 URL 一定要动态获取，不能 hardcode
//	        String url = "http://example.com";
//			String jsapi_ticket = request.getParameter("jsapi_ticket");
			
	        JsonMap ret = sign(ticket.getTicket(), url);
	        ret.putString("appId", wxRes.getWeixinAppId());
	        
	        final JsonMap config = ret;
	        
//	        for (Entry<String, Object> entry : ret.entrySet()) {
//	            System.out.println(entry.getKey() + ", " + entry.getValue());
//	        }
	        
	        jobject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					return config;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	

	private static String HttpRequest(String requestUrl) {
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
	
    private static InputStream getInputStream(String requestUrl) {
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
	
	private static JsonMap sign(String jsapi_ticket, String url) {
//        Map<String, String> ret = new HashMap<String, String>();
        JsonMap ret = new JsonMap();
        
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + url;
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ret.putString("url", url);
        ret.putString("jsapi_ticket", jsapi_ticket);
        ret.putString("nonceStr", nonce_str);
        ret.putString("timestamp", timestamp);
        ret.putString("signature", signature);
        

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }	

}
