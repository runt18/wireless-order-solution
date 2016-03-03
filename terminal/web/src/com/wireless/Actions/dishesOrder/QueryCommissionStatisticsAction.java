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

import com.wireless.db.billStatistics.CalcCommissionStatisticsDao;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByEachDay;
import com.wireless.pojo.billStatistics.commission.CommissionStatistics;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryCommissionStatisticsAction extends DispatchAction{

	/**
	 * 获取提成统计
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
		final String deptId = request.getParameter("deptId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			
			if(deptId != null && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			DutyRange range = DutyRangeDao.exec(staff, beginDate, endDate);
			if(range == null){
				range = new DutyRange(beginDate, endDate); 
			}
			
			List<CommissionStatistics> list = CalcCommissionStatisticsDao.getCommissionStatisticsDetail(staff, range, extraCond);
			
			if(!list.isEmpty()){
				jObject.setTotalProperty(list.size());
				CommissionStatistics total = new CommissionStatistics();
				total.setDept(new Department(-1));
				for (CommissionStatistics item : list) {
					total.setTotalPrice(item.getTotalPrice() + total.getTotalPrice());
					total.setCommission(item.getCommission() + total.getCommission());
				}
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(total);
			}
			jObject.setRoot(list);
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
	 * 获取提成统计走势图（按金额）
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
		final String deptId = request.getParameter("deptID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			DutyRange range = DutyRangeDao.exec(staff, dateBeg, dateEnd);
			if(range == null){
				range = new DutyRange(dateBeg, dateEnd); 
			}
			
			final List<CommissionIncomeByEachDay> result = CalcCommissionStatisticsDao.calcCommissionIncomeByEachDay(staff, range, extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (CommissionIncomeByEachDay c : result) {
				xAxis.add("\'" + c.getRange().getOffDutyFormat() + "\'");
				data.add(c.getmCommissionPrice());
				amountData.add(c.getmCommissionAmount());
				
				totalMoney += c.getmCommissionPrice();
				totalCount += c.getmCommissionAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/result.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/result.size())*100)/100 + 
					",\"ser\":[{\"name\":\'提成金额\', \"data\" : " + data + "}, {\"name\":\'提成数量\', \"data\" : " + amountData + "}]}";
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
	 * 获取提成统计走势图（按员工）
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
		final String deptId = request.getParameter("deptID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			DutyRange range = DutyRangeDao.exec(staff, dateBeg, dateEnd);
			if(range == null){
				range = new DutyRange(dateBeg, dateEnd); 
			}
			
			jObject.setRoot(CalcCommissionStatisticsDao.calcCommissionIncomeByStaff(staff, range, extraCond));
			
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
