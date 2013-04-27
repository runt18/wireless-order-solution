package com.wireless.Actions.inventoryMgr.material;

import java.util.Date;

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

public class AddMaterialAction extends Action{
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
			String cateId = request.getParameter("cateId");
			String name = request.getParameter("name");
			String price = request.getParameter("price");
			String amount = request.getParameter("amount");
			String status = request.getParameter("status");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			Material material = new Material();
			material.setCateId(Integer.parseInt(cateId));
			material.setLastModDate(new Date());
			material.setLastModStaff(terminal.owner);
			material.setMaterialId(0);
			material.setName(name);
			material.setAmount(Float.parseFloat(amount));
			material.setPrice(Float.parseFloat(price));
			material.setStatus(Integer.parseInt(status));
			MaterialDao.insert(terminal, material);
			jsonObject.put("success", true);
			jsonObject.put("msg", "成功保存一条记录!");
		}
		catch(Exception e){
			jsonObject.put("success", false);
			jsonObject.put("msg", "保存数据失败!");
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
