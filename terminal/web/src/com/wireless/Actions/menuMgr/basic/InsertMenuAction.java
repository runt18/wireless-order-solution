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

public class InsertMenuAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject();
				
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
			String pin = (String)request.getAttribute("pin");
			
			String foodAliasId = request.getParameter("foodAliasID");
			String foodName = request.getParameter("foodName");
			String foodPrice = request.getParameter("foodPrice");
			String foodPrices = request.getParameter("foodPrices");
			
			String kitchenId = request.getParameter("kitchenID");
			String foodDesc = request.getParameter("foodDesc");
			String isSpecial = request.getParameter("isSpecial");
			String isRecommend = request.getParameter("isRecommend");
			String isGift = request.getParameter("isFree");
			String isSellout = request.getParameter("isStop");
			String isCurrPrice = request.getParameter("isCurrPrice");
			String isHot = request.getParameter("isHot");
			String isWeight = request.getParameter("isWeight");
			String isCommission = request.getParameter("isCommission");
			String isLimit = request.getParameter("isLimit");
			String limitCount = request.getParameter("limitCount");			
			
			String commission = request.getParameter("commission");
			String multiFoodPrices = request.getParameter("multiFoodPrices");
//			String stockStatus = request.getParameter("stockStatus");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Food.InsertBuilder builder = new Food.InsertBuilder(foodName, Float.parseFloat(foodPrice), new Kitchen(Integer.parseInt(kitchenId)))
												 .setDesc(foodDesc)
//												 .setStockStatus(Food.StockStatus.valueOf(Integer.valueOf(stockStatus)))
												 .setSpecial(Boolean.valueOf(isSpecial))
												 .setRecommend(Boolean.valueOf(isRecommend))
												 .setSellOut(Boolean.valueOf(isSellout))
												 .setGift(Boolean.valueOf(isGift))
												 .setCurPrice(Boolean.valueOf(isCurrPrice))
												 .setHot(Boolean.valueOf(isHot))
												 .setWeigh(Boolean.valueOf(isWeight));
			
			if(foodAliasId != null && !foodAliasId.isEmpty()){
				builder.setAliasId(Integer.parseInt(foodAliasId));
			}
			
			if(Boolean.valueOf(isCommission)){
				builder.setCommission(Float.parseFloat(commission));
			}
			
			if(Boolean.parseBoolean(isLimit)){
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
			
			jobject.initTip(true, "操作成功, 添加新菜品'" + foodName + "'信息");
			
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().write(jobject.toString());
		}
		return null;
	}
	
}
