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
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByEachDay;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByStaff;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryCancelledFoodAction extends DispatchAction{
	
	public ActionForward getDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		
		JObject jobject = new JObject();
		try{
			final String pin = (String)request.getAttribute("pin");
			final String beginDate = request.getParameter("dateBeg");
			final String endDate = request.getParameter("dateEnd");
			final String deptId = request.getParameter("deptID");
			final String reasonId = request.getParameter("reasonID");
			final String staffId = request.getParameter("staffID");
			final String opening = request.getParameter("opening");
			final String ending = request.getParameter("ending");
			
			if(beginDate == null || beginDate.trim().isEmpty()){
				jobject.initTip(false, JObject.TIP_TITLE_ERROE, "操作失败, 请指定统计日期开始时间.");
				return null;
			}
			if(endDate == null || endDate.trim().isEmpty()){
				jobject.initTip(false, JObject.TIP_TITLE_ERROE, "操作失败, 请指定统计日期结束时间.");
				return null;
			}
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));

			final OrderFoodDao.ExtraCond4CancelFood extraCond = new OrderFoodDao.ExtraCond4CancelFood(DateType.HISTORY);
			
			DutyRange range = DutyRangeDao.exec(staff, beginDate, endDate);
			if(range != null){
				extraCond.setDutyRange(range);
			}else{
				extraCond.setDutyRange(new DutyRange(beginDate, endDate));
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
			
			List<OrderFood> cancelList = OrderFoodDao.getSingleDetail(staff, extraCond, null);
			if(!cancelList.isEmpty()){
				jobject.setTotalProperty(cancelList.size());
				float totalPrice = 0, totalCount = 0;
				for (OrderFood orderFood : cancelList) {
					totalPrice += orderFood.calcPrice();
					totalCount += orderFood.getCount();
				}
				
				Food totalFood = new Food(0);
				totalFood.setKitchen(cancelList.get(0).asFood().getKitchen());
				totalFood.setPrice(totalPrice);
				
				OrderFood total = new OrderFood(totalFood);
				
				total.setCount(totalCount);
				
				total.setCancelReason(cancelList.get(0).getCancelReason());
				
				cancelList = DataPaging.getPagingData(cancelList, true, start, limit);
				
				cancelList.add(total);
				
			}
			jobject.setRoot(cancelList);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
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
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<CancelIncomeByEachDay> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			
			List<Float> amountData = new ArrayList<Float>();
			
			float totalMoney = 0, totalCount = 0;
			for (CancelIncomeByEachDay c : cancelList) {
				xAxis.add("\'"+c.getDutyRange().getOffDutyFormat()+"\'");
				data.add(c.getCancelPrice());
				amountData.add(c.getCancelAmount());
				
				totalMoney += c.getCancelPrice();
				totalCount += c.getCancelAmount();
				
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/cancelList.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/cancelList.size())*100)/100 +
					",\"ser\":[{\"name\":\'退菜金额\', \"data\" : " + data + "},{\"name\":\'退菜数量\', \"data\" : " + amountData + "}]}";
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
	
	public ActionForward getReasonChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
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
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<CancelIncomeByReason> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByReason(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
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
	
	public ActionForward getStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
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
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<CancelIncomeByStaff> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByStaff(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
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
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
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
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<CancelIncomeByDept> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByDept(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
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
