package com.wireless.Actions.weixin.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class WXQueryDianpingDataAction extends DispatchAction{

	public ActionForward getAllGroupBuying(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String fid = request.getParameter("fid");
		DBCon dbCon = null;
		
		try {
			dbCon = new DBCon();
			dbCon.connect();
			Restaurant restaurant = RestaurantDao.getById(dbCon, WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, fid));

	    	String appkey = "6373481645";  
	    	String secret = "21dcd218a828460bbea7d1977d7140a8";  
//	    	String apiUrl = "http://api.dianping.com/v1/deal/get_deals_by_business_id";  
	    	  
	    	// 创建参数表  
	    	Map<String, String> paramMap = new HashMap<String, String>();  
	    	paramMap.put("city", "广州");  
	    	paramMap.put("business_id",restaurant.getDianpingId()+"");
	    	  
	    	// 对参数名进行字典排序  
	    	String[] keyArray = paramMap.keySet().toArray(new String[0]);  
	    	Arrays.sort(keyArray);  
	    	  
	    	// 拼接有序的参数名-值串  
	    	StringBuilder stringBuilder = new StringBuilder();  
	    	stringBuilder.append(appkey);  
	    	for (String key : keyArray)  
	    	{  
	    	    stringBuilder.append(key).append(paramMap.get(key));  
	    	}  
	    	  
	    	stringBuilder.append(secret);  
	    	String codes = stringBuilder.toString();  
	    	String sign = org.apache.commons.codec.digest.DigestUtils.shaHex(codes).toUpperCase();
	    	
			String data = HttpRequest("http://api.dianping.com/v1/deal/get_deals_by_business_id?appkey=6373481645&sign="+sign+"&business_id=" + restaurant.getDianpingId() +"&city=%E5%B9%BF%E5%B7%9E");
			
//			System.out.println(JSON.parseObject(data).getString("status"));
			response.getWriter().print(data);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
		}
		return null;
	}
	
	
	
	private static String HttpRequest(String requestUrl) {
        StringBuffer sb = new StringBuffer();
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

}
