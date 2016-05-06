package com.wireless.sms;

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

import com.alibaba.fastjson.JSON;
import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.SMSError;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sms.msg.Msg;

import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public final class SMS {
	
	private final static String API_KEY = "api:1f93e050d9f9dcb083c4c2782074d67b";
	
	private SMS(){}
	
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
	 * @throws IOException
	 * 			throws if failed to send SMS
	 */
	public static void send(Staff staff, String mobile, Msg msg) throws SQLException, BusinessException, IOException{
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
	 * @throws IOException
	 * 			throws if failed to send SMS
	 */
	public static void send(DBCon dbCon, Staff staff, String mobile, Msg msg) throws SQLException, BusinessException, IOException{
		//Check to see whether the restaurant has SMS module.
		Restaurant restaurant = RestaurantDao.getById(dbCon, staff.getRestaurantId());
		if(restaurant.hasModule(Module.Code.SMS)){
			//Check to see whether has the remaining SMS to send.
			if(SMStatDao.get(dbCon, staff).getRemaining() > 0){
				Status status = send(mobile, new Msg(msg.getContent(), restaurant.getName(), msg.getOperation()));
				if(status.isSuccess()){
					//Log the SMS record if succeed to send.
					SMStatDao.update(dbCon, staff, new SMStat.UpdateBuilder(staff.getRestaurantId(), msg.getOperation()).setAmount(1));
				}else{
					throw new BusinessException(status.msg);
				}
			}else{
				throw new BusinessException(SMSError.INSUFFICIENT_SMS_AMOUNT);
			}
		}else{
			//throw new BusinessException(ModuleError.SMS_LIMIT);
		}
	}
	
	private static Status send(String mobile, Msg msg) throws IOException{
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
                    StringBuilder sb = new StringBuilder();
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

        }catch(ClientProtocolException e){
        	throw new IOException(e);
        	
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
