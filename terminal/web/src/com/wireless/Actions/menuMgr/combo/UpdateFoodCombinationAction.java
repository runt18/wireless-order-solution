package com.wireless.Actions.menuMgr.combo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodCombinationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.FoodCombo;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.WebParams;

public class UpdateFoodCombinationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		
		FoodCombo fc = new FoodCombo();
		JObject jobject = new JObject();
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin), Privilege.Code.BASIC);
			
			String restaurantID = request.getParameter("restaurantID");
			String foodID = request.getParameter("foodID");
			String status = request.getParameter("status");
			String comboContent = request.getParameter("comboContent");
			
			if(restaurantID == null || restaurantID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取餐厅信息失败!");
				return null;
			}
			
			if(foodID == null || foodID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取菜品信息失败!");
				return null;
			}
			
			fc.setRestaurantId(Integer.parseInt(restaurantID));
			fc.setParentId(Integer.parseInt(foodID));
			
			FoodCombinationDao.updateFoodCombination(Integer.parseInt(foodID), Integer.parseInt(restaurantID), Short.parseShort(status), comboContent);
			jobject.initTip(true, "操作成功,已修改套菜关联信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
}
