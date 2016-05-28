package com.wireless.Actions.inventoryMgr.monthlySettle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMonthlyBalanceAction extends DispatchAction{
	
	
	/**
	 * 用于判断库单入库的时间
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getCurrentMonthly(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final DateFormat df = new SimpleDateFormat("yyyy-MM"); 
		final JObject jObject = new JObject();
		Calendar accountingMonth = Calendar.getInstance();
		try{
			long monthly = 0;
			/**
			 * 获取会计月份
			 */
			monthly = MonthlyBalanceDao.getCurrentMonthTime(staff);
			accountingMonth.setTime(df.parse(df.format(new Date(monthly))));
			if(monthly > 0){
				jObject.initTip(true, (accountingMonth.get(Calendar.MONTH) + 1)+"");
			}else{
				throw new BusinessException(StockError.NOT_MONTHLY_BALANCE);
			}
			
			accountingMonth.setTimeInMillis(monthly);
			
			final String date = accountingMonth.get(Calendar.YEAR) + "-" + (accountingMonth.get(Calendar.MONTH) + 1) + "-" + accountingMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
			final String minDate = accountingMonth.get(Calendar.YEAR) + "-" + (accountingMonth.get(Calendar.MONTH) + 1) + "-01";
			
			final Date minDay;
			/**
			 * 获取库单最小值
			 */
			Long stockActionDate = StockActionDao.getStockActionInsertTime(staff);
			if(stockActionDate != null && stockActionDate > 0){
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(stockActionDate));
				c.add(Calendar.DATE, -1);
				minDay = c.getTime();
			}else{
				minDay = null;
			}
			
			jObject.setExtra(new Jsonable(){
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
			
		}catch(BusinessException e) {
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	
	/**
	 * 月结操作
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateMonthlyBalance(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final JObject jObject = new JObject();
		
		try{
			MonthlyBalanceDao.insert(staff);
			jObject.initTip(true, "操作成功, 已经月结.");
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally {
			response.getWriter().print(jObject.toString());
		}
		
		return null;

	}
}
