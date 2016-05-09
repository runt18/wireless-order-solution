package com.wireless.Actions.billStatistics;

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
import com.wireless.db.shift.PaymentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class BusinessStatisticsAction extends DispatchAction {
	
	/**
	 * 营业统计
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward history(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		
		final String dutyRange = request.getParameter("dutyRange");
		
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final String region = request.getParameter("region");
		
		final String chart = request.getParameter("chart");
		
		final String branchId = request.getParameter("branchId");
		
		final JObject jObject = new JObject();
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcBillStatisticsDao.ExtraCond extraCond;
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
					extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY);
				}else{
					extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY).setChain(true);
				}
			}else{
				extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY);
			}
			
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			if(region != null && !region.isEmpty() && !region.equals("-1")){
				extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			extraCond.setDutyRange(new DutyRange(onDuty, offDuty));
			
			List<IncomeByEachDay> incomesByEachDay;
			String chartData = null ;
			if(chart != null && !chart.isEmpty()){
				incomesByEachDay = new ArrayList<IncomeByEachDay>();
				
				incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(staff, extraCond));
				
				List<String> xAxis = new ArrayList<String>();
				List<Float> data = new ArrayList<Float>();
				List<Integer> countList = new ArrayList<Integer>();
				float totalMoney = 0, totalCount = 0;
				int count = 0;
				for (IncomeByEachDay e : incomesByEachDay) {
					xAxis.add("\'"+e.getDate()+"\'");
					data.add(e.getIncomeByPay().getTotalActual());
					countList.add(e.getTotalAmount());
					totalMoney += e.getIncomeByPay().getTotalActual();
					totalCount += e.getTotalAmount();
					count ++ ;
				}
				
				chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + ", \"avgCount\" : " + Math.round((totalCount/count)*100)/100 + 
									",\"ser\":[{\"name\":\'营业额\', \"data\" : " + data + "},{\"name\":\'账单数\', \"data\":" + countList + "}]}";				
				
			}
			final String chartDatas = chartData;
			final ShiftDetail shiftDetail;
			if(!dutyRange.equals("null") && !dutyRange.trim().isEmpty()){
				shiftDetail = CalcBillStatisticsDao.calcSalesIncome(staff, extraCond.setDutyRange(new DutyRange(onDuty, offDuty)));
			}else{
				shiftDetail = ShiftDao.getByRange(staff, extraCond.setDutyRange(new DutyRange(onDuty, offDuty)));
			}
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("business", shiftDetail, 0);
					if(chart != null && !chart.isEmpty()){
						jm.putString("businessChart", chartDatas);
					}
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
	 * today
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward today(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		
		final JObject jObject = new JObject();
		try{

			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final ShiftDetail sdetail = ShiftDao.getByRange(staff, new CalcBillStatisticsDao.ExtraCond(DateType.TODAY).setDutyRange(new DutyRange(onDuty, offDuty)));
			
			if(sdetail != null){
				jObject.setExtra(new Jsonable(){
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable("business", sdetail, 0);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
					
				});
			}else{
				jObject.initTip(false, JObject.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
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
	 * payment
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward paymentToday(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jObject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String staffId = request.getParameter("staffId");
			PaymentDao.ExtraCond extraCond = new PaymentDao.ExtraCond(DateType.TODAY);
			
			if(staffId != null && !staffId.isEmpty()){
				extraCond.setStaffId(Integer.parseInt(staffId));
			}
			
			final ShiftDetail sdetail = PaymentDao.getDetail(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), extraCond);
			
			if(sdetail != null){
				jObject.setExtra(new Jsonable(){
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable("business", sdetail, 0);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
					
				});
			}else{
				jObject.initTip(false, JObject.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
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
	 * payment
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward paymentHistory(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jObject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String staffId = request.getParameter("staffId");
			PaymentDao.ExtraCond extraCond = new PaymentDao.ExtraCond(DateType.HISTORY);
			
			if(staffId != null && !staffId.isEmpty()){
				extraCond.setStaffId(Integer.parseInt(staffId));
			}
			
			final ShiftDetail sdetail = PaymentDao.getDetail(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), extraCond);
			
			if(sdetail != null){
				jObject.setExtra(new Jsonable(){
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable("business", sdetail, 0);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
					
				});
			}else{
				jObject.initTip(false, JObject.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
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
