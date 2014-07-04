package com.wireless.Actions.billStatistics;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.DutyRangeDao;
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
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;
import com.wireless.util.WebParams;

public class BusinessStatisticsAction extends DispatchAction {
	
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
		
		JObject jObject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String dutyRange = request.getParameter("dutyRange");
			
			String opening = request.getParameter("opening");
			String ending = request.getParameter("ending");
			
			String region = request.getParameter("region");
			
			final String chart = request.getParameter("chart");
			
			CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY); 
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			if(region != null && !region.equals("-1")){
				extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			List<IncomeByEachDay> incomesByEachDay;
			String chartData = null ;
			if(chart != null && !chart.isEmpty()){
				incomesByEachDay = new ArrayList<IncomeByEachDay>();
				
				incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), extraCond));
				
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
				DutyRange range = DutyRangeDao.exec(staff, onDuty, offDuty);
				
				if(range != null){
					shiftDetail = ShiftDao.getByRange(staff, range, extraCond);
				}else{
					shiftDetail = new ShiftDetail(new DutyRange(onDuty, offDuty));
				}
			}else{
				shiftDetail = ShiftDao.getByRange(staff, new DutyRange(onDuty, offDuty), extraCond);
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
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
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
		
		
		
		JObject jObject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			final ShiftDetail sdetail = ShiftDao.getByRange(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), new CalcBillStatisticsDao.ExtraCond(DateType.TODAY));
			
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
				jObject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
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
				jObject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
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
				jObject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}
