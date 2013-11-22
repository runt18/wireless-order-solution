package com.wireless.Actions.inventoryMgr.monthlySettle;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.stockMgr.MonthlyBalance;

public class QueryCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		String restaurantID = (String) request.getAttribute("restaurantID");
		Map<Object, Object> map = new HashMap<Object, Object>();
		Calendar c = Calendar.getInstance();
		int month;
		try{
			MonthlyBalance monthly = MonthlyBalanceDao.getCurrentMonthByRestaurant(Integer.parseInt(restaurantID));
			if(monthly != null){
				c.setTime(new Date(monthly.getMonth()));
				month = (c.get(Calendar.MONTH)+1);
			}else{
				Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(restaurantID));
				c.setTime(new Date(restaurant.getBirthDate()));
				month = (c.get(Calendar.MONTH)+1);
			}
			map.put("month", month);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setOther(map);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
