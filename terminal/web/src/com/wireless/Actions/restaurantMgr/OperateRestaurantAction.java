package com.wireless.Actions.restaurantMgr;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.restaurantMgr.Restaurant.InsertBuilder;
import com.wireless.pojo.restaurantMgr.Restaurant.RecordAlive;
import com.wireless.pojo.restaurantMgr.Restaurant.UpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.WebParams;

public class OperateRestaurantAction extends DispatchAction {
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("utf-8");
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
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception{
		response.setContentType("text/json; charset=utf-8");
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
					.setPwd(pwd)
					.setTele1(tele1)
					.setTele2(tele2)
					.setAddress(address)
					.setRecordAlive(RecordAlive.valueOf(Integer.parseInt(recordAlive)))
					.setExpireDate(DateUtil.parseDate(expireDate))
					.setRestaurantInfo(info);
			
			RestaurantDao.update(builder);
			
			jobject.initTip(true, "修改成功");
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage());
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
	
	public ActionForward systemUpdate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		
		response.setContentType("text/json; charset=utf-8");
		
		PrintWriter out = response.getWriter();
		
		JSONObject all = new JSONObject();
		
		JSONObject msg = new JSONObject();
		
		boolean success = false;
		
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
			
			success = true;
			
			msg.put("success", success);
			
			msg.put("message", "操作成功!");
			
		}
		catch(BusinessException e){
			
			e.printStackTrace();
			
			success = false;
			
			msg.put("success", success);
			
			msg.put("message", e.getDesc());
			
		}
		finally{
			
			dbCon.disconnect();
			
			all.put("all", msg);
			
			out.write(all.toString());
			
			out.flush();
			
			out.close();
			
		}
		
		return null;
	}
}
