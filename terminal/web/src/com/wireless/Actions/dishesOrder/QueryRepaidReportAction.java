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
import com.wireless.pojo.billStatistics.repaid.RepaidStatistics;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryRepaidReportAction extends DispatchAction{

	/**
	 * 获取反结账数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String staffId = request.getParameter("staffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
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
				jObject.setTotalProperty(result.size());
				RepaidStatistics total = new RepaidStatistics();
				for (RepaidStatistics item : result) {
					total.setRepaidPrice(total.getRepaidPrice() + item.getRepaidPrice());
				}
				result = DataPaging.getPagingData(result, true, start, limit);
				result.add(total);
			}
			jObject.setRoot(result);
		}catch (SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 获取反结账走势（按金额）
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getDetailChart(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		final String staffID = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<RepaidIncomeByEachDay> repaidIncomeByEachDay = CalcRepaidStatisticsDao.calcRepaidIncomeByEachDay(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (RepaidIncomeByEachDay c : repaidIncomeByEachDay) {
				xAxis.add("\'"+c.getDutyRange().getOffDutyFormat()+"\'");
				data.add(c.getRepaidPrice());
				amountData.add(c.getRepaidAmount());
				
				totalMoney += c.getRepaidPrice();
				totalCount += c.getRepaidAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/repaidIncomeByEachDay.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/repaidIncomeByEachDay.size())*100)/100 +
					",\"ser\":[{\"name\":\'反结账金额\', \"data\" : " + data + "}, {\"name\":\'反结账数量\', \"data\" : " + amountData + "}]}";
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
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}

		return null;
	}
	
	/**
	 * 获取反结账走势（按员工）
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getStaffChart(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			jObject.setRoot(CalcRepaidStatisticsDao.calcRepaidIncomeByStaff(staff, new DutyRange(dateBeg, dateEnd), extraCond));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}

		return null;
	}

}
