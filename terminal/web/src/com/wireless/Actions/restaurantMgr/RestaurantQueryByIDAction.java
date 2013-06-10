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
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.Terminal;

public class RestaurantQueryByIDAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		
		response.setContentType("text/json; charset=utf-8");
		
		PrintWriter out = response.getWriter();
		
		String pin = request.getParameter("pin");
		
		JSONObject all = new JSONObject();
		
		JSONObject msg = new JSONObject();
		
		boolean success = false;
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			
			Restaurant restaurant = RestaurantDao.getById(term);
			
			if(restaurant != null){
				
				success = true;	
				
				msg.put("restaurant_name", restaurant.getName());
				
				msg.put("restaurant_info", restaurant.getInfo());
				
				msg.put("address", restaurant.getAddress());
				
				msg.put("tele1", restaurant.getTele1());
				
				msg.put("tele2", restaurant.getTele2());
				
			}
			
			msg.put("success", success);
		
		}
		catch(BusinessException e){
			
			e.printStackTrace();
			
			success = false;
			
			msg.put("success", success);
			
			msg.put("message", e.getDesc());
			
		}
		finally{
			
			dbCon.disconnect();
			
			all.put("all", msg.toString());
			
			out.write(all.toString());
			
			out.flush();
			
			out.close();
			
		}
		
		return null;
	}
}
