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
import com.wireless.pojo.billStatistics.gift.GiftIncomeByDept;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByEachDay;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByStaff;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class QueryGiftStatisticAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		String region = request.getParameter("region");
		String foodName = request.getParameter("foodName");
		String giftStaffId = request.getParameter("giftStaffId");
		
		JObject jobject = new JObject();
		try{
			OrderFoodDao.ExtraCond extraCond = new OrderFoodDao.ExtraCond(DateType.HISTORY);
			
			extraCond.setDutyRange(new DutyRange(onDuty, offDuty));
			
			extraCond.setGift(true);
			
			if(region != null && !region.equals("-1")){
				extraCond.setRegionId(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			if(foodName != null && !foodName.trim().isEmpty()){
				extraCond.setFoodName(foodName);
			}
			
			if(giftStaffId != null && !giftStaffId.isEmpty() && !giftStaffId.equals("-1")){
				extraCond.setStaffId(Integer.parseInt(giftStaffId));
			}
			
			List<OrderFood> orderFoodList = OrderFoodDao.getSingleDetail(StaffDao.verify(Integer.parseInt(pin)), extraCond, null);
			
			jobject.setTotalProperty(orderFoodList.size());
			
			if(start != null){
				orderFoodList = DataPaging.getPagingData(orderFoodList, true, Integer.parseInt(start), Integer.parseInt(limit));
			}
			jobject.setRoot(orderFoodList);
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
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
		String region = request.getParameter("region");
		String foodName = request.getParameter("foodName");
		String giftStaffId = request.getParameter("giftStaffId");
		
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
			
			List<GiftIncomeByEachDay> giftList = CalcGiftStatisticsDao.calcGiftIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			List<Float> amountData = new ArrayList<Float>();
			float totalMoney = 0, totalCount = 0;
			for (GiftIncomeByEachDay c : giftList) {
				xAxis.add("\'"+c.getRange().getOffDutyFormat()+"\'");
				data.add(c.getGiftPrice());
				amountData.add(c.getGiftAmount());
				
				totalMoney += c.getGiftPrice();
				totalCount += c.getGiftAmount();
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/giftList.size())*100)/100 + ",\"avgCount\" : " + Math.round((totalCount/giftList.size())*100)/100 + 
					",\"ser\":[{\"name\":\'赠送金额\', \"data\" : " + data + "}, {\"name\":\'赠送数量\', \"data\" : " + amountData + "}]}";
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
		String region = request.getParameter("region");
		String foodName = request.getParameter("foodName");
		String giftStaffId = request.getParameter("giftStaffId");
		
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
			
			List<GiftIncomeByStaff> giftList = CalcGiftStatisticsDao.calcGiftIncomeByStaff(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(giftList);
			
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
		String region = request.getParameter("region");
		String foodName = request.getParameter("foodName");
		String giftStaffId = request.getParameter("giftStaffId");
		
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
			
			List<GiftIncomeByDept> giftList = CalcGiftStatisticsDao.calcGiftIncomeByDept(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(dateBeg, dateEnd), extraCond);
			
			jobject.setRoot(giftList);
			
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