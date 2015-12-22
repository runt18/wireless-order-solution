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
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class UpdateMenuAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject();
		try {
			
			String pin = (String)request.getAttribute("pin");
			
			String foodID = request.getParameter("foodID");
			String foodName = request.getParameter("foodName");
			String foodPrice = request.getParameter("foodPrice");
			String foodPrices = request.getParameter("foodPrices");
			
			String kitchenID = request.getParameter("kitchenID");
			String foodDesc = request.getParameter("foodDesc");
			String isSpecial = request.getParameter("isSpecial");
			String isRecommend = request.getParameter("isRecommend");
			String isFree = request.getParameter("isFree");
			String isStop = request.getParameter("isStop");
			String isCurrPrice = request.getParameter("isCurrPrice");
			String isHot = request.getParameter("isHot");
			String isWeight = request.getParameter("isWeight");
			String isCommission = request.getParameter("isCommission");
			String isLimit = request.getParameter("isLimit");
			String limitCount = request.getParameter("limitCount");
			
			String commission = request.getParameter("commission");
			String foodAliasId = request.getParameter("foodAliasID");
			String foodImage = request.getParameter("foodImage");
			String multiFoodPrices = request.getParameter("multiFoodPrices");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Food.UpdateBuilder builder = new Food.UpdateBuilder(Integer.parseInt(foodID))
												 .setName(foodName)
												 .setPrice(Float.parseFloat(foodPrice))
												 .setKitchen(new Kitchen(Integer.parseInt(kitchenID)))
												 .setDesc(foodDesc)
												 .setSpecial(Boolean.parseBoolean(isSpecial))
												 .setRecommend(Boolean.parseBoolean(isRecommend))
												 .setSellOut(Boolean.parseBoolean(isStop))
												 .setGift(Boolean.parseBoolean(isFree))
												 .setCurPrice(Boolean.parseBoolean(isCurrPrice))
												 .setHot(Boolean.parseBoolean(isHot))
												 .setWeigh(Boolean.parseBoolean(isWeight))
												 .setLimit(Boolean.parseBoolean(isLimit), Integer.parseInt(limitCount));
			
			if(foodImage != null && !foodImage.isEmpty()){
				builder.setImage(Integer.parseInt(foodImage));
			}
			
			if(!foodAliasId.isEmpty()){
				builder.setAliasId(Integer.parseInt(foodAliasId));
			}else{
				builder.setAliasId(0);
			}
			
			if(Boolean.parseBoolean(isCommission)){
				builder.setCommission(Float.parseFloat(commission));
			}else{
				builder.setCommission(false);
			}
			
			if(foodPrices != null && !foodPrices.isEmpty()){
				String[] food_prices = foodPrices.split("&");
				for (String p : food_prices) {
					String[] planPrice = p.split(",");
					builder.addPrice(Integer.parseInt(planPrice[0]), Float.parseFloat(planPrice[1]));
				}
			}
			
			
			if(multiFoodPrices != null && !multiFoodPrices.isEmpty()){
				String[] multiPrices = multiFoodPrices.split("&");
				for (String p : multiPrices) {
					String[] unitPrice = p.split(",");
					builder.addUnit(Float.parseFloat(unitPrice[1]), unitPrice[0]);
				}
			}else{
				builder.emptyUnit();
			}
			
			FoodDao.update(staff, builder.setCheckUsed(true));
			
			jobject.initTip(true, "操作成功, 已修改菜品'" + foodName + "'信息.");
			
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
