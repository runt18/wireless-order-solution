package com.wireless.Actions.billStatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByCoupon;
import com.wireless.pojo.billStatistics.IncomeByDiscount;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByErase;
import com.wireless.pojo.billStatistics.IncomeByGift;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.billStatistics.IncomeByRepaid;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class BusinessReceiptsStatisticsAction extends DispatchAction {
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		JObject jobject = new JObject();
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("dateBegin");
			String offDuty = request.getParameter("dateEnd");
			
			incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), onDuty, offDuty));
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(incomesByEachDay.size() == 1 && incomesByEachDay.get(0).getIncomeByPay() == null){
				jobject.setRoot(null);
				
			}else{
				IncomeByEachDay total = new IncomeByEachDay(start);
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
				total.setIncomeByCoupon(couponTotal);
				
				jobject.setTotalProperty(incomesByEachDay.size());
				incomesByEachDay = DataPaging.getPagingData(incomesByEachDay, isPaging, start, limit);
				
				incomesByEachDay.add(total);
				jobject.setRoot(incomesByEachDay);
			}
			response.getWriter().print(jobject.toString());
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
			incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), DateUtil.format(c.getTime()), DateUtil.format(endDate.getTime())));
			
			jobject.setRoot(incomesByEachDay);
			
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
			
			Map<Object, Object> map = new HashMap<Object, Object>();
			String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + ", \"avgCount\" : " + Math.round((totalCount/count)*100)/100 + 
								",\"ser\":[{\"name\":\'营业额\', \"data\" : " + data + "},{\"name\":\'账单数\', \"data\":" + countList + "}]}";
			map.put("chart", chartData);
			jobject.setOther(map);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
