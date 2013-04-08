package com.wireless.Actions.restaurantMgr;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.Terminal;

public class RestaurantUpdateAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		
		PrintWriter out = null;
		String restaurant_name = request.getParameter("restaurant_name");
		String restaurant_info = request.getParameter("restaurant_info");
		String address = request.getParameter("address");
		String tele1 = request.getParameter("tel1");
		String tele2 = request.getParameter("tel2");
		String pin = request.getParameter("pin");
		String id = request.getParameter("restaurantID"); 
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			id = term.restaurantID+"";
			dbCon.disconnect();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		response.setContentType("text/json; charset=utf-8");
		out = response.getWriter();
		Restaurant restaurant = new Restaurant();
		restaurant.setId(Integer.parseInt(id));
		restaurant.setRestaurantName(restaurant_name);
		restaurant.setRestaurantInfo(restaurant_info);
		restaurant.setAddress(address);
		restaurant.setTele1(tele1);
		restaurant.setTele2(tele2);
		boolean success = RestaurantDao.update(Integer.parseInt(id), restaurant);
		JSONObject all = new JSONObject();
		JSONObject msg = new JSONObject();
		if(success){//操作成功
			msg.put("success", success);
			msg.put("message", "操作成功!");
		}
		else{//操作失败
			msg.put("success", success);
			msg.put("message", "操作失败!");
		}
		all.put("all", msg);
		out.write(all.toString());
		out.flush();
		out.close();
		return null;
	}
}
