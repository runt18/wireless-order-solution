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
		try{
			//输出JSON数据
			response.setContentType("text/json; charset=utf-8");
			PrintWriter out = response.getWriter();
			String id = request.getParameter("restaurantID");
			String pin = request.getParameter("pin");
			JSONObject all = new JSONObject();
			JSONObject msg = new JSONObject();
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			id = term.restaurantID+"";
			dbCon.disconnect();
			Restaurant restaurant = RestaurantDao.queryByID(term);
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
			out.flush();
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
