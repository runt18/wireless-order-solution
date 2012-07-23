package com.wireless.Actions.menuMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodMaterialDao;
import com.wireless.pojo.menuMgr.FoodMaterial;
import com.wireless.util.JObject;

public class UpdateFoodMaterialAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("text/json; charset=utf-8");
		
		JObject jobject = new JObject();
		
		try{
			String restaurantId = request.getParameter("restaurantId");
			String foodId = request.getParameter("foodId");
			String materailId = request.getParameter("materailId");
			String consumption = request.getParameter("consumption");
			
			FoodMaterial fm = new FoodMaterial();
			fm.setRestaurantId(Long.valueOf(restaurantId.trim()));
			fm.setFoodId(Long.valueOf(foodId.trim()));
			fm.setMaterialId(Long.valueOf(materailId.trim()));
			fm.setConsumption(Float.valueOf(consumption.trim()));
			
			FoodMaterialDao.updateFoodMaterial(fm);
			
			jobject.setSuccess(true);
			jobject.setMsg("消耗修改成功!");
			
		} catch(Exception e){
			System.out.println(e.getMessage());
			jobject.setSuccess(false);
			jobject.setMsg("消耗修改失败!");
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
				
		return null;
	}
	
}
