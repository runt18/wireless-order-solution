package org.marker.weixin.js;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.UUID;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class JsApiSign implements Jsonable{
	
	private final String appId;
	private final JsApiTicket ticket;
	private final String url;
	
	public static JsApiSign newInstance(String appId, JsApiTicket ticket, String url){
		return new JsApiSign(appId, ticket, url);
	}

	private JsApiSign(String appId, JsApiTicket ticket, String url) {
		this.appId = appId;
		this.ticket = ticket;
		this.url = url;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
        JsonMap jm = new JsonMap();
        
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + this.ticket.getTicket() +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + this.url;
        try{
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }catch (NoSuchAlgorithmException | UnsupportedEncodingException e){
            e.printStackTrace();
        }

        jm.putString("url", this.appId);
        jm.putString("jsapi_ticket", this.ticket.getTicket());
        jm.putString("nonceStr", nonce_str);
        jm.putString("timestamp", timestamp);
        jm.putString("signature", signature);
        jm.putString("appId", this.appId);

        return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
	
    private String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash){
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }	
}
