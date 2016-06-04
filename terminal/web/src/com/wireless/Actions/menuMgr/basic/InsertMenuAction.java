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

public class InsertMenuAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		
		final String foodAliasId = request.getParameter("foodAliasID");
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
		
		final String split = request.getParameter("split");
		final String commission = request.getParameter("commission");
		final String multiFoodPrices = request.getParameter("multiFoodPrices");
		
		final JObject jObject = new JObject();
				
		try {
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final Food.InsertBuilder builder = new Food.InsertBuilder(foodName, Float.parseFloat(foodPrice), new Kitchen(Integer.parseInt(kitchenId)));
			
			//是否特价
			if(isSpecial != null && !isSpecial.isEmpty()){
				builder.setSpecial(Boolean.parseBoolean(isSpecial));
			}
			
			//是否推荐
			if(isRecommend != null && !isRecommend.isEmpty()){
				builder.setRecommend(Boolean.parseBoolean(isRecommend));
			}
			
			//是否数量不累加
			if(split != null && !split.isEmpty() && Boolean.parseBoolean(split)){
				builder.setSplit(true);
			}else{
				builder.setSplit(false);
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
			
			//简介
			if(foodDesc != null && !foodDesc.isEmpty()){
				builder.setDesc(foodDesc);
			}
			
			//打印厨房
			if(printKitchenId != null && !printKitchenId.isEmpty()){
				builder.setPrintKitchen(Integer.parseInt(printKitchenId));
			}
			
			//助记码
			if(foodAliasId != null && !foodAliasId.isEmpty()){
				builder.setAliasId(Integer.parseInt(foodAliasId));
			}
			
			if(isCommission != null && isCommission.isEmpty() && Boolean.valueOf(isCommission)){
				builder.setCommission(Float.parseFloat(commission));
			}
			
			if(isLimit != null && !isLimit.isEmpty() && Boolean.parseBoolean(isLimit)){
				builder.setLimit(Boolean.parseBoolean(isLimit), Integer.parseInt(limitCount));
			}else{
				builder.setLimit(false, 0);
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
			}
			
			FoodDao.insert(staff, builder);
			
			jObject.initTip(true, "操作成功, 添加新菜品'" + foodName + "'信息");
			
		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally {
			response.getWriter().write(jObject.toString());
		}
		return null;
	}
	
}
