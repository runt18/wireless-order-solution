package com.wireless.util.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.wireless.pojo.sms.VerifySMS;
import com.wireless.pojo.sms.VerifySMS.ExpiredPeriod;

public final class TextSMS {
	private TextSMS(){}
	private static final String USERID = "fj1631000";
	private static final String USERPASS = "123456";
	private static final String URL_ENTRY = "http://jk.92c2.com";
	private static final String URL_SEND = URL_ENTRY + "/send.asp?";
	private static final String URL_INFO = URL_ENTRY + "/userinfo.asp?";
	private static int timeOut = 5 * 1000;
	
	public enum Status{
		_0(0, "发送成功"),
		_1(-1, "内容或号码为空"),
		_2(-2, "不支持长短信"),
		_3(-3, "用户或密码不正确"),
		_4(-4, "用户配置错误"),
		_5(-5, "该用户配置不支持长短信"),
		_6(-6, "内容过长"),
		_7(-7, "内容超出最大长度"),
		_8(-8, "余额不足"),
		_9(-9, "存在敏感词语"),
		_10(-10, "超过每天的测试额度"),
		_11(-11, "未知错误");
		
		Status(int val, String msg){
			this.val = val;
			this.msg = msg;
		}
		
		private int val;
		private String msg;
		public int getVal(){
			return val;
		}
		public String getMsg(){
			return msg;
		}
		public boolean isSuccess(){
			return this == _0;
		}
		
		public static Status valueOf(int val){
			for(Status temp : values()){
				if(val == temp.getVal()){
					return temp;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
	}
	
	/**
	 * 获取请求结果
	 * @param requestURL
	 * @return
	 * @throws IOException
	 */
	private static synchronized String getResult(String requestURL) throws IOException{
		String result = "-11";
		HttpURLConnection httpConn = null;
		try{
			URL url = new URL(requestURL);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(timeOut);
			BufferedReader rd = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "gb2312"));
			result = rd.readLine();
			rd.close();
		}finally{
			if(httpConn != null){
				httpConn.disconnect();
				httpConn = null;
			}
		}
		return result;
	}
	
	/**
	 * 发送普通文本信息
	 * @param mobile
	 * @param content
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws Exception
	 */
	public static Status sendTextSMS(String mobile, String content) throws UnsupportedEncodingException, IOException, Exception {
		String result = "-11";
		result = getResult(new StringBuilder().append(URL_SEND)
					.append("userid=").append(USERID)
					.append("&userpass=").append(USERPASS)
					.append("&urllongsms=" + (content.length() < 70 ? 0 : 1))
					.append("&mobile=").append(mobile)
					.append("&content=").append(URLEncoder.encode(content, "gb2312"))
					.toString());
		return Status.valueOf(Integer.valueOf(result.trim()));
	}
	
	/**
	 * 发送验证码信息
	 * @param mobile
	 * @param code
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws Exception
	 */
	public static Status sendCodeSMS(String mobile, int code, String sign) throws UnsupportedEncodingException, IOException, Exception{
		return sendTextSMS(mobile, CodeSMS.CODE_CONTENT.replace("{code}", code+"").replace("{sign}", sign == null ? CodeSMS.SIGN : sign)); 
	}
	
	/**
	 * 获取短信平台剩余短信记录数
	 * @return
	 * @throws IOException
	 */
	public static String getUserInfo() throws IOException{
		return getResult(new StringBuilder().append(URL_INFO)
				  .append("userid=").append(USERID)
				  .append("&userpass=").append(USERPASS).toString());
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException, Exception{
		System.out.println("当前剩余短信数: "+getUserInfo());
//		String msg = "短信test: " + (int)(Math.random() * 10000)
//				+ "   当前时间: " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()); 
//		Status status = sendTextSMS("15999955793", msg);
		Status status = sendCodeSMS("15999955793", new VerifySMS.InsertBuilder(ExpiredPeriod.MINUTE_10).build().getCode(), null);
		System.out.println("发送状态:  " + status.getMsg());
		System.out.println("当前剩余短信数: "+getUserInfo());
	}
	
}
