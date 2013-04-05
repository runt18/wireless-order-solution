package com.wireless.Actions.restaurantMgr;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;

public class RestaurantQueryByIDAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		String id = request.getParameter("restaurantID");
		String pin = request.getParameter("pin");//获取pin值
		DBCon dbCon = new DBCon();
		dbCon.connect();
		Statement stmt = dbCon.stmt;
		String sql = "SELECT * FROM restaurant WHERE restaurant.id = "+id+";";
		ResultSet rs = stmt.executeQuery(sql);
		JSONObject all = new JSONObject();
		JSONObject msg = new JSONObject();
		boolean success = false;
		while(rs.next()){
			success = true;
			String restaurant_name = rs.getString("restaurant_name");
			String restaurant_info = rs.getString("restaurant_info");
			String address = rs.getString("address");
			String tele1 = rs.getString("tele1");
			String tele2 = rs.getString("tele2");
			
			msg.put("restaurant_name", restaurant_name);
			msg.put("restaurant_info", restaurant_info);
			msg.put("address", address);
			msg.put("tele1", tele1);
			msg.put("tele2", tele2);
			break;
		}
		msg.put("success", success);
		all.put("all", msg.toString());
		rs.close();
		stmt.close();
		dbCon.disconnect();
		out.write(all.toString());
		return null;
	}
}
