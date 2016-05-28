package com.wireless.Actions.inventoryMgr.monthlySettle;

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

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;

public class QueryCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jobject = new JObject();
		final String restaurantId = (String) request.getAttribute("restaurantID");
		final DateFormat df = new SimpleDateFormat("yyyy-MM");  
		final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
		//会计月份
		Calendar accountingMonth = Calendar.getInstance();
		long monthly = 0;
		try{
			//获取最近月结时间
			monthly = MonthlyBalanceDao.getCurrentMonthTime(staff);
			accountingMonth.setTime(df.parse(df.format(new Date(monthly))));
			if(monthly > 0){
				jobject.initTip(true, (accountingMonth.get(Calendar.MONTH) + 1)+"");
			}else{
				throw new BusinessException(StockError.NOT_MONTHLY_BALANCE);
			}
			
			accountingMonth.setTimeInMillis(monthly);
			
			final String date = accountingMonth.get(Calendar.YEAR) + "-" + (accountingMonth.get(Calendar.MONTH) + 1) + "-" + accountingMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
			final String minDate = accountingMonth.get(Calendar.YEAR) + "-" + (accountingMonth.get(Calendar.MONTH) + 1) + "-01";
			
			/**
			 * 盘点或者月结最后一张单
			 */
			final Date minDay;
			Long stockActionDate = StockActionDao.getStockActionInsertTime(staff);
			if(stockActionDate != null && stockActionDate > 0){
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(stockActionDate));
				c.add(Calendar.DATE, -1);
				minDay = c.getTime();
			}else{
				minDay = null;
			}
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("currentDay", date);
					jm.putString("minDay", minDay != null ? new SimpleDateFormat("yyyy-MM-dd").format(minDay) : minDate);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
				}
			});
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
