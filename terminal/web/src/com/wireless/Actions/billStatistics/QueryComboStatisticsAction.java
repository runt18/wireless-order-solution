package com.wireless.Actions.billStatistics;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.CalcComboStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.combo.ComboIncome;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class QueryComboStatisticsAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String subFoodName = request.getParameter("subFoodName");
		final String deptId = request.getParameter("deptId");
		final String branchId = request.getParameter("branchId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jobject = new JObject();
		
		try{
			final CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)
																		.setDutyRange(new DutyRange(onDuty, offDuty))
																		.setCalcByDuty(true);
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			if(subFoodName != null && !subFoodName.isEmpty()){
				extraCond.setFoodName(subFoodName);
			}
			
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDept(DeptId.valueOf(Integer.parseInt(deptId)));
			}
			
			if(opening != null && !opening.isEmpty() && ending != null && !ending.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending));
			}
			
			List<ComboIncome> result = CalcComboStatisticsDao.calcCombo(staff, extraCond);
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jobject.setTotalProperty(result.size());
				
				ComboIncome c = new ComboIncome();
				
				for(ComboIncome comboIncome : result){
					c.setAmount(comboIncome.getAmount() + c.getAmount());
					c.setTotalPrice(comboIncome.getTotalPrice() + c.getTotalPrice());
				}
				
				result = DataPaging.getPagingData(result, true, Integer.parseInt(start), Integer.parseInt(limit));
				
				result.add(c);
			}
			
			jobject.setRoot(result);
			
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
		
}
