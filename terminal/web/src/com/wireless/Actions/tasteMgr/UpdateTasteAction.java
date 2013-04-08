package com.wireless.Actions.tasteMgr;

import java.io.PrintWriter;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.tasteRef.TasteRefDao;

public class UpdateTasteAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String restaurantID = request.getParameter("restaurantID");//餐厅ID
		String pin = request.getParameter("pin");//获取pin值
		PrintWriter out = null;
		response.setContentType("text/json; charset=utf-8");
		out = response.getWriter();
		String modTastes = request.getParameter("modTastes");
		TasteRefDao.update(modTastes);
		JSONObject json = new JSONObject();
		json.put("success", true);
		json.put("message","操作成功!");
		out.write(json.toString());
		out.flush();
		out.close();
		return null;
	}

}
