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
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;
import com.wireless.util.DataPaging;

public class BusinessReceiptsStatisticsAction extends DispatchAction {
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String includingChart = request.getParameter("includingChart");
		
		JObject jobject = new JObject();
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("dateBegin");
			String offDuty = request.getParameter("dateEnd");
			String opening = request.getParameter("opening");
			String ending = request.getParameter("ending");
			

			
			CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY);
			if(opening != null && !opening.isEmpty()){
				HourRange hr = new HourRange(opening, ending, Pattern.HOUR);
				extraCond.setHourRange(hr);
			}

			
			incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), extraCond));
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(incomesByEachDay.size() == 1 && incomesByEachDay.get(0).getIncomeByPay() == null){
				jobject.setRoot(new ArrayList<Jsonable>(0));
				
			}else{
/*				IncomeByEachDay total = new IncomeByEachDay(start);
				IncomeByPay payTotal = new IncomeByPay();
				IncomeByCancel cancelTotal = new IncomeByCancel();
				IncomeByDiscount discountTotal = new IncomeByDiscount();
				IncomeByErase eraseTotal = new IncomeByErase();
				IncomeByGift giftTotal = new IncomeByGift();
				IncomeByRepaid repaidTotal = new IncomeByRepaid();
				IncomeByCoupon couponTotal = new IncomeByCoupon();
				for (IncomeByEachDay eachDay : incomesByEachDay) {
					payTotal.setCashActual(eachDay.getIncomeByPay().getCashActual() + payTotal.getCashActual());
					payTotal.setCashAmount(eachDay.getIncomeByPay().getCashAmount() + payTotal.getCashAmount());
					payTotal.setCreditCardActual(eachDay.getIncomeByPay().getCreditCardActual() + payTotal.getCreditCardActual());
					payTotal.setCreditCardAmount(eachDay.getIncomeByPay().getCreditCardAmount() + payTotal.getCreditCardAmount());
					payTotal.setHangActual(eachDay.getIncomeByPay().getHangActual() + payTotal.getHangActual());
					payTotal.setHangAmount(eachDay.getIncomeByPay().getHangAmount() + payTotal.getHangAmount());
					payTotal.setSignActual(eachDay.getIncomeByPay().getSignActual() + payTotal.getSignActual());
					payTotal.setSignAmount(eachDay.getIncomeByPay().getSignAmount() + payTotal.getSignAmount());
					payTotal.setMemberCardActual(eachDay.getIncomeByPay().getMemberCardActual() + payTotal.getMemberCardActual());
					payTotal.setMemeberCardAmount(eachDay.getIncomeByPay().getMemberCardAmount() + payTotal.getMemberCardAmount());
					cancelTotal.setTotalCancel(eachDay.getIncomeByCancel().getTotalCancel() + cancelTotal.getTotalCancel());
					discountTotal.setTotalDiscount(eachDay.getIncomeByDiscount().getDiscountAmount() + discountTotal.getTotalDiscount());
					eraseTotal.setErasePrice(eachDay.getIncomeByErase().getEraseAmount() + eraseTotal.getTotalErase());
					giftTotal.setTotalGift(eachDay.getIncomeByGift().getGiftAmount() + giftTotal.getTotalGift());
					repaidTotal.setTotalRepaid(eachDay.getIncomeByRepaid().getRepaidAmount() + repaidTotal.getTotalRepaid());
					couponTotal.setTotalCoupon(eachDay.getIncomeByCoupon().getTotalCoupon() + couponTotal.getTotalCoupon());
					couponTotal.setCouponAmount(eachDay.getIncomeByCoupon().getCouponAmount() + couponTotal.getCouponAmount());
				}
				
				total.setIncomeByCancel(cancelTotal);
				total.setIncomeByDiscount(discountTotal);
				total.setIncomeByErase(eraseTotal);
				total.setIncomeByGift(giftTotal);
				total.setIncomeByPay(payTotal);
				total.setIncomeByRepaid(repaidTotal);
				total.setIncomeByCoupon(couponTotal);*/
				
				jobject.setTotalProperty(incomesByEachDay.size());
				incomesByEachDay = DataPaging.getPagingData(incomesByEachDay, isPaging, start, limit);
				
//				incomesByEachDay.add(total);
				jobject.setRoot(incomesByEachDay);
			}
			if(includingChart != null && !includingChart.isEmpty()){
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
			}
			
			response.getWriter().print(jobject.toString(IncomeByPay.PAY_TYPE_FOR_STATISTICS));
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
