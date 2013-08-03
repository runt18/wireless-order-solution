package com.wireless.Actions.menuMgr.basic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class InsertMenuAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		Food fb = new Food();
				
		try {
			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal dishNumber: dishName: dishSpill:
			 * dishPrice: kitchen: isSpecial : isRecommend : isFree : isStop :
			 * 
			 */
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			
			String foodAliasID = request.getParameter("foodAliasID");
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
			String comboContent = request.getParameter("comboContent");
			String isHot = request.getParameter("isHot");
			String isWeight = request.getParameter("isWeight");
			String stockStatus = request.getParameter("stockStatus");
			
			if(pin == null || restaurantID == null || pin.trim().length() == 0 || restaurantID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取餐厅编号失败.");
				return null;
			}
			
			if((foodAliasID == null || foodAliasID.trim().length() == 0)
					|| (foodName == null || foodName.trim().length() == 0)
					|| (foodPrice == null || foodPrice.trim().length() == 0)){
				jobject.initTip(false, "操作失败,获取食品基础信息失败.");
				return null;
			}
				
			if(jobject.isSuccess()){
				Staff staff = StaffDao.verify(Integer.parseInt(pin));
				
				fb.setRestaurantId(Integer.parseInt(restaurantID));
				fb.setAliasId(Integer.parseInt(foodAliasID));
				fb.setName(foodName);
				fb.setPinyin(foodPinyin);
				fb.setPrice(Float.parseFloat(foodPrice));
				fb.getKitchen().setAliasId(Short.parseShort(kitchenAliasID));
				fb.getKitchen().setId(Integer.parseInt(kitchenID));
//				fb.setStatus(status);
				fb.setDesc(foodDesc);
				fb.setStockStatus(Integer.valueOf(stockStatus));
				
				fb.setSpecial(Boolean.valueOf(isSpecial));
				fb.setRecommend(Boolean.valueOf(isRecommend));
				fb.setSellOut(Boolean.valueOf(isStop));
				fb.setGift(Boolean.valueOf(isFree));
				fb.setCurPrice(Boolean.valueOf(isCurrPrice));
				fb.setCombo(Boolean.valueOf(isCombination));
				fb.setHot(Boolean.valueOf(isHot));
				fb.setWeigh(Boolean.valueOf(isWeight));
				
				if (isCombination != null && isCombination.equals("true")) {
					FoodDao.insertFoodBaisc(staff, fb, comboContent);
				}else{
					FoodDao.insertFoodBaisc(staff, fb);
				}
				
				jobject.initTip(true, "操作成功,已添加新菜品.");
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			response.getWriter().write(jobject.toString());
		}
		return null;
	}
	
}
