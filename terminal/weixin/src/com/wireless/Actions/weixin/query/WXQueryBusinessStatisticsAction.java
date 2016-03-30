package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond;
import com.wireless.db.billStatistics.CalcCancelStatisticsDao;
import com.wireless.db.billStatistics.CalcCommissionStatisticsDao;
import com.wireless.db.billStatistics.CalcDiscountStatisticsDao;
import com.wireless.db.billStatistics.CalcGiftStatisticsDao;
import com.wireless.db.billStatistics.CalcMemberStatisticsDao;
import com.wireless.db.billStatistics.CalcRepaidStatisticsDao;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.billStatistics.SaleDetailsDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.finance.WeixinFinanceDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByFood;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByStaff;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByStaff;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByDept;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByStaff;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByDept;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByStaff;
import com.wireless.pojo.billStatistics.member.MemberStatistics;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByStaff;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class WXQueryBusinessStatisticsAction extends DispatchAction {
	
	/**
	 * history
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward history(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		String openId = request.getParameter("oid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
			staff = StaffDao.getAdminByRestaurant(rid);
		}
		JObject jObject = new JObject();
		try{
			
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String dutyRange = request.getParameter("dutyRange");
			
			String region = request.getParameter("region");
			
			final String chart = request.getParameter("chart");
			
			CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY); 
			
			if(region != null && !region.equals("-1")){
				extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			List<IncomeByEachDay> incomesByEachDay;
			String chartData = null ;
			if(chart != null && !chart.isEmpty()){
				incomesByEachDay = new ArrayList<IncomeByEachDay>();
				
				incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(staff, new DutyRange(onDuty, offDuty), extraCond));
				
				List<String> xAxis = new ArrayList<String>();
				List<Float> data = new ArrayList<Float>();
				List<Integer> countList = new ArrayList<Integer>();
				float totalMoney = 0, totalCount = 0;
				int count = 0;
				for (IncomeByEachDay e : incomesByEachDay) {
					xAxis.add("\""+e.getDate()+"\"");
					data.add(e.getIncomeByPay().getTotalActual());
					countList.add(e.getTotalAmount());
					totalMoney += e.getIncomeByPay().getTotalActual();
					totalCount += e.getTotalAmount();
					count ++ ;
				}
				
//				chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + ", \"avgCount\" : " + Math.round((totalCount/count)*100)/100 + 
//									",\"ser\":[{\"name\":\'营业额\', \"data\" : " + data + "},{\"name\":\'账单数\', \"data\":" + countList + "}]}";
				
				chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + ", \"avgCount\" : " + Math.round((totalCount/count)*100)/100 + 
						",\"ser\":[{\"name\":\"营业额\", \"data\" : " + data + "},{\"name\":\"账单数\", \"data\":" + countList + "}]}";		
				
			}
			final String chartDatas = chartData;
			final ShiftDetail shiftDetail;
			final MemberStatistics memberStatistics;
			if(!dutyRange.equals("null") && !dutyRange.trim().isEmpty()){
				DutyRange range = DutyRangeDao.exec(staff, onDuty, offDuty);
				
				if(range != null){
					shiftDetail = ShiftDao.getByRange(staff, range, extraCond);
					
					memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, range, new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY));
				}else{
					shiftDetail = new ShiftDetail(new DutyRange(onDuty, offDuty));
					memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(onDuty, offDuty), new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY));
				}
			}else{
				shiftDetail = ShiftDao.getByRange(staff, new DutyRange(onDuty, offDuty), extraCond);
				
				memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(onDuty, offDuty), new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY));
			}
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("business", shiftDetail, 0);
					if(chart != null && !chart.isEmpty()){
						jm.putString("businessChart", chartDatas);
					}
					jm.putJsonable("memberStatistics", memberStatistics, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException e){
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
	
	
	public ActionForward deptSaleStatistic(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		String openId = request.getParameter("oid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
			staff = StaffDao.getAdminByRestaurant(rid);
		}
	
		
		List<SalesDetail> salesDetailList = new ArrayList<>();
		JObject jobject = new JObject();
		String dataType = request.getParameter("dataType");
		String queryType = request.getParameter("queryType");
		try{
			String dateBeg = request.getParameter("dateBeg");
			String dateEnd = request.getParameter("dateEnd");
			
			String orderType = request.getParameter("orderType");
			String deptID = request.getParameter("deptID");
			String deptName = request.getParameter("deptName");
			String region = request.getParameter("region");
			
			
			dataType = dataType != null && dataType.length() > 0 ? dataType.trim() : "1";
			queryType = queryType != null && queryType.length() > 0 ? queryType.trim() : "0";
			orderType = orderType != null && orderType.length() > 0 ? orderType.trim() : "1";
			deptID = deptID != null && deptID.length() > 0 ? deptID.trim() : "-1";
			
			
			Integer qt = Integer.valueOf(queryType), ot = (orderType != null && !orderType.isEmpty()) ? Integer.parseInt(orderType) : SaleDetailsDao.ORDER_BY_SALES;
			DateType dt = DateType.valueOf(Integer.valueOf(dataType));
			
			CalcBillStatisticsDao.ExtraCond extraConds = new ExtraCond(dt);
				
			if(dt.isHistory()){
				dateBeg = dateBeg != null && dateBeg.length() > 0 ? dateBeg.trim() + " 00:00:00" : "";
				dateEnd = dateEnd != null && dateEnd.length() > 0 ? dateEnd.trim() + " 23:59:59" : "";
			}
			
			DutyRange dutyRange = new DutyRange(dateBeg, dateEnd);
			
			if(region != null && !region.equals("-1")){
				extraConds.setRegion(RegionId.valueOf(Integer.parseInt(region)));
				
			}
			
			if(qt == SaleDetailsDao.QUERY_BY_DEPT){
				
				salesDetailList = SaleDetailsDao.getByDept(
						staff, 
						dutyRange,
						extraConds);
				
			}else if(qt == SaleDetailsDao.QUERY_BY_FOOD){
				Department dept = DepartmentDao.getByCond(staff, new DepartmentDao.ExtraCond().setName(deptName), null).get(0);
				if(deptName != null && dept.getId() >= 0){
					extraConds.setDept(Department.DeptId.valueOf(dept.getId()));
				} 
				salesDetailList = SaleDetailsDao.getByFood(staff, dutyRange, extraConds, ot);
			}else if(qt == SaleDetailsDao.QUERY_BY_KITCHEN){
				
				List<SalesDetail> result = SaleDetailsDao.getByKitchen(
						staff, 
						dutyRange,
						extraConds);
				
				for (SalesDetail salesDetail : result) {
					if(salesDetail.getDept().getName().equals(deptName)){
						salesDetailList.add(salesDetail);
					}
				}
			}
				
		} catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally{
			jobject.setTotalProperty(salesDetailList.size());
			
			jobject.setRoot(salesDetailList);
			response.getWriter().print(jobject.toString());
		}		
		
		return null;
	}
	
	public ActionForward getDiscountStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
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
			List<DiscountIncomeByStaff> cancelList = CalcDiscountStatisticsDao.calcDiscountIncomeByStaff(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
		
	public ActionForward getDiscountDeptChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
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
			List<DiscountIncomeByDept> cancelList = CalcDiscountStatisticsDao.calcDiscountIncomeByDept(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
	
	public ActionForward getGiftStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String region = request.getParameter("region");
		String foodName = request.getParameter("foodName");
		String giftStaffId = request.getParameter("giftStaffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jobject = new JObject();
		
		try{
			CalcGiftStatisticsDao.ExtraCond extraCond = new CalcGiftStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(region != null && !region.equals("-1")){
				extraCond.setRegionId(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			if(foodName != null && !foodName.trim().isEmpty()){
				extraCond.setFoodName(foodName);
			}
			
			if(giftStaffId != null && !giftStaffId.isEmpty() && !giftStaffId.equals("-1")){
				extraCond.setStaffId(Integer.parseInt(giftStaffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<GiftIncomeByStaff> giftList = CalcGiftStatisticsDao.calcGiftIncomeByStaff(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(giftList);
			
		}catch(SQLException e){
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
	
	public ActionForward getGiftDeptChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String region = request.getParameter("region");
		String foodName = request.getParameter("foodName");
		String giftStaffId = request.getParameter("giftStaffId");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jobject = new JObject();
		
		try{
			CalcGiftStatisticsDao.ExtraCond extraCond = new CalcGiftStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(region != null && !region.equals("-1")){
				extraCond.setRegionId(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			if(foodName != null && !foodName.trim().isEmpty()){
				extraCond.setFoodName(foodName);
			}
			
			if(giftStaffId != null && !giftStaffId.isEmpty() && !giftStaffId.equals("-1")){
				extraCond.setStaffId(Integer.parseInt(giftStaffId));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			List<GiftIncomeByDept> giftList = CalcGiftStatisticsDao.calcGiftIncomeByDept(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(giftList);
			
		}catch(SQLException e){
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
	
	public ActionForward getCancelReasonChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
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
			
			List<CancelIncomeByReason> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByReason(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
	
	public ActionForward getCancelStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
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
			
			List<CancelIncomeByStaff> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByStaff(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
	
	/**
	 * 获取退菜菜品
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward queryCancelFoods(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jobject = new JObject();
		
		try{
			CalcCancelStatisticsDao.ExtraCond extraCond = new CalcCancelStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			
			List<CancelIncomeByFood> cancelList = CalcCancelStatisticsDao.calcCancelIncomeByFood(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
	
	public ActionForward getRepaidStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jobject = new JObject();
		
		try{
			CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<RepaidIncomeByStaff> cancelList = CalcRepaidStatisticsDao.calcRepaidIncomeByStaff(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
	public ActionForward getCommissionStaffChart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String staffID = request.getParameter("staffID");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String openId = request.getParameter("oid");
		
		int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);		
		
		JObject jobject = new JObject();
		
		try{
			CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
			if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
				extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
			}
			if(staffID != null && !staffID.isEmpty() && !staffID.equals("-1")){
				extraCond.setStaffId(Integer.valueOf(staffID));
			}
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			List<CommissionIncomeByStaff> cancelList = CalcCommissionStatisticsDao.calcCommissionIncomeByStaff(staff, new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(cancelList);
			
		}catch(SQLException e){
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
