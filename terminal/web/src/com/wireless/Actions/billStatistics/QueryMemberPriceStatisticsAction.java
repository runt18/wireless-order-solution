package com.wireless.Actions.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcMemberPriceDao;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.member.MemberPriceByEachDay;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryMemberPriceStatisticsAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String staffId = request.getParameter("staffId");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			CalcMemberPriceDao.ExtraCond extraCond = new CalcMemberPriceDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.isEmpty()){
				extraCond.setStaff(Integer.parseInt(staffId));
			}
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			final List<Order> result;
			DutyRange range = DutyRangeDao.exec(staff, beginDate, endDate);
			if(range != null){
				result = CalcMemberPriceDao.getMemberPriceDetail(staff, range, extraCond);
			}else{
				result = CalcMemberPriceDao.getMemberPriceDetail(staff, new DutyRange(beginDate, endDate), extraCond);
			}
			
			if(!result.isEmpty()){
				jobject.setTotalProperty(result.size());
				Order total = new Order();
				for (Order item : result) {
					total.setPurePrice(item.getPurePrice() + total.getPurePrice());
					total.setActualPrice(item.getActualPrice() + total.getActualPrice());
				}
				result.clear();
				result.addAll(DataPaging.getPagingData(result, true, start, limit));
				result.add(total);
				
			}
			jobject.setRoot(result);
		}catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward chart4IncomeByEachDay(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String staffId = request.getParameter("staffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		JObject jobject = new JObject();
		
		try{
			CalcMemberPriceDao.ExtraCond extraCond = new CalcMemberPriceDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.isEmpty()){
				extraCond.setStaff(Integer.valueOf(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<MemberPriceByEachDay> result = CalcMemberPriceDao.calcMemberPriceByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (MemberPriceByEachDay c : result) {
				xAxis.add("\'" + c.getRange().getOffDutyFormat() + "\'");
				data.add(c.getPrice());
				amountData.add(c.getAmount());
				
				totalMoney += c.getPrice();
				totalCount += c.getAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/result.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/result.size())*100)/100 + 
					",\"ser\":[{\"name\":\'会员价金额\', \"data\" : " + data + "}, {\"name\":\'会员价数量\', \"data\" : " + amountData + "}]}";
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
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
}
