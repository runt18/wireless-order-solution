package com.wireless.Actions.inventoryMgr.monthlySettle;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.stockMgr.StockTake;

public class QueryCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		String restaurantID = (String) request.getAttribute("restaurantID");
		int m = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM");  
		
		Calendar presentMonth = Calendar.getInstance();
		Calendar accountMonth = Calendar.getInstance();
		
		presentMonth.setTime(df.parse(df.format(new Date())));
		DBCon dbCon = new DBCon();
		long monthly = 0;
		try{
			//获取最近月结时间
			monthly = MonthlyBalanceDao.getCurrentMonthTimeByRestaurant(Integer.parseInt(restaurantID));
			accountMonth.setTime(df.parse(df.format(new Date(monthly))));
			while(presentMonth.after(accountMonth)){
				m ++;
				presentMonth.add(Calendar.MONTH, -1);
			}
			if(m >= 1){
				jobject.initTip(true, (accountMonth.get(Calendar.MONTH)+1)+"");
			}else{
				throw new BusinessException(StockError.NOT_MONTHLY_BALANCE);
			}

		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			accountMonth.setTimeInMillis(monthly);
			final String date = accountMonth.get(Calendar.YEAR) + "-" + (accountMonth.get(Calendar.MONTH) + 1) + "-" + accountMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			
			dbCon.connect();
			String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + restaurantID + 
					" UNION ALL " +
					" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + restaurantID + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
			dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
			final Timestamp minDay;
			if(dbCon.rs.next()){
				if(dbCon.rs.getTimestamp("date") != null){
					minDay = dbCon.rs.getTimestamp("date");
				}else{
					minDay = null;
				}
			}else{
				minDay = null;
			}
			jobject.setExtra(new Jsonable(){

				@Override
				public Map<String, Object> toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("currentDay", date);
					jm.putString("minDay", minDay != null ? new SimpleDateFormat("yyyy-MM-dd").format(minDay) : null);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
