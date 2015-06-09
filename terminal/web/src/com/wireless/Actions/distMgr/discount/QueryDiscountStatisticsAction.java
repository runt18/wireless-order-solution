package com.wireless.Actions.distMgr.discount;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcDiscountStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByDept;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByEachDay;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByStaff;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryDiscountStatisticsAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String staffId = request.getParameter("staffID");
		String deptID = request.getParameter("deptID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<Order> list;
			
			CalcDiscountStatisticsDao.ExtraCond extraCond = new CalcDiscountStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
				extraCond.setStaffId(Integer.valueOf(staffId));
			}
			
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			list = CalcDiscountStatisticsDao.getDiscountStatisticsDetail(staff, new DutyRange(beginDate, endDate), extraCond);
			
			if(!list.isEmpty()){
				jobject.setTotalProperty(list.size());
				Order total = new Order();
				for (Order item : list) {
					total.setDiscountPrice(item.getDiscountPrice() + total.getDiscountPrice());
					total.setActualPrice(item.getActualPrice() + total.getActualPrice());
				}
				total.setDestTbl(list.get(0).getDestTbl());
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(total);
				
			}
			jobject.setRoot(list);
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
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		JObject jobject = new JObject();
		
		try{
			CalcDiscountStatisticsDao.ExtraCond extraCond = new CalcDiscountStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			List<DiscountIncomeByEachDay> cancelList = CalcDiscountStatisticsDao.calcDiscountIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (DiscountIncomeByEachDay c : cancelList) {
				xAxis.add("\'"+c.getRange().getOffDutyFormat()+"\'");
				data.add(c.getmDiscountPrice());
				amountData.add(c.getmDiscountAmount());
				
				totalMoney += c.getmDiscountPrice();
				totalCount += c.getmDiscountAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/cancelList.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/cancelList.size())*100)/100 + 
					",\"ser\":[{\"name\":\'折扣金额\', \"data\" : " + data + "}, {\"name\":\'折扣数量\', \"data\" : " + amountData + "}]}";
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
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		JObject jobject = new JObject();
		
		try{
			CalcDiscountStatisticsDao.ExtraCond extraCond = new CalcDiscountStatisticsDao.ExtraCond(DateType.HISTORY);
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			List<DiscountIncomeByStaff> cancelList = CalcDiscountStatisticsDao.calcDiscountIncomeByStaff(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
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
		
	public ActionForward getDeptChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		JObject jobject = new JObject();
		
		try{
			CalcDiscountStatisticsDao.ExtraCond extraCond = new CalcDiscountStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			List<DiscountIncomeByDept> cancelList = CalcDiscountStatisticsDao.calcDiscountIncomeByDept(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
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
