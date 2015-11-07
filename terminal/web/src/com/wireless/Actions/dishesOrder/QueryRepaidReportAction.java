package com.wireless.Actions.dishesOrder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcRepaidStatisticsDao;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByEachDay;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByStaff;
import com.wireless.pojo.billStatistics.repaid.RepaidStatistics;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryRepaidReportAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String staffId = request.getParameter("staffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			
			List<RepaidStatistics> result;
			DutyRange range = DutyRangeDao.exec(staff, beginDate, endDate);
			if(range != null){
				result = CalcRepaidStatisticsDao.getRepaidIncomeDetail(staff, range, extraCond);
			}else{
				result = CalcRepaidStatisticsDao.getRepaidIncomeDetail(staff, new DutyRange(beginDate, endDate), extraCond);
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jobject.setTotalProperty(result.size());
				RepaidStatistics total = new RepaidStatistics();
				for (RepaidStatistics item : result) {
					total.setRepaidPrice(total.getRepaidPrice() + item.getRepaidPrice());
				}
				result = DataPaging.getPagingData(result, true, start, limit);
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
	
	public ActionForward getDetailChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		JObject jobject = new JObject();
		
		try{
			CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<RepaidIncomeByEachDay> cancelList = CalcRepaidStatisticsDao.calcRepaidIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (RepaidIncomeByEachDay c : cancelList) {
				xAxis.add("\'"+c.getDutyRange().getOffDutyFormat()+"\'");
				data.add(c.getRepaidPrice());
				amountData.add(c.getRepaidAmount());
				
				totalMoney += c.getRepaidPrice();
				totalCount += c.getRepaidAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/cancelList.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/cancelList.size())*100)/100 +
					",\"ser\":[{\"name\":\'反结账金额\', \"data\" : " + data + "}, {\"name\":\'反结账数量\', \"data\" : " + amountData + "}]}";
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
	
	public ActionForward getStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		JObject jobject = new JObject();
		
		try{
			CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<RepaidIncomeByStaff> cancelList = CalcRepaidStatisticsDao.calcRepaidIncomeByStaff(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
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
