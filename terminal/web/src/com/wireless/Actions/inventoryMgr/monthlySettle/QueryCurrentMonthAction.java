package com.wireless.Actions.inventoryMgr.monthlySettle;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
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
		int m = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM");  
		
		Calendar presentMonth = Calendar.getInstance();
		Calendar accountMonth = Calendar.getInstance();
		
		presentMonth.setTime(df.parse(df.format(new Date())));
		
		try{
			MonthlyBalance monthly = MonthlyBalanceDao.getCurrentMonthByRestaurant(Integer.parseInt(restaurantID));
			if(monthly.getId() > 0){
				accountMonth.setTime(df.parse(df.format(new Date(monthly.getMonth()))));
				while(presentMonth.after(accountMonth)){
					m ++;
					presentMonth.add(Calendar.MONTH, -1);
				}
				if(m >= 2){
					jobject.initTip(true, (accountMonth.get(Calendar.MONTH)+2)+"");
				}else{
					throw new BusinessException(StockError.NOT_MONTHLY_BALANCE);
				}
			}else{
				Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(restaurantID));
				accountMonth.setTime(df.parse(df.format(new Date(restaurant.getBirthDate()))));
				while(presentMonth.after(accountMonth)){
					m ++;
					presentMonth.add(Calendar.MONTH, -1);
				}
				if(m >= 1){
					jobject.initTip(true, (accountMonth.get(Calendar.MONTH)+1)+"");
				}else{
					throw new BusinessException(StockError.NOT_MONTHLY_BALANCE);
				}
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
