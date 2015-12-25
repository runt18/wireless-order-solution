package com.wireless.beeCloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public class BeeCloud {
	
	final static String DYNC = "apidynamic.beecloud.cn";
    final static String SZ = "https://apisz.beecloud.cn";
	final static String QD = "https://apiqd.beecloud.cn";
	final static String BJ = "https://apibj.beecloud.cn";
	final static String HZ = "https://apihz.beecloud.cn";
	
	final String appId;
	final String appSecret;
	final Status status;
	final Bill bill;
	final Revert revert;
	
	private BeeCloud(String appId, String appSecret){
		this.appId = appId;
		this.appSecret = appSecret;
		this.status = new Status(this);
		this.bill = new Bill(this);
		this.revert = new Revert(this);
	}
	
	public static BeeCloud registerApp(String appId, String appSecret){
		BeeCloud app = new BeeCloud(appId, appSecret);
		return app;
	}
	
	public Bill bill(){
		return this.bill;
	}
	
	public Status status(){
		return this.status;
	}
	
	public Revert revert(){
		return this.revert;
	}
	
	String createAppSign(long timestamp){
		return getMessageDigest(this.appId + timestamp + this.appSecret);
	}
	
    private String getMessageDigest(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
                'e', 'f' };
        try {
            byte[] buffer = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");

            // 使用指定的字节更新摘要
            mdTemp.update(buffer);

            // 获得密文
            byte[] md = mdTemp.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
	
    private static class SSLClient extends DefaultHttpClient{  
        public SSLClient() throws KeyManagementException, NoSuchAlgorithmException{  
            super();  
            SSLContext ctx = SSLContext.getInstance("TLS");  
            X509TrustManager tm = new X509TrustManager() {  
                    @Override  
                    public void checkClientTrusted(X509Certificate[] chain,  
                            String authType) throws CertificateException {  
                    }  
                    @Override  
                    public void checkServerTrusted(X509Certificate[] chain,  
                            String authType) throws CertificateException {  
                    }  
                    @Override  
                    public X509Certificate[] getAcceptedIssuers() {  
                        return null;  
                    }  
            };  
            ctx.init(null, new TrustManager[]{tm}, null);  
            SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
            ClientConnectionManager ccm = this.getConnectionManager();  
            SchemeRegistry sr = ccm.getSchemeRegistry();  
            sr.register(new Scheme("https", 443, ssf));  
        }  
    }  
    
	String doPost(String url, String param) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException{
		
	    HttpClient client = new SSLClient();
	    
        HttpPost request = new HttpPost(url);
        if(param != null && !param.isEmpty()){
        	StringEntity se = new StringEntity(param, HTTP.UTF_8);
        	//se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		    request.setEntity(se);
        }
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        
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
	            throw new IOException(response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase());
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
    
    public static void main(String[] args) throws Exception{
    	final String APP_ID = "c3918fd6-6162-4d21-815d-01b6757c673c";
    	final String APP_SECRET = "a4b4b3c0-79a6-49a9-ac00-fff8dd12d868";
    	final String BILL_NO = System.currentTimeMillis() + "";
    	final Bill.Channel CHANNEL = Bill.Channel.WX_NATIVE;
    	System.out.println(BILL_NO);
    	BeeCloud app = BeeCloud.registerApp(APP_ID, APP_SECRET);
    	System.out.println(app.bill().ask(new Bill.Request().setChannel(CHANNEL).setTotalFee(1).setBillNo(BILL_NO).setTitle("a"), null));
    	
    	Status.Response response;
    	do{
    		response = app.status().ask(CHANNEL, BILL_NO);
    		if(response != null){
    			System.out.println(response);
    		}
    		Thread.sleep(5000);
    		System.out.println(app.revert().ask(BILL_NO, CHANNEL));
    	}while(!response.isPaySuccess());
    	//System.out.println(app.status().ask(Bill.Channel.WX_SCAN, BILL_NO));
    }
    
}
