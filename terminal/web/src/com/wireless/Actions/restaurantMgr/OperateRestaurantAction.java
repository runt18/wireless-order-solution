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
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Module.Code;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.restaurantMgr.Restaurant.InsertBuilder;
import com.wireless.pojo.restaurantMgr.Restaurant.RecordAlive;
import com.wireless.pojo.restaurantMgr.Restaurant.UpdateBuilder;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.restaurant.WeixinRestaurant;

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
		String moduleCheckeds = request.getParameter("moduleCheckeds");
		//String activeness = request.getParameter("activeness");
		try{
			Restaurant.InsertBuilder builder = new InsertBuilder(account, name, DateUtil.parseDate(expireDate), pwd)
												.setAddress(address)
												.setRecordAlive(RecordAlive.valueOf(Integer.parseInt(recordAlive)))
												.setRestaurantInfo(info)
												.setTele1(tele1)
												.setTele2(tele2);
			if(moduleCheckeds != null && !moduleCheckeds.isEmpty() ){
				String[] modules = moduleCheckeds.split(",");
				for (String module : modules) {
					builder.addModule(Code.valueOf(Integer.parseInt(module)));
				}
			}
			
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
		String moduleCheckeds = request.getParameter("moduleCheckeds");
		
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
			if(moduleCheckeds != null && !moduleCheckeds.isEmpty() ){
				String[] modules = moduleCheckeds.split(",");
				for (String module : modules) {
					builder.addModule(Code.valueOf(Integer.parseInt(module)));
				}
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
			String id = request.getParameter("restaurantID"); 
			
			Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(id));
			
			Restaurant.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(id), restaurant.getAccount());
			builder.setRestaurantName(restaurant_name)
					.setTele1(tele1)
					.setTele2(tele2)
					.setAddress(address)
					.setRestaurantInfo(restaurant_info);
			
			RestaurantDao.update(builder);
			
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
			int rid = Integer.parseInt(request.getAttribute("restaurantID").toString());
			
			if(!info.isEmpty()){
//				info = info.replaceAll("&", "&amp;")
//						.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;")
//						.replaceAll("\n\r", "&#10;").replaceAll("\r\n", "&#10;").replaceAll("\n", "&#10;")
//						.replaceAll(" ", "&#032;").replaceAll("'", "&#039;").replaceAll("!", "&#033;");
	    		
				WeixinRestaurantDao.update(StaffDao.getAdminByRestaurant(rid), new WeixinRestaurant.UpdateBuilder().setWeixinInfo(info));
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
	public ActionForward getInfo(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			final String info = WeixinRestaurantDao.get(StaffDao.getAdminByRestaurant(Integer.valueOf(request.getAttribute("restaurantID").toString()))).getWeixinInfo();
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("info", info);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
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
			final StringBuilder logo = new StringBuilder(WeixinRestaurantDao.get(StaffDao.getAdminByRestaurant(Integer.valueOf(request.getAttribute("restaurantID").toString()))).getWeixinLogo());
			if(logo.length() == 0){
				logo.setLength(0);
				logo.append(getServlet().getInitParameter("imageBrowseDefaultFile"));
			}else{
				String path = "http://" + getServlet().getInitParameter("oss_bucket_image")	+
							   "." + getServlet().getInitParameter("oss_outer_point") + "/";
				logo.insert(0, path);
			}
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("logo", logo.toString());
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
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
