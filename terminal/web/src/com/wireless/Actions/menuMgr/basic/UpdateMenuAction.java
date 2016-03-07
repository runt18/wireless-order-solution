package com.wireless.Actions.menuMgr.basic;

import java.sql.SQLException;

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
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		
		final String foodId = request.getParameter("foodID");
		final String foodName = request.getParameter("foodName");
		final String foodPrice = request.getParameter("foodPrice");
		final String foodPrices = request.getParameter("foodPrices");
		
		final String kitchenId = request.getParameter("kitchenID");
		final String printKitchenId = request.getParameter("printKitchenId");
		final String foodDesc = request.getParameter("foodDesc");
		final String isSpecial = request.getParameter("isSpecial");
		final String isRecommend = request.getParameter("isRecommend");
		final String isGift = request.getParameter("isFree");
		final String isSellout = request.getParameter("isStop");
		final String isCurrPrice = request.getParameter("isCurrPrice");
		final String isHot = request.getParameter("isHot");
		final String isWeight = request.getParameter("isWeight");
		final String isCommission = request.getParameter("isCommission");
		final String isLimit = request.getParameter("isLimit");
		final String limitCount = request.getParameter("limitCount");
		
		final String commission = request.getParameter("commission");
		final String foodAliasId = request.getParameter("foodAliasID");
		final String foodImage = request.getParameter("foodImage");
		final String multiFoodPrices = request.getParameter("multiFoodPrices");
		
		final JObject jObject = new JObject();
		try {
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final Food.UpdateBuilder builder = new Food.UpdateBuilder(Integer.parseInt(foodId));
			
			if(foodName != null && !foodName.isEmpty()){
				builder.setName(foodName);
			}
			
			//价钱
			if(foodPrice != null && !foodPrice.isEmpty()){
				builder.setPrice(Float.parseFloat(foodPrice));
			}
			
			//所属厨房
			if(kitchenId != null && !kitchenId.isEmpty()){
				builder.setKitchen(new Kitchen(Integer.parseInt(kitchenId)));
			}
			
			//打印厨房
			if(printKitchenId != null && !printKitchenId.isEmpty()){
				builder.setPrintKitchen(Integer.parseInt(printKitchenId));
			}
			
			//简介
			if(foodDesc != null && !foodDesc.isEmpty()){
				builder.setDesc(foodDesc);
			}
			
			//是否特价
			if(isSpecial != null && !isSpecial.isEmpty()){
				builder.setSpecial(Boolean.parseBoolean(isSpecial));
			}
			
			//是否推荐
			if(isRecommend != null && !isRecommend.isEmpty()){
				builder.setRecommend(Boolean.parseBoolean(isRecommend));
			}
			
			//是否停售
			if(isSellout != null && !isSellout.isEmpty()){
				builder.setSellOut(Boolean.parseBoolean(isSellout));
			}
			
			//是否赠送
			if(isGift != null && !isGift.isEmpty()){
				builder.setGift(Boolean.parseBoolean(isGift));
			}
			
			//是否时价
			if(isCurrPrice != null && !isCurrPrice.isEmpty()){
				builder.setCurPrice(Boolean.parseBoolean(isCurrPrice));
			}
			
			//是否热销
			if(isHot != null && !isHot.isEmpty()){
				builder.setHot(Boolean.parseBoolean(isHot));
			}
			
			//是否称重
			if(isWeight != null && !isWeight.isEmpty()){
				builder.setWeigh(Boolean.parseBoolean(isWeight));
			}
			
			//是否限量估清
			if(isLimit != null && !isLimit.isEmpty() && Boolean.parseBoolean(isLimit)){
				builder.setLimit(true, Integer.parseInt(limitCount));
			}else{
				builder.setLimit(false, 0);
			}
			
			//菜品图片
			if(foodImage != null && !foodImage.isEmpty()){
				builder.setImage(Integer.parseInt(foodImage));
			}
			
			//助记码
			if(foodAliasId != null && !foodAliasId.isEmpty()){
				builder.setAliasId(Integer.parseInt(foodAliasId));
			}else{
				builder.setAliasId(0);
			}
			
			//是否提成
			if(Boolean.parseBoolean(isCommission)){
				builder.setCommission(Float.parseFloat(commission));
			}else{
				builder.setCommission(false);
			}
			
			//价格方案
			if(foodPrices != null && !foodPrices.isEmpty()){
				String[] food_prices = foodPrices.split("&");
				for (String p : food_prices) {
					String[] planPrice = p.split(",");
					builder.addPrice(Integer.parseInt(planPrice[0]), Float.parseFloat(planPrice[1]));
				}
			}
			
			//多单位
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
			
			jObject.initTip(true, "操作成功, 已修改菜品'" + foodName + "'信息.");
			
		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
			
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}

		return null;
	}

}
