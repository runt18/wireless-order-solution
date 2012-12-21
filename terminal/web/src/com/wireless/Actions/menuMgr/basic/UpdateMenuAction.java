package com.wireless.Actions.menuMgr.basic;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodBasicDao;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateMenuAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		FoodBasic fb = new FoodBasic();
		JObject jobject = new JObject();
		try {
			
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			
			String foodID = request.getParameter("foodID");
			String foodName = request.getParameter("foodName");
			String foodPinyin = request.getParameter("foodPinyin");
			String foodPrice = request.getParameter("foodPrice");
			
			String kitchenAliasID = request.getParameter("kitchenAliasID");
			String kitchenID = request.getParameter("kitchenID");
			String foodDesc = request.getParameter("foodDesc");
			String isSpecial = request.getParameter("isSpecial");
			String isRecommend = request.getParameter("isRecommend");
			String isFree = request.getParameter("isFree");
			String isStop = request.getParameter("isStop");
			String isCurrPrice = request.getParameter("isCurrPrice");
			String isCombination = request.getParameter("isCombination");
			String isHot = request.getParameter("isHot");
			
			if(pin == null || restaurantID == null || pin.trim().length() == 0 || restaurantID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取餐厅编号失败.");
				return null;
			}
			
			if((foodName == null || foodName.trim().length() == 0)
					|| (foodPrice == null || foodPrice.trim().length() == 0)){
				jobject.initTip(false, "操作失败,获取食品基础信息失败.");
				return null;
			}
			
			fb.setFoodID(Integer.parseInt(foodID));
			fb.setRestaurantID(Integer.parseInt(restaurantID));
			fb.setFoodName(foodName);
			fb.setPinyin(foodPinyin);
			fb.setUnitPrice(Float.parseFloat(foodPrice));
			fb.getKitchen().setKitchenAliasID(Integer.parseInt(kitchenAliasID));
			fb.getKitchen().setKitchenID(Integer.parseInt(kitchenID));
//			fb.setStatus(status);
			fb.setDesc(foodDesc);	
			
			fb.setSpecial(isSpecial);
			fb.setRecommend(isRecommend);
			fb.setStop(isStop);
			fb.setGift(isFree);
			fb.setCurrPrice(isCurrPrice);
			fb.setCombination(isCombination);
			fb.setHot(isHot);
			
			FoodBasicDao.updateFoodBaisc(fb);
			
			jobject.initTip(true, "操作成功,已修改菜品信息.");
			
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}

		return null;
	}

}
