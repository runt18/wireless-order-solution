package com.wireless.Actions.menuMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodTasteDao;
import com.wireless.db.tasteRef.TasteRef;
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
			String tasteID = request.getParameter("tasteID");
			
			if(foodID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,获取菜品失败!");
			}
			if(restaurantID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,获取餐厅失败!");
			}
			if(nValue == null || oValue == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,获取菜品口味关联方式失败!");
			}
			if(nValue.trim().equals(oValue.trim())){
				jobject.initTip(false, "无需修改!");
			}
			if(jobject.isSuccess()){
				if(Long.valueOf(nValue) == WebParams.TASTE_SMART_REF){
					FoodTasteDao.updataBySmart(Long.valueOf(foodID), Long.valueOf(restaurantID));
					jobject.initTip(true, "操作成功,已修改菜品口味关联方式为<智能关联>!");
				}else if(Long.valueOf(nValue) == WebParams.TASTE_MANUAL_REF){
					FoodTasteDao.updataByManual(Long.valueOf(foodID), tasteID, Long.valueOf(restaurantID));
					jobject.initTip(true, "操作成功,已修改菜品口味关联方式为<人工关联>!");
				}else{
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,菜品口味关联方式选择不正确!");
				}
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败,修改菜品口味关联方式发生异常!");
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
	
}
