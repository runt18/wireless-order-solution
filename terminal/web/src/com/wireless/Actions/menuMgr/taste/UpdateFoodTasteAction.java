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
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.WebParams;

public class UpdateFoodTasteAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
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
					jobject.initTip(true, "操作成功,已修改菜品口味关联方式为<智能关联>");
				}else if(Short.valueOf(nValue) == Food.TasteRef.MANUAL.getVal()){
					FoodTasteDao.updateFoodTaste(Integer.parseInt(foodID), Integer.parseInt(restaurantID), Food.TasteRef.MANUAL, tasteContent);
					jobject.initTip(true, "操作成功,已修改菜品口味关联方式为<人工关联>");
				}else{
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, "操作失败,菜品口味关联方式选择不正确!");
				}
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(true, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		} catch(Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	
}
