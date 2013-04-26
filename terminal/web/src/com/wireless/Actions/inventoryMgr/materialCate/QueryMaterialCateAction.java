package com.wireless.Actions.inventoryMgr.materialCate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.protocol.Terminal;

public class QueryMaterialCateAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		JSONObject jsonObject = new JSONObject();
		try{
			response.setContentType("text/json; charset=utf-8");
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
		    List<MaterialCate> materialCates = MaterialCateDao.select(terminal, "");
		    JSONArray jsonArray = JSONArray.fromObject(materialCates);
		    jsonObject.put("allCount", materialCates.size());
		    jsonObject.put("all", jsonArray.toString());
		}
		catch(Exception e){
			jsonObject.put("allCount", "0");
			jsonObject.put("all", "[]");
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
