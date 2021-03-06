package com.wireless.Actions.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.DataPaging;

public class BusinessReceiptsStatisticsAction extends DispatchAction {
	
	/**
	 * 收款统计
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("dateBegin");
		final String offDuty = request.getParameter("dateEnd");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String region = request.getParameter("region");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String includingChart = request.getParameter("includingChart");
		
		final JObject jObject = new JObject();
		try{

			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)
																	.setDutyRange(new DutyRange(onDuty, offDuty))
																	.setCalcByDuty(true);

			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				} 
			}

			
			if(opening != null && !opening.isEmpty()){
				HourRange hr = new HourRange(opening, ending, Pattern.HOUR);
				extraCond.setHourRange(hr);
			}
			
			if(region != null && !region.isEmpty()){
				extraCond.setRegion(Region.RegionId.valueOf(Integer.parseInt(region)));
			}
			
			List<IncomeByEachDay> incomesByEachDay = CalcBillStatisticsDao.calcIncomeByEachDay(staff, extraCond);

			 jObject.setTotalProperty(incomesByEachDay.size());
			 IncomeByEachDay total = new IncomeByEachDay("2050-01-01");
			 
			 for(IncomeByEachDay item : incomesByEachDay){
				 total.append(item);
			 }
			 
			 if(start != null && !start.isEmpty() && limit != null && ! limit.isEmpty()){
				 incomesByEachDay = DataPaging.getPagingData(incomesByEachDay, true, start, limit);
			 }
			 
			if(includingChart != null && !includingChart.isEmpty()){
				final List<String> xAxis = new ArrayList<String>();
				final List<Float> incomes = new ArrayList<Float>();
				final List<Integer> orderAmounts = new ArrayList<Integer>();
				final List<Integer> customerAmounts = new ArrayList<Integer>();
				float totalMoney = 0, totalCount = 0; 
				int totalCustomerAmount = 0;
				int count = 0;
				
				for (IncomeByEachDay e : incomesByEachDay) {
					xAxis.add("\'" + e.getDate() + "\'");
					incomes.add(e.getIncomeByPay().getTotalActual());
					orderAmounts.add(e.getTotalAmount());
					customerAmounts.add(e.getCustomerAmount());
					totalMoney += e.getIncomeByPay().getTotalActual();
					totalCount += e.getTotalAmount();
					totalCustomerAmount += e.getCustomerAmount();
					count++ ;
				}
				
				final String chartData = "{\"xAxis\":" + xAxis + 
											",\"totalMoney\" : " + totalMoney + 
											",\"avgMoney\" : " + (count > 0 ? NumericUtil.roundFloat((totalMoney / count)) : "0") + 
											", \"avgCount\" : " + (count > 0 ? NumericUtil.roundFloat((totalCount / count)) : "0") +
											", \"totalCustomer\" : " + totalCustomerAmount +
											", \"avgCustomer\" : " + (count > 0 ? NumericUtil.roundFloat(totalCustomerAmount / count) : "0") +
										 ",\"ser\":[" +
											"{\"name\":\'营业额\', \"data\" : " + incomes + "}," +
											"{\"name\":\'账单数\', \"data\":" + orderAmounts + "}," + 
											"{\"name\":\'客流量\', \"data\":" + customerAmounts + "}" +
										 "]}";
				jObject.setExtra(new Jsonable(){
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("chart", chartData);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				
				});
			}
			
		 incomesByEachDay.add(total);
			
		 jObject.setRoot(incomesByEachDay);
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString(IncomeByPay.PAY_TYPE_FOR_STATISTICS));
		}
		return null;
	}
	
	public ActionForward chart(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String time = request.getParameter("time");
		JObject jobject = new JObject();
		Calendar c = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		endDate.add(Calendar.DATE, -1);
		
		final String pin = (String)request.getAttribute("pin");
		if(Integer.parseInt(time) == 7){
			c.add(Calendar.DATE, -7);
		}else if(Integer.parseInt(time) == 14){
			c.add(Calendar.DATE, -14);
		}else if(Integer.parseInt(time) == 30){
			c.add(Calendar.MONTH, -1);
		}
		
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY);
			
			extraCond.setDutyRange(new DutyRange(DateUtil.format(c.getTime()), DateUtil.format(endDate.getTime())));
			
			incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(staff, extraCond));
			
//			jobject.setRoot(incomesByEachDay);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Integer> countList = new ArrayList<Integer>();
			float totalMoney = 0, totalCount = 0;
			int count = 0;
			for (IncomeByEachDay e : incomesByEachDay) {
				xAxis.add("\'"+e.getDate()+"\'");
				data.add(e.getIncomeByPay().getTotalActual());
				countList.add(e.getTotalAmount());
				totalMoney += e.getIncomeByPay().getTotalActual();
				totalCount += e.getTotalAmount();
				count ++ ;
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + ", \"avgCount\" : " + Math.round((totalCount/count)*100)/100 + 
								",\"ser\":[{\"name\":\'营业额\', \"data\" : " + data + "},{\"name\":\'账单数\', \"data\":" + countList + "}]}";
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("chart", chartData);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			
			});
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
