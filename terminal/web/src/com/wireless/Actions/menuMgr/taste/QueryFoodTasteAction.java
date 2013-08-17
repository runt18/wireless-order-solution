package com.wireless.Actions.menuMgr.taste;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodTasteDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.util.WebParams;

public class QueryFoodTasteAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String foodID = request.getParameter("foodID");
		String restaurantID = request.getParameter("restaurantID");
		JObject jobject = new JObject();
		List<FoodTaste> list = null;
		
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			response.setContentType("text/json; charset=utf-8");
			if(foodID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,没有指定查询口味的菜品!");
			}
			
			if(restaurantID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,没有指定查询口味的餐厅!");
			}
			
			if(jobject.isSuccess()){
				list = FoodTasteDao.getFoodCommonTaste(Integer.valueOf(restaurantID), Integer.valueOf(foodID));
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(true, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		} catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		} finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(list);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
