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

public class RestaurantQueryByIDAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		
		response.setContentType("text/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		String id = request.getParameter("restaurantID");
		String pin = request.getParameter("pin");//保留该参数，Verify，Terminal以后会用到
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			id = term.restaurantID+"";
			dbCon.disconnect();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//com.wireless.protocol.Terminal terminal = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
		JSONObject all = new JSONObject();
		JSONObject msg = new JSONObject();
		Restaurant restaurant = RestaurantDao.queryByID(Integer.parseInt(id));
		boolean success = false;
		if(restaurant != null){
			success = true;	
			msg.put("restaurant_name", restaurant.getRestaurantName());
			msg.put("restaurant_info", restaurant.getRestaurantInfo());
			msg.put("address", restaurant.getAddress());
			msg.put("tele1", restaurant.getTele1());
			msg.put("tele2", restaurant.getTele2());
		}
		msg.put("success", success);
		all.put("all", msg.toString());
		out.write(all.toString());
		return null;
	}
}
