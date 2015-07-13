package org.marker.weixin.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.alibaba.fastjson.JSON;
import com.wireless.json.JObject;
import com.wireless.json.Jsonable;

public final class BaseAPI {
	public static final String BASE_URI = "https://api.weixin.qq.com";
	public static final String MEDIA_URI = "http://file.api.weixin.qq.com";
	public static final String QRCODE_DOWNLOAD_URI = "https://mp.weixin.qq.com";
	
	public static String doGet(String url) throws ClientProtocolException, IOException{
		return doPost(url, "");
	}
	
	public static Status doPost(String url, Jsonable jsonable) throws ClientProtocolException, IOException{
		return JObject.parse(Status.JSON_CREATOR, 0, doPost(url, JSON.toJSONString(jsonable.toJsonMap(0))));
	}
	
	public static Status doPost(String url, Jsonable jsonable, int flag) throws ClientProtocolException, IOException{
		return JObject.parse(Status.JSON_CREATOR, 0, doPost(url, JSON.toJSONString(jsonable.toJsonMap(flag))));
	}
	
	public static String doPost(String url, String param) throws ClientProtocolException, IOException{
		
	    DefaultHttpClient client = new DefaultHttpClient();
	    
//	    TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
//	        public boolean isTrusted(X509Certificate[] certificate, String authType) {
//	            return true;
//	        }
//	    };
	    
//		try {
//			SSLSocketFactory sf = new SSLSocketFactory(acceptingTrustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//			client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sf));
//			
//		}catch (KeyManagementException e) {
//			e.printStackTrace();
//		} catch (UnrecoverableKeyException e) {
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (KeyStoreException e) {
//			e.printStackTrace();
//		}
        HttpPost request = new HttpPost(url);
        if(param != null && !param.isEmpty()){
		    request.setEntity(new StringEntity(param, HTTP.UTF_8));
        }
        
        ByteArrayOutputStream bos = null;
        InputStream bis = null;
        byte[] buf = new byte[10240];

        String content = null;
        
	    try{

	        HttpResponse response = client.execute(request);
	
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            bis = response.getEntity().getContent();
	
	            bos = new ByteArrayOutputStream();
	            int count;
	            while ((count = bis.read(buf)) != -1) {
	                bos.write(buf, 0, count);
	            }
	            bis.close();
	            content = bos.toString("UTF-8");
	            
	        } else {
	            throw new IOException("error code is " + response.getStatusLine().getStatusCode());
	        }
			
			return content;
	    }finally{
            if (bis != null) {
                try {
                    bis.close();// 最后要关闭BufferedReader
                } catch (Exception ignored) { }
            }
	    }
	}


}
