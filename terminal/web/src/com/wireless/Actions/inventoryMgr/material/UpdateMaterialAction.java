package com.wireless.Actions.inventoryMgr.material;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.protocol.Terminal;

public class UpdateMaterialAction extends Action{
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
			String materialID = request.getParameter("materialID");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin),Terminal.MODEL_STAFF);
			dbCon.disconnect();
			String whereCondition = " WHERE "+Material.TableFields.MATERIAL_ID+" = "+materialID;
			List<Material> materials = MaterialDao.select(terminal, whereCondition);
			Material material = materials.get(0);
			String updateWhereCondition = " WHERE ";
			MaterialDao.update(terminal, material, updateWhereCondition);
			jsonObject.put("success", true);
			jsonObject.put("msg", "成功更新一条记录！");
		}
		catch(Exception e){
			jsonObject.put("success", true);
			jsonObject.put("msg", "更新数据失败！");
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
