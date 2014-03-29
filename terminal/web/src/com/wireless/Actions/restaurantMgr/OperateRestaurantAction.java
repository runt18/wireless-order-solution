package com.wireless.Actions.restaurantMgr;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.WeixinInfoDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.restaurantMgr.Restaurant.InsertBuilder;
import com.wireless.pojo.restaurantMgr.Restaurant.RecordAlive;
import com.wireless.pojo.restaurantMgr.Restaurant.UpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.weixinInfo.WeixinInfo;

public class OperateRestaurantAction extends DispatchAction {
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		String account = request.getParameter("account");
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		String info = request.getParameter("info");
		String tele1 = request.getParameter("tele1");
		String tele2 = request.getParameter("tele2");
		String address = request.getParameter("address");
		String recordAlive = request.getParameter("recordAlive");
		String expireDate = request.getParameter("expireDate");
		//String activeness = request.getParameter("activeness");
		try{
			Restaurant.InsertBuilder builder = new InsertBuilder(account, name, DateUtil.parseDate(expireDate), pwd)
												.setAddress(address)
												.setRecordAlive(RecordAlive.valueOf(Integer.parseInt(recordAlive)))
												.setRestaurantInfo(info)
												.setTele1(tele1)
												.setTele2(tele2);
			
			RestaurantDao.insert(builder);
			jobject.initTip(true, "添加成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception{
		
		String id = request.getParameter("id");
		String account = request.getParameter("account");
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		String info = request.getParameter("info");
		String tele1 = request.getParameter("tele1");
		String tele2 = request.getParameter("tele2");
		String address = request.getParameter("address");
		String recordAlive = request.getParameter("recordAlive");
		String expireDate = request.getParameter("expireDate");
		
		JObject jobject = new JObject();
		try{
			Restaurant.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(id), account);
			builder.setRestaurantName(name)
					.setTele1(tele1)
					.setTele2(tele2)
					.setAddress(address)
					.setRecordAlive(RecordAlive.valueOf(Integer.parseInt(recordAlive)))
					.setExpireDate(DateUtil.parseDate(expireDate))
					.setRestaurantInfo(info);
			
			if(pwd != null && !pwd.isEmpty()){
				builder.setPwd(pwd);
			}
			
			RestaurantDao.update(builder);
			
			jobject.initTip(true, "修改成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, "含有非法字符");
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
	public ActionForward systemUpdate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		PrintWriter out = response.getWriter();
		JObject jobject = new JObject();
		try{
			String restaurant_name = request.getParameter("restaurant_name");
			String restaurant_info = request.getParameter("restaurant_info");
			String address = request.getParameter("address");
			String tele1 = request.getParameter("tel1");
			String tele2 = request.getParameter("tel2");
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("restaurantID"); 
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			id = staff.getRestaurantId()+"";
			Restaurant restaurant = new Restaurant();
			restaurant.setId(Integer.parseInt(id));
			restaurant.setName(restaurant_name);
			restaurant.setInfo(restaurant_info);
			restaurant.setAddress(address);
			restaurant.setTele1(tele1);
			restaurant.setTele2(tele2);
			RestaurantDao.update(staff, restaurant);
			
			jobject.initTip(true, "操作成功!");
		} catch(BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			dbCon.disconnect();
			out.print(jobject.toString());
		}
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
	public ActionForward updateInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String info = request.getParameter("info");
			String rid = request.getAttribute("restaurantID").toString();
			
			if(!info.isEmpty()){
				info = info.replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;")
						.replaceAll("\n\r", "&#10;").replaceAll("\r\n", "&#10;").replaceAll("\n", "&#10;")
						.replaceAll(" ", "&#032;").replaceAll("'", "&#039;").replaceAll("!", "&#033;");
	    		WeixinInfo.UpdateBuilder builder = new WeixinInfo.UpdateBuilder(Integer.parseInt(rid));
	    		
	    		builder.setWeixinInfo(info);
	    		
	    		WeixinInfoDao.update(builder);
			}

    		
    		
//			WeixinRestaurantDao.updateInfo(Integer.valueOf(rid), info);
			jobject.initTip(true, "操作成功, 已修改微信餐厅简介信息.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
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
	public ActionForward getInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			jobject.getOther().put("info", WeixinRestaurantDao.getInfo(Integer.valueOf(request.getAttribute("restaurantID").toString())));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
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
	public ActionForward getLogo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String logo = WeixinRestaurantDao.getLogo(Integer.valueOf(request.getAttribute("restaurantID").toString()));
			if(logo == null || logo.trim().isEmpty()){
				logo = getServlet().getInitParameter("imageBrowseDefaultFile");
			}else{
				logo = "http://" + getServlet().getInitParameter("oss_bucket_image")
	    	    		+ "." + getServlet().getInitParameter("oss_outer_point") 
	    	    		+ "/" + logo;
			}
			jobject.getOther().put("logo", logo);
			jobject.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
