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
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.protocol.Terminal;

public class UpdateTasteAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
	
		try{
			response.setContentType("text/json; charset=utf-8");
			PrintWriter out = null;
			out = response.getWriter();
			String restaurantID = request.getParameter("restaurantID");//餐厅ID
			String pin = request.getParameter("pin");//获取pin值
			String modTastes = request.getParameter("modTastes");
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			restaurantID = term.restaurantID+"";
			dbCon.disconnect();
			boolean success = TasteRefDao.update(term,modTastes);
			JSONObject json = new JSONObject();
			if(success){
				json.put("success", success);
				json.put("message","操作成功!");
			}
			else{
				json.put("success", success);
				json.put("message","操作失败!");
			}
			out.write(json.toString());
			out.flush();
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
