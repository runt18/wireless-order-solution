package com.wireless.Actions.inventoryMgr.material;

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
import com.wireless.db.inventoryMgr.MaterialRefCateDao;
import com.wireless.pojo.inventoryMgr.MaterialRefCate;
import com.wireless.protocol.Terminal;

public class QueryMaterialAction extends Action{
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
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			MaterialRefCateDao materialRefCateDao = new MaterialRefCateDao();
			List<MaterialRefCate> materialRefCates = materialRefCateDao.selectMaterial(terminal, " LIMIT "+start+","+limit+";");
			JSONArray jsonArray = JSONArray.fromObject(materialRefCates);
			jsonObject.put("allCount", (materialRefCates.size()!=0)?materialRefCates.get(0).getAllCount():0);
			jsonObject.put("all",jsonArray.toString());
		}
		catch(Exception e){
			jsonObject.put("allCount", "0");
			jsonObject.put("all", e.getMessage());
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
