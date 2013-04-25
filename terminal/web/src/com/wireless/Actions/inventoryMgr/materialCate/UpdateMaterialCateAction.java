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

public class UpdateMaterialCateAction extends Action{
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
			String cateID = request.getParameter("cateID");
			String type = request.getParameter("type");
			String name = request.getParameter("name");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			MaterialCate materialCate = MaterialCateDao.select(terminal, " WHERE "+MaterialCate.TableFields.CATE_ID+" = "+cateID).get(0);
			materialCate.setName(name);
			materialCate.setRestaurantId(terminal.restaurantID);
			materialCate.setType(Integer.parseInt(type));
			MaterialCateDao.update(terminal, materialCate, " WHERE"+MaterialCate.TableFields.CATE_ID+" = "+cateID);
			jsonObject.put("success", true);
			jsonObject.put("msg", "成功更新一条记录！");
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
