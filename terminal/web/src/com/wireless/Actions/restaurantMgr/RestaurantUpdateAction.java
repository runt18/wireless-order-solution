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
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;

public class RestaurantUpdateAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
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
			
			String pin = (String) request.getSession().getAttribute("pin");
			
			String id = request.getParameter("restaurantID"); 
			
			dbCon.connect();

			Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.SYSTEM);
			
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
