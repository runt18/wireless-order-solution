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

import com.wireless.db.billStatistics.CalcGiftStatisticsDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByEachDay;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.DataPaging;

public class QueryGiftStatisticAction extends DispatchAction{

	/**
	 * 获取赠送明细数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String region = request.getParameter("region");
		final String foodName = request.getParameter("foodName");
		final String giftStaffId = request.getParameter("giftStaffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jobject = new JObject();
		try{
			
			final OrderFoodDao.ExtraCond extraCond = new OrderFoodDao.ExtraCond(DateType.HISTORY)
																	 .setDutyRange(new DutyRange(onDuty, offDuty))
																	 .setCalcByDuty(true)
																	 .setGift(true);
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
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
			
			List<OrderFood> orderFoodList = OrderFoodDao.getSingleDetail(staff, extraCond, null);
			
			
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jobject.setTotalProperty(orderFoodList.size());
				OrderFood total = new OrderFood();
				for(OrderFood item : orderFoodList){
					total.setCount(item.getCount() + total.getCount());
					total.setPlanPrice(item.getPlanPrice() + total.getPlanPrice());
					total.asFood().setPrice(item.asFood().getPrice() + total.asFood().getPrice());
				}
				
				orderFoodList = DataPaging.getPagingData(orderFoodList, true, Integer.parseInt(start), Integer.parseInt(limit));
				orderFoodList.add(total);
			}
			jobject.setRoot(orderFoodList);
			
		}catch(BusinessException | SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	/**
	 * 获取赠送走势图（按数量）
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
		final String region = request.getParameter("region");
		final String foodName = request.getParameter("foodName");
		final String giftStaffId = request.getParameter("giftStaffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{

			final CalcGiftStatisticsDao.ExtraCond extraCond = new CalcGiftStatisticsDao.ExtraCond(DateType.HISTORY)
																					   .setDutyRange(new DutyRange(dateBeg, dateEnd))
																					   .setCalcByDuty(true);
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
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
			
			final List<GiftIncomeByEachDay> giftList = CalcGiftStatisticsDao.calcIncomeByEachDay(staff, extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (GiftIncomeByEachDay c : giftList) {
				xAxis.add("\'" + c.getRange().getOnDutyFormat() + "\'");
				data.add(c.getGiftPrice());
				amountData.add(c.getGiftAmount());
				
				totalMoney += c.getGiftPrice();
				totalCount += c.getGiftAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + NumericUtil.roundFloat(totalMoney) + ",\"avgMoney\" : " + Math.round((totalMoney/giftList.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/giftList.size())*100)/100 + 
					",\"ser\":[{\"name\":\'赠送金额\', \"data\" : " + data + "}, {\"name\":\'赠送数量\', \"data\" : " + amountData + "}]}";
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
	 * 获取赠送走势图（按员工）
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
		final String region = request.getParameter("region");
		final String foodName = request.getParameter("foodName");
		final String giftStaffId = request.getParameter("giftStaffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			
			final CalcGiftStatisticsDao.ExtraCond extraCond = new CalcGiftStatisticsDao.ExtraCond(DateType.HISTORY)
																						.setCalcByDuty(true)
																						.setDutyRange(new DutyRange(dateBeg, dateEnd));

			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
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
			
			jObject.setRoot(CalcGiftStatisticsDao.calcIncomeByStaff(staff, extraCond));
			
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
	 * 获取赠送走势图（按部门）
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
		final String region = request.getParameter("region");
		final String foodName = request.getParameter("foodName");
		final String giftStaffId = request.getParameter("giftStaffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		
		try{
			final CalcGiftStatisticsDao.ExtraCond extraCond = new CalcGiftStatisticsDao.ExtraCond(DateType.HISTORY)
																					   .setCalcByDuty(true)
																					   .setDutyRange(new DutyRange(dateBeg, dateEnd));
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				if(Integer.parseInt(branchId) > 0){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}else{
					extraCond.setChain(true);
				}
			}
			
			
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
			
			jObject.setRoot(CalcGiftStatisticsDao.calcIncomeByDept(staff, extraCond));
			
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
}
