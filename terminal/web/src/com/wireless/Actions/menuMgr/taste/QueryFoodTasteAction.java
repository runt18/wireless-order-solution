package com.wireless.Actions.menuMgr.taste;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodTasteDao;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryFoodTasteAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String foodID = request.getParameter("foodID");
		String restaurantID = request.getParameter("restaurantID");
		FoodTaste query = new FoodTaste();
		JObject jobject = new JObject();
		FoodTaste[] list = null;
		
		try{
			response.setContentType("text/json; charset=utf-8");
			if(foodID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,没有指定查询口味的菜品!");
			}
			
			if(restaurantID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,没有指定查询口味的餐厅!");
			}
			
			if(jobject.isSuccess()){
				query.setFoodId(Integer.valueOf(foodID));
				query.setRestaurantId(Integer.valueOf(restaurantID));
				list = FoodTasteDao.getFoodCommonTaste(query);
			}
			
		} catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		} finally{
			if(list != null){
				jobject.setTotalProperty(list.length);
				jobject.setRoot(Arrays.asList(list));
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
	
}
