package com.wireless.Actions.billHistory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcCancelStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.cancel.CancelDetail;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByEachDay;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.DataPaging;

public class QueryCancelledFoodAction extends DispatchAction{
	
	/**
	 * 获取退菜明细
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String limit = request.getParameter("limit");
		final String start = request.getParameter("start");
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String beginDate = request.getParameter("dateBeg");
		final String endDate = request.getParameter("dateEnd");
		final String deptId = request.getParameter("deptID");
		final String reasonId = request.getParameter("reasonID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		try{
			
			if(beginDate == null || beginDate.trim().isEmpty()){
				jObject.initTip(false, JObject.TIP_TITLE_ERROE, "操作失败, 请指定统计日期开始时间.");
				return null;
			}
			if(endDate == null || endDate.trim().isEmpty()){
				jObject.initTip(false, JObject.TIP_TITLE_ERROE, "操作失败, 请指定统计日期结束时间.");
				return null;
			}
			
			final CalcCancelStatisticsDao.ExtraCond extraCond4Total = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY).setCalcByDuty(true);
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond4Total.setChain(true);
				}
			}
			
			DutyRange range = new DutyRange(beginDate, endDate);
			extraCond4Total.setRange(range);
			
			if(reasonId != null && !reasonId.isEmpty() && !reasonId.equals("-1")){
				extraCond4Total.setReasonId(Integer.parseInt(reasonId));
			}
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond4Total.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond4Total.setStaffId(Integer.parseInt(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond4Total.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<CancelDetail> cancelList = CalcCancelStatisticsDao.getDetail(staff, extraCond4Total);
			if(!cancelList.isEmpty()){
				jObject.setTotalProperty(cancelList.size());
				
				CancelDetail total = new CancelDetail();
				
				for(CancelDetail cancelDetail : cancelList){
					total.setTotalAmount(cancelDetail.getTotalAmount() + total.getTotalAmount());
					total.setTotalCancel(cancelDetail.getTotalCancel() + total.getTotalCancel());
				}
				
				cancelList = DataPaging.getPagingData(cancelList, true, Integer.parseInt(start), Integer.parseInt(limit));
				
				cancelList.add(total);
				
			}
			jObject.setRoot(cancelList);
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 获取退菜走势图（按数量）
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
		final String reasonId = request.getParameter("reasonID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			final CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY)
																						   .setRange(new DutyRange(dateBeg, dateEnd))
																						   .setCalcByDuty(true);
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
			if(reasonId != null && !reasonId.isEmpty() && !reasonId.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonId));
			}
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			final List<CancelIncomeByEachDay> cancelList = CalcCancelStatisticsDao.calcIncomeByEachDay(staff, extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			
			List<Float> amountData = new ArrayList<Float>();
			
			float totalMoney = 0, totalCount = 0;
			for (CancelIncomeByEachDay c : cancelList) {
				xAxis.add("\'" + c.getDutyRange().getOnDutyFormat() + "\'");
				data.add(c.getCancelPrice());
				amountData.add(c.getCancelAmount());
				
				totalMoney += c.getCancelPrice();
				totalCount += c.getCancelAmount();
				
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + NumericUtil.roundFloat(totalMoney) + ",\"avgMoney\" : " + Math.round((totalMoney/cancelList.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/cancelList.size())*100)/100 +
					",\"ser\":[{\"name\":\'退菜金额\', \"data\" : " + data + "},{\"name\":\'退菜数量\', \"data\" : " + amountData + "}]}";
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
			
		}catch(BusinessException | SQLException e){
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
	 * 获取退菜走势图（按原因）
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getReasonChart(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		final String deptId = request.getParameter("deptID");
		final String reasonId = request.getParameter("reasonID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			final CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY)
																						   .setRange(new DutyRange(dateBeg, dateEnd))
																						   .setCalcByDuty(true);
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
			
			if(reasonId != null && !reasonId.isEmpty() && !reasonId.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonId));
			}
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			jObject.setRoot(CalcCancelStatisticsDao.calcIncomeByReason(staff, extraCond));
			
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
	 * 获取退菜走势图（按员工）
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
		final String reasonId = request.getParameter("reasonID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			final CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY)
																						   .setCalcByDuty(true)
																						   .setRange(new DutyRange(dateBeg, dateEnd));
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
			if(reasonId != null && !reasonId.isEmpty() && !reasonId.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonId));
			}
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			jObject.setRoot(CalcCancelStatisticsDao.calcIncomeByStaff(staff, extraCond));
			
		}catch(BusinessException | SQLException e){
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
	 * 获取退菜走势图（按部门）
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getDeptChart(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		final String deptId = request.getParameter("deptID");
		final String reasonId = request.getParameter("reasonID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			final CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY)
																						   .setRange(new DutyRange(dateBeg, dateEnd))
																						   .setCalcByDuty(true);
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
			if(reasonId != null && !reasonId.isEmpty() && !reasonId.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonId));
			}
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			jObject.setRoot(CalcCancelStatisticsDao.calcIncomeByDept(staff, extraCond));
			
		}catch(BusinessException  | SQLException e){
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
