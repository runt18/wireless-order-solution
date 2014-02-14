package com.wireless.Actions.menuMgr.taste;

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

public class DeleteFoodTasteAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String foodID = request.getParameter("foodID");
		String restaurantID = request.getParameter("restaurantID");
		String tasteID = request.getParameter("tasteID");
		
		JObject jobject = new JObject();
		FoodTaste ft = new FoodTaste();
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			
			if(foodID == null || restaurantID == null || tasteID == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,口味信息不完整!");
			}
			if(jobject.isSuccess()){
				ft.getFood().setFoodId(Integer.valueOf(foodID));
				ft.getFood().setRestaurantId(Integer.valueOf(restaurantID));
				ft.getTaste().setTasteId(Integer.valueOf(tasteID));
				int count = FoodTasteDao.deleteFoodTaste(ft);
				if(count == 0){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,未找到要删除的关联口味信息!");
				}else{
					jobject.initTip(true, "操作成功,已删除关联口味!");
				}
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(true, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		} catch(Exception e) {
			e.printStackTrace();
			jobject.initTip(true, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败,删除关联口味时发生异常!");
		} finally {
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	
}
