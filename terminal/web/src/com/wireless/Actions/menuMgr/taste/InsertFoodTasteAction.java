package com.wireless.Actions.menuMgr.taste;

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

public class InsertFoodTasteAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String foodID = request.getParameter("foodID");
		String restaurantID = request.getParameter("restaurantID");
		String tasteID = request.getParameter("tasteID");
		
		JObject jobject = new JObject();
		FoodTaste ft = new FoodTaste();
		
		try{
			response.setContentType("text/json; charset=utf-8");
			if(foodID == null || restaurantID == null || tasteID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,口味信息不完整!");
			}
			if(jobject.isSuccess()){
				ft.setFoodId(Integer.valueOf(foodID));
				ft.setRestaurantId(Integer.valueOf(restaurantID));
				ft.setTasteID(Integer.valueOf(tasteID));
				int count = FoodTasteDao.insertFoodTaste(ft);
				if(count == 0){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,未知错误!");
				}else{
					jobject.initTip(true, "操作成功,已关联口味!");
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
