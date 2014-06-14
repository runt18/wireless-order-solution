package com.wireless.Actions.billHistory;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcCancelStatisticsDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByEachDay;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByStaff;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;
import com.wireless.util.WebParams;

public class QueryCancelledFoodAction extends DispatchAction{
	
	public ActionForward getDetail(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String dateBeg = request.getParameter("dateBeg");
			String dateEnd = request.getParameter("dateEnd");
			String deptID = request.getParameter("deptID");
			String reasonID = request.getParameter("reasonID");
			String staffID = request.getParameter("staffID");
			
			if(dateBeg == null || dateBeg.trim().isEmpty()){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 请指定统计日期开始时间.");
				return null;
			}
			if(dateEnd == null || dateEnd.trim().isEmpty()){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 请指定统计日期结束时间.");
				return null;
			}
			
			
			Integer did = Integer.valueOf(deptID);
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			OrderFoodDao.ExtraCond4CancelFood extraCond = new OrderFoodDao.ExtraCond4CancelFood(DateType.HISTORY);
			
			extraCond.setDutyRange(new DutyRange(dateBeg, dateEnd));
			
			if(reasonID != null && !reasonID.isEmpty() && !reasonID.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonID));
			}
			if(did != -1){
				extraCond.setDeptId(DeptId.valueOf(did));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<OrderFood> cancelList = OrderFoodDao.getSingleDetail(staff, extraCond, null);
			
			
			if(!cancelList.isEmpty()){
				jobject.setTotalProperty(cancelList.size());
				cancelList = DataPaging.getPagingData(cancelList, true, start, limit);
				
				
/*				List<String> xAxis = new ArrayList<String>();
				List<Float> data = new ArrayList<Float>();
				for (CancelIncomeByEachDay c : cancelList) {
					xAxis.add("\'"+c.getDutyRange().getOffDutyFormat()+"\'");
					data.add(c.getCancelPrice());
					
					totalCount += c.getCancelAmount();
					totalPrice += c.getCancelPrice();
				}
				CancelIncomeByEachDay total = new CancelIncomeByEachDay(new DutyRange(dateBeg, dateEnd), totalCount, totalPrice);
				
				cancelList = DataPaging.getPagingData(cancelList, true, start, limit);
				
				cancelList.add(total);*/
			}
			jobject.setRoot(cancelList);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		} finally{
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
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		
		JObject jobject = new JObject();
		
		try{
			CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(reasonID != null && !reasonID.isEmpty() && !reasonID.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonID));
			}
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<CancelIncomeByEachDay> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			float totalMoney = 0;
			for (CancelIncomeByEachDay c : cancelList) {
				xAxis.add("\'"+c.getDutyRange().getOffDutyFormat()+"\'");
				data.add(c.getCancelPrice());
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/cancelList.size())*100)/100 + 
					",\"ser\":[{\"name\":\'退菜金额\', \"data\" : " + data + "}]}";
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
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
	
	public ActionForward getReasonChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		
		JObject jobject = new JObject();
		
		try{
			CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(reasonID != null && !reasonID.isEmpty() && !reasonID.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonID));
			}
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<CancelIncomeByReason> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByReason(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		
		JObject jobject = new JObject();
		
		try{
			CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(reasonID != null && !reasonID.isEmpty() && !reasonID.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonID));
			}
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<CancelIncomeByStaff> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByStaff(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
	
	public ActionForward getDeptChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		
		JObject jobject = new JObject();
		
		try{
			CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(reasonID != null && !reasonID.isEmpty() && !reasonID.equals("-1")){
				extraCond.setReasonId(Integer.valueOf(reasonID));
			}
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<CancelIncomeByDept> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByDept(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
	
}
