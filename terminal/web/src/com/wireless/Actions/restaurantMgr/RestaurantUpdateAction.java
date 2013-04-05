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

public class RestaurantUpdateAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		PrintWriter out = null;
		String restaurant_name = request.getParameter("restaurant_name");
		String restaurant_info = request.getParameter("restaurant_info");
		String address = request.getParameter("address");
		String tel1 = request.getParameter("tel1");
		String tel2 = request.getParameter("tel2");
		String pin = request.getParameter("pin");
		String id = request.getParameter("restaurantID"); 
		// 解决后台中文传到前台乱码
		response.setContentType("text/json; charset=utf-8");
		out = response.getWriter();
		dbCon.connect();
		String sql = "UPDATE restaurant SET restaurant.restaurant_info = '"+restaurant_info+"',restaurant.restaurant_name='"+restaurant_name+"',address='"+address+"',restaurant.tele1='"+tel1+"',restaurant.tele2='"+tel2+"' WHERE restaurant.id = "+id+"";
		dbCon.stmt.executeUpdate(sql);
		dbCon.disconnect();
		JSONObject all = new JSONObject();
		JSONObject msg = new JSONObject();
		msg.put("success", true);
		msg.put("message", "操作成功!");
		all.put("all", msg);
		out.write(all.toString());
		out.flush();
		out.close();
		return null;
	}
}
