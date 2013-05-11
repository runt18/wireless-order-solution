package com.wireless.Actions.menuMgr.taste;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodTasteDao;
import com.wireless.protocol.Food;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateFoodTasteAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		
		try{
			
			String foodID = request.getParameter("foodID");
			String restaurantID = request.getParameter("restaurantID");
			String nValue = request.getParameter("nValue");
			String oValue = request.getParameter("oValue");
			String tasteContent = request.getParameter("tasteContent");
			
			if(foodID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,获取菜品失败!");
				return null;
			}
			if(restaurantID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,获取餐厅失败!");
				return null;
			}
			if(nValue == null || oValue == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,获取菜品口味关联方式失败!");
				return null;
			}
			if(Short.valueOf(nValue) == Food.TasteRef.SMART.getVal() && nValue.trim().equals(oValue.trim())){
				jobject.initTip(true, "智能关联方式无需修改!");
				return null;
			}
			if(jobject.isSuccess()){
				if(Short.valueOf(nValue) == Food.TasteRef.SMART.getVal()){
					FoodTasteDao.updateFoodTaste(Integer.parseInt(foodID), Integer.parseInt(restaurantID), Food.TasteRef.SMART, tasteContent);
					jobject.initTip(true, "操作成功,已修改菜品口味关联方式为<智能关联>!");
				}else if(Short.valueOf(nValue) == Food.TasteRef.MANUAL.getVal()){
					FoodTasteDao.updateFoodTaste(Integer.parseInt(foodID), Integer.parseInt(restaurantID), Food.TasteRef.MANUAL, tasteContent);
					jobject.initTip(true, "操作成功,已修改菜品口味关联方式为<人工关联>!");
				}else{
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,菜品口味关联方式选择不正确!");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
	
}
