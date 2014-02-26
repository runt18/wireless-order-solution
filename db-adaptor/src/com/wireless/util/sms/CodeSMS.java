package com.wireless.util.sms;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wireless.pojo.sms.VerifySMS;
import com.wireless.pojo.sms.VerifySMS.ExpiredPeriod;
import com.wireless.pojo.sms.VerifySMS.InsertBuilder;

public class CodeSMS{
	public static final String SIGN = "智易科技";
	public static final String CODE_CONTENT = "您本次操作的验证码是：{code}，如果不您本人操作请联系客服!【{sign}】";
	private static final boolean IS_OPEN = true; // 是否开启短信功能, 后期改为用户配置
	private static final String USER_ID = "3913"; //"4311"; 
	private static final String ACCOUNT = "ddxxkj666"; //"fcr2013";
	private static final String PASSWORD = "qq123123";
	private static final String URL = "http://inter.ueswt.com/smsGBK.aspx?";
	private static final String ACTION = "send";
	private static int TIMEOUT = 5 * 1000;
	
	private CodeSMS(){}
	public static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	public enum Status{
		CLOSE_FUNCTION("closeFunction", "系统已关闭短信功能."),
		FAILD("Faild", "短信发送失败."),
		SUCCESS("Success", "短信发送成功.");
		
		Status(String status, String msg){
			this.status = status;
			this.msg = msg;
		}
		private String status;
		private String msg;
		
		public String getStatus(){
			return this.status;
		}
		public String getMsg(){
			return this.msg;
		}
		public boolean isClose(){
			return this == Status.CLOSE_FUNCTION;
		}
		public boolean isSuccess(){
			return this == Status.SUCCESS;
		}
		
		public String toString(){
			return "status: "+this.status+", msg:"+this.msg;
		}
		
		public static Status parse(String val) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException{
//			System.out.println(val);
			Document document = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(val.getBytes("GBK")));
			NodeList list = document.getElementsByTagName("returnsms").item(0).getChildNodes();
			for(int i = 0; i < list.getLength(); i++){   
				if(list.item(i).getNodeName().equals("returnstatus")){
					if(list.item(i).getTextContent().trim().equals(CodeSMS.Status.SUCCESS.getStatus())){
						return CodeSMS.Status.SUCCESS;
					}else if(list.item(i).getTextContent().trim().equals(CodeSMS.Status.FAILD.getStatus())){
						return CodeSMS.Status.FAILD;
					}
				}
	        }
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
	}
	
	/**
	 * 发送短信, 返回响应状态
	 * @param mobile
	 * @param content
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static Status send(String mobile, int code, String sign) throws UnsupportedEncodingException, IOException, Exception {
		if(CodeSMS.IS_OPEN){
			HttpURLConnection httpconn = null;
			String result = "";
			String body = CodeSMS.CODE_CONTENT.replace("{code}", String.valueOf(code)).replace("{sign}", sign == null ? CodeSMS.SIGN : sign);
			System.out.println("短信内容: " + body);
			StringBuilder sb = new StringBuilder();
			sb.append(CodeSMS.URL)
			  .append("userid=").append(CodeSMS.USER_ID)
			  .append("&account=").append(CodeSMS.ACCOUNT)
			  .append("&password=").append(CodeSMS.PASSWORD)
			  .append("&mobile=").append(mobile)
			  .append("&content=").append(URLEncoder.encode(body, "gb2312"))
			  .append("&action=").append(CodeSMS.ACTION);
			try {
				URL url = new URL(sb.toString());
				httpconn = (HttpURLConnection) url.openConnection();
				httpconn.setConnectTimeout(CodeSMS.TIMEOUT);
				BufferedReader rd = new BufferedReader(new InputStreamReader(httpconn.getInputStream(), "gb2312"));
				while(rd.ready()){
					result += rd.readLine();
				}
				rd.close();
			} catch (MalformedURLException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} catch(Exception e) {
				throw e;
			} finally{
				if(httpconn != null){
					httpconn.disconnect();
					httpconn = null;
				}
			}
			return Status.parse(result);
		}else{
			return Status.CLOSE_FUNCTION;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private static int createCode(){
		int code = 0;
		boolean s = true;
		while(s){
			code = (int)(Math.random() * 10000);
			if(code > 1000 && code < 9999){
				s = false;
			}
		}
		return code;
	}
	
	public static void main(String[] args){
//		operate(1);
		operate(2);
	}
	
	private static void operate(int type){
		if(type == 1){
			System.out.println("当前时间: " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
		}else if(type == 2){
			try {
				VerifySMS builder = null;
				for(int i=1; i<= 3; i++){
					builder = new InsertBuilder(ExpiredPeriod.MINUTE_10).build();
//					Status temp = CodeUtil.send("15999955793", createCode(), null);
					Status temp = CodeSMS.send("15999955793", builder.getCode(), null);
					if(!temp.isClose() && !temp.isSuccess()){
						System.out.println("警告: 验证码信息短信发送失败, 远程接口响应异常.");
					}
					System.out.println("响应结果:  " + temp.getMsg());
					operate(1);
				}
			} catch (Exception e) {
				System.out.println("异常: 验证码短信发送失败, 程序运行异常.");
				e.printStackTrace();
			}
		}
	}
}
