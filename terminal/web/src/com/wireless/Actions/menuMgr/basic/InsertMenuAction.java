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
import com.wireless.util.WebParams;

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
			String commission = request.getParameter("commission");
			String stockStatus = request.getParameter("stockStatus");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Food.InsertBuilder builder = new Food.InsertBuilder(foodName, Float.parseFloat(foodPrice), new Kitchen(Integer.parseInt(kitchenId)))
												 .setAliasId(Integer.parseInt(foodAliasId))
												 .setDesc(foodDesc)
												 .setStockStatus(Food.StockStatus.valueOf(Integer.valueOf(stockStatus)))
												 .setSpecial(Boolean.valueOf(isSpecial))
												 .setRecommend(Boolean.valueOf(isRecommend))
												 .setSellOut(Boolean.valueOf(isSellout))
												 .setGift(Boolean.valueOf(isGift))
												 .setCurPrice(Boolean.valueOf(isCurrPrice))
												 .setHot(Boolean.valueOf(isHot))
												 .setWeigh(Boolean.valueOf(isWeight));
			if(Boolean.valueOf(isCommission)){
				builder.setCommission(Float.parseFloat(commission));
			}
			
			FoodDao.insert(staff, builder);
			
			jobject.initTip(true, "操作成功, 添加新菜品'" + foodName + "'信息");
			
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
