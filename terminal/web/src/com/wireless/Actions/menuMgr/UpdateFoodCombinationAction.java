package com.wireless.Actions.menuMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodCombinationDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodCombination;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateFoodCombinationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		
		FoodCombination fc = new FoodCombination();
		JObject jobject = new JObject();
		
		try{
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
			
			fc.setRestaurantID(Integer.parseInt(restaurantID));
			fc.setParentFoodID(Integer.parseInt(foodID));
			
			FoodCombinationDao.updateFoodCombination(Integer.parseInt(foodID), Integer.parseInt(restaurantID), Byte.parseByte(status), comboContent);
			
			jobject.initTip(true, "操作成功,已修改套菜关联信息.");
			
		}catch(BusinessException e){
			jobject.initTip(false, e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
}
