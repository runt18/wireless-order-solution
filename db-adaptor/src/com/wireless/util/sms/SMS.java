package com.wireless.util.sms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;

import sun.misc.BASE64Encoder;

import com.alibaba.fastjson.JSON;
import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ModuleError;
import com.wireless.exception.SMSError;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;

public final class SMS {
	
	private final static String API_KEY = "api:1f93e050d9f9dcb083c4c2782074d67b";
	
	private SMS(){}
	
	static class Msg{
		private final String sign;
		private final String content;
		private final SMSDetail.Operation operation;
		
		Msg(String content, String sign, SMSDetail.Operation operation){
			this.content = content;
			this.sign = sign;
			this.operation = operation;
		}
		
		@Override
		public String toString(){
			return content + "【" + (sign.trim().isEmpty() ? "微信餐厅" : sign) + "】";
		}
	}
	
	public static class Msg4Verify extends Msg{
		public Msg4Verify(int code){
			super("您本次操作的验证码是" + code, null, SMSDetail.Operation.USE_VERIFY);
		}
	}
	
	public static class Msg4Consume extends Msg{
		public Msg4Consume(MemberOperation mo){
			super("尊敬的会员，您本次消费" + NumericUtil.float2String(mo.getPayMoney()) + "元" +
				  (mo.getDeltaPoint() > 0 ? ("，积分" + mo.getDeltaPoint()) : "") +
				  "，余额" + (mo.getRemainingBaseMoney() + mo.getRemainingExtraMoney()) + "元" +
				  "，谢谢您的光临", 
				  null, SMSDetail.Operation.USE_CONSUME);
			
			if(mo.getOperationType() != OperationType.CONSUME){
				throw new IllegalArgumentException();
			}
		}
	}
	
	public static class Msg4Charge extends Msg{
		public Msg4Charge(MemberOperation mo){
			super("尊敬的会员，您本次充值实收" + NumericUtil.float2String2(mo.getChargeMoney()) + "元" +
				  "，充额" + NumericUtil.float2String2(mo.getDeltaBaseMoney() + mo.getDeltaExtraMoney()) + "元" +
				  "，余额" + NumericUtil.float2String2(mo.getRemainingBaseMoney() + mo.getRemainingExtraMoney()) + "元", 
				  null, SMSDetail.Operation.USE_CHARGE);
			
			if(mo.getOperationType() != OperationType.CHARGE){
				throw new IllegalArgumentException();
			}
		}
	}
	
	public static class Msg4Refund extends Msg{
		public Msg4Refund(MemberOperation mo){
			super("尊敬的会员，您本次退款实退" + NumericUtil.float2String2(Math.abs(mo.getChargeMoney())) + "元" +
				  "，扣额" + NumericUtil.float2String2(Math.abs(mo.getDeltaBaseMoney() + mo.getDeltaExtraMoney())) + "元" +
				  "，余额" + NumericUtil.float2String2(mo.getRemainingBaseMoney() + mo.getRemainingExtraMoney()) + "元", 
				  null, SMSDetail.Operation.USE_CHARGE);
			
			if(mo.getOperationType() != OperationType.REFUND){
				throw new IllegalArgumentException();
			}
		}
	}
	
	public static enum Status{
		_0(0, "发送成功"),
		_10(-10, "验证信息失败"),
		_20(-20, "短信余额不足"),
		_30(-30, "短信内容为空"),
		_31(-31, "短信内容存在敏感词"),
		_32(-32, "短信内容确少签名信息"),
		_40(-40, "错误的手机号"),
		_50(-50, "请求发送IP不在白名单内	");
		
		Status(int val, String msg){
			this.val = val;
			this.msg = msg;
		}
		
		private final int val;
		private final String msg;
		
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
			for(Status status : values()){
				if(val == status.getVal()){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return getMsg();
		}
	}
	
	/**
	 * Send the SMS.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param mobile
	 * 			the mobile to send SMS
	 * @param msg
	 * 			the content to send SMS
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below.
	 * 			<li>the restaurant does NOT has SMS module
	 * 			<li>insufficient SMS to send
	 * @throws ClientProtocolException
	 * 			throws if failed to send SMS
	 * @throws IOException
	 * 			throws if failed to send SMS
	 */
	public static void send(Staff staff, String mobile, SMS.Msg msg) throws SQLException, BusinessException, ClientProtocolException, IOException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			send(dbCon, staff, mobile, msg);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Send the SMS.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param mobile
	 * 			the mobile to send SMS
	 * @param msg
	 * 			the content to send SMS
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below.
	 * 			<li>the restaurant does NOT has SMS module
	 * 			<li>insufficient SMS to send
	 * @throws ClientProtocolException
	 * 			throws if failed to send SMS
	 * @throws IOException
	 * 			throws if failed to send SMS
	 */
	public static void send(DBCon dbCon, Staff staff, String mobile, SMS.Msg msg) throws SQLException, BusinessException, ClientProtocolException, IOException{
		//Check to see whether the restaurant has SMS module.
		Restaurant restaurant = RestaurantDao.getById(dbCon, staff.getRestaurantId());
		if(restaurant.hasModule(Module.Code.SMS)){
			//Check to see whether has the remaining SMS to send.
			if(SMStatDao.get(dbCon, staff).getRemaining() > 0){
				Status status = send(mobile, new Msg(msg.content, restaurant.getName(), msg.operation));
				if(status.isSuccess()){
					//Log the SMS record if succeed to send.
					SMStatDao.update(dbCon, staff, new SMStat.UpdateBuilder(staff.getRestaurantId(), msg.operation).setAmount(1));
				}else{
					throw new BusinessException(status.msg);
				}
			}else{
				throw new BusinessException(SMSError.INSUFFICIENT_SMS_AMOUNT);
			}
		}else{
			throw new BusinessException(ModuleError.SMS_LIMIT);
		}
	}
	
	private static Status send(String mobile, Msg msg) throws ClientProtocolException, IOException{
        DefaultHttpClient client = new DefaultHttpClient();

        client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                request.addHeader("Accept-Encoding", "gzip");
                request.addHeader("Authorization", "Basic " + new BASE64Encoder().encode(API_KEY.getBytes("utf-8")));
            }

        });

        client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);

        HttpPost request = new HttpPost("https://sms-api.luosimao.com/v1/send.json");

        ByteArrayOutputStream bos = null;
        InputStream bis = null;
        byte[] buf = new byte[10240];

        String content = null;
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mobile", mobile));
            params.add(new BasicNameValuePair("message", msg.toString()));
            request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));


            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                bis = response.getEntity().getContent();
                Header[] gzip = response.getHeaders("Content-Encoding");

                bos = new ByteArrayOutputStream();
                int count;
                while ((count = bis.read(buf)) != -1) {
                    bos.write(buf, 0, count);
                }
                bis.close();

                if (gzip.length > 0 && gzip[0].getValue().equalsIgnoreCase("gzip")) {
                    GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(bos.toByteArray()));
                    StringBuffer sb = new StringBuffer();
                    int size;
                    while ((size = gzin.read(buf)) != -1) {
                        sb.append(new String(buf, 0, size, "utf-8"));
                    }
                    gzin.close();
                    bos.close();

                    content = sb.toString();
                } else {
                    content = bos.toString();
                }
                
                return Status.valueOf(Integer.parseInt(JSON.parseObject(content).get("error").toString()));
                
            } else {
                throw new IOException("error code is " + response.getStatusLine().getStatusCode());
            }

        } finally {
            if (bis != null) {
                try {
                    bis.close();// 最后要关闭BufferedReader
                } catch (Exception e) {
                }
            }
        }
	}
	
	
	
}
