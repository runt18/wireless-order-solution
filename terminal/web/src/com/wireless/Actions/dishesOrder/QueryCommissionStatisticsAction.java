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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByEachDay;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByStaff;
import com.wireless.pojo.billStatistics.commission.CommissionStatistics;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class QueryCommissionStatisticsAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String staffId = request.getParameter("staffId");
		String deptId = request.getParameter("deptId");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<CommissionStatistics> list;
			
			CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			
			if(deptId != null && !deptId.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			list = CalcCommissionStatisticsDao.getCommissionStatisticsDetail(staff, new DutyRange(beginDate, endDate), extraCond);
			
			if(!list.isEmpty()){
				jobject.setTotalProperty(list.size());
				CommissionStatistics total = new CommissionStatistics();
				total.setDept(new Department(-1));
				for (CommissionStatistics item : list) {
					total.setTotalPrice(item.getTotalPrice() + total.getTotalPrice());
					total.setCommission(item.getCommission() + total.getCommission());
				}
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(total);
				jobject.setRoot(list);
			}
		}catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
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
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffID");
		
		JObject jobject = new JObject();
		
		try{
			CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<CommissionIncomeByEachDay> cancelList = CalcCommissionStatisticsDao.calcCommissionIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			float totalMoney = 0;
			for (CommissionIncomeByEachDay c : cancelList) {
				xAxis.add("\'"+c.getRange().getOffDutyFormat()+"\'");
				data.add(c.getmCommissionPrice());
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/cancelList.size())*100)/100 + 
					",\"ser\":[{\"name\":\'提成金额\', \"data\" : " + data + "}]}";
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
	
	public ActionForward getStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffID");
		
		JObject jobject = new JObject();
		
		try{
			CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			List<CommissionIncomeByStaff> cancelList = CalcCommissionStatisticsDao.calcCommissionIncomeByStaff(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
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
