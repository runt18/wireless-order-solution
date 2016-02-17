package com.wireless.Actions.billStatistics;

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
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;
import com.wireless.util.DataPaging;

public class BusinessReceiptsStatisticsAction extends DispatchAction {
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String onDuty = request.getParameter("dateBegin");
		final String offDuty = request.getParameter("dateEnd");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String isPaging = request.getParameter("isPaging");
		final String region = request.getParameter("region");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String includingChart = request.getParameter("includingChart");
		
		final JObject jObject = new JObject();
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		try{

			final CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY);
			if(opening != null && !opening.isEmpty()){
				HourRange hr = new HourRange(opening, ending, Pattern.HOUR);
				extraCond.setHourRange(hr);
			}
			
			if(region != null && !region.isEmpty()){
				extraCond.setRegion(Region.RegionId.valueOf(Integer.parseInt(region)));
			}
			
			incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), extraCond));
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(incomesByEachDay.size() == 1 && incomesByEachDay.get(0).getIncomeByPay() == null){
				jObject.setRoot(new ArrayList<Jsonable>(0));
				
			}else{
				
				jObject.setTotalProperty(incomesByEachDay.size());
				incomesByEachDay = DataPaging.getPagingData(incomesByEachDay, isPaging, start, limit);
				
//				incomesByEachDay.add(total);
				jObject.setRoot(incomesByEachDay);
			}
			if(includingChart != null && !includingChart.isEmpty()){
				List<String> xAxis = new ArrayList<String>();
				List<Float> data = new ArrayList<Float>();
				List<Integer> countList = new ArrayList<Integer>();
				float totalMoney = 0, totalCount = 0;
				int count = 0;
				for (IncomeByEachDay e : incomesByEachDay) {
					xAxis.add("\'" + e.getDate() + "\'");
					data.add(e.getIncomeByPay().getTotalActual());
					countList.add(e.getTotalAmount());
					totalMoney += e.getIncomeByPay().getTotalActual();
					totalCount += e.getTotalAmount();
					count++ ;
				}
				
				final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + ", \"avgCount\" : " + Math.round((totalCount/count)*100)/100 + 
									",\"ser\":[{\"name\":\'营业额\', \"data\" : " + data + "},{\"name\":\'账单数\', \"data\":" + countList + "}]}";
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
			
			response.getWriter().print(jObject.toString(IncomeByPay.PAY_TYPE_FOR_STATISTICS));
		}
		return null;
	}
	
	public ActionForward chart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String time = request.getParameter("time");
		JObject jobject = new JObject();
		Calendar c = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		endDate.add(Calendar.DATE, -1);
		
		if(Integer.parseInt(time) == 7){
			c.add(Calendar.DATE, -7);
		}else if(Integer.parseInt(time) == 14){
			c.add(Calendar.DATE, -14);
		}else if(Integer.parseInt(time) == 30){
			c.add(Calendar.MONTH, -1);
		}
		
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		try{
			String pin = (String)request.getAttribute("pin");
			incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(DateUtil.format(c.getTime()), DateUtil.format(endDate.getTime())), new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)));
			
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
