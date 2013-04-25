package com.wireless.Actions.inventoryMgr.materialCate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class AddMaterialCateAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		JSONObject jsonObject = new JSONObject();
		try{
			response.setCharacterEncoding("utf-8");
		
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String name = request.getParameter("name");
			String type = request.getParameter("type");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			MaterialCate materialCate = new MaterialCate();
			materialCate.setName(name);
			materialCate.setType(Integer.parseInt(type));
			materialCate.setParentId(0);
			materialCate.setRestaurantId(terminal.restaurantID);
			MaterialCateDao.insert(terminal, materialCate);
			jsonObject.put("success", true);
			jsonObject.put("msg", "成功保存一条记录!");
		}
		catch(Exception e){
			jsonObject.put("success", false);
			jsonObject.put("msg", e.getMessage());
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
