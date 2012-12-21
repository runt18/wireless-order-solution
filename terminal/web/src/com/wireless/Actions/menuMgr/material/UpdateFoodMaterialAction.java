package com.wireless.Actions.menuMgr.material;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodMaterialDao;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateFoodMaterialAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("text/json; charset=utf-8");
		
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String foodID = request.getParameter("foodID");
			String materailContent = request.getParameter("materailContent");					
			
			if(restaurantID == null || restaurantID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取餐厅信息失败!");
				return null;
			}
			if(foodID == null || foodID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取需要修改的菜品编号失败!");
				return null;
			}
			
			FoodMaterialDao.updateFoodMaterial(Integer.parseInt(foodID), Integer.parseInt(restaurantID), materailContent);
			
			jobject.initTip(true, "操作成功,已修改菜品食材关联信息!");
			
		} catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
				
		return null;
	}
	
}
