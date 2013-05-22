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

public class DeleteFoodTasteAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String foodID = request.getParameter("foodID");
		String restaurantID = request.getParameter("restaurantID");
		String tasteID = request.getParameter("tasteID");
		
		JObject jboject = new JObject();
		FoodTaste ft = new FoodTaste();
		
		try{
			response.setContentType("text/json; charset=utf-8");
			if(foodID == null || restaurantID == null || tasteID == null){
				jboject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,口味信息不完整!");
			}
			if(jboject.isSuccess()){
				ft.getFood().setFoodId(Integer.valueOf(foodID));
				ft.getFood().setRestaurantId(Integer.valueOf(restaurantID));
				ft.getTaste().setTasteId(Integer.valueOf(tasteID));
				int count = FoodTasteDao.deleteFoodTaste(ft);
				if(count == 0){
					jboject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,未找到要删除的关联口味信息!");
				}else{
					jboject.initTip(true, "操作成功,已删除关联口味!");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			jboject.initTip(true, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败,删除关联口味时发生异常!");
		} finally {
			JSONObject json = JSONObject.fromObject(jboject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
	
}
