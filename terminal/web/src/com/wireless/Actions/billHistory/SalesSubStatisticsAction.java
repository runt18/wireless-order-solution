package com.wireless.Actions.billHistory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.SaleDetailsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class SalesSubStatisticsAction extends Action {
	
	/**
	 * 销售统计
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");

		final String branchId = request.getParameter("branchId");
		
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		
		final String businessHourBeg = request.getParameter("opening");
		final String businessHourEnd = request.getParameter("ending");
		final String orderType = request.getParameter("orderType");
		final String deptID = request.getParameter("deptID");
		final String kitchenID = request.getParameter("kitchenID");
		final String foodName = request.getParameter("foodName");
		final String region = request.getParameter("region");
		final String staffId = request.getParameter("staffId");
		
		final String isPaging = request.getParameter("isPaging");
		final String limit = request.getParameter("limit");
		final String start = request.getParameter("start");
		final JObject jObject = new JObject();
		final String dataType = request.getParameter("dataType");
		final String queryType = request.getParameter("queryType");
		try{
			/**
			 * The parameters looks like below.
			 * 1st example: 按部门查询
			 * pin=0x1 & dateBeg="2012-5-12" & dateEnd="2012-6-12" & queryType=0 & orderType=0 
			 * 
			 * 2nd example: 查询所有菜品
			 * pin=0x1 & dateBeg="2012-5-12" & dateEnd="2012-6-12" & queryType=1 & deptID=-1 & orderType=0 
			 * 
			 * pin : the pin the this terminal
			 * 
			 * dateBeg : the begin date to query
			 * 
			 * dateEnd : the end date to query
			 * 
			 * queryType : "0" means "按部门查询"
			 * 			   "1" means "按菜品查询"
			 * 
			 * deptID : the department id to query in case of "按菜品查询",
			 * 			the value less than means all the foods
			 * 
			 * orderType : "0" means "按毛利排序"
			 * 			   "1" means "按销量排序"
			 * 
			 */
			
			final int qt;
			if(queryType != null && !queryType.isEmpty()){
				qt = Integer.valueOf(queryType);
			}else{
				qt = SaleDetailsDao.QUERY_BY_DEPT;
			}
			
			final int ot;
			if(orderType != null && !orderType.isEmpty()){
				ot = Integer.parseInt(orderType);
			}else{
				ot = SaleDetailsDao.ORDER_BY_SALES;
			}
			
			final DateType dt;
			if(dataType != null && !dataType.isEmpty()){
				dt = DateType.valueOf(Integer.valueOf(dataType));
			}else{
				dt = DateType.HISTORY;
			}
			
			final CalcBillStatisticsDao.ExtraCond extraCond;
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				if(branchId.equals("-1")){
					extraCond = new CalcBillStatisticsDao.ExtraCond(dt).setChain(true);
				}else{
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
					extraCond = new CalcBillStatisticsDao.ExtraCond(dt);
				}
			}else{
				extraCond = new CalcBillStatisticsDao.ExtraCond(dt);
			}
			
				
			if(dt.isHistory()){
				dateBeg = dateBeg != null && dateBeg.length() > 0 ? dateBeg.trim() + " 00:00:00" : "";
				dateEnd = dateEnd != null && dateEnd.length() > 0 ? dateEnd.trim() + " 23:59:59" : "";
			}
			
			final DutyRange dutyRange = new DutyRange(dateBeg, dateEnd);
			
			if(region != null && !region.isEmpty() && !region.equals("-1")){
				extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
			}
			
			if(businessHourBeg != null && !businessHourBeg.isEmpty()){
				HourRange hr = new HourRange(businessHourBeg, businessHourEnd, DateUtil.Pattern.HOUR);
				extraCond.setHourRange(hr);
			}
			
			List<SalesDetail> salesDetailList;
			
			if(qt == SaleDetailsDao.QUERY_BY_DEPT){
				
				salesDetailList = SaleDetailsDao.getByDept(staff, dutyRange, extraCond);
				
			}else if(qt == SaleDetailsDao.QUERY_BY_FOOD){
				if(foodName != null && !foodName.isEmpty()){
					extraCond.setFoodName(foodName);
				}
				if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
					extraCond.setDept(Department.DeptId.valueOf(Integer.parseInt(deptID)));
				}
				
				if(kitchenID != null && !kitchenID.isEmpty() && !kitchenID.equals("-1")){
					extraCond.setKitchen(Integer.parseInt(kitchenID));
				}
				
				if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
					extraCond.setStaffId4OrderFood(Integer.parseInt(staffId));
				}
				salesDetailList = SaleDetailsDao.getByFood(staff, dutyRange, extraCond, ot);
				
			}else if(qt == SaleDetailsDao.QUERY_BY_KITCHEN){
				salesDetailList = SaleDetailsDao.getByKitchen(staff, dutyRange, extraCond);
				
			}else{
				salesDetailList = Collections.emptyList();
			}
			
			jObject.setTotalProperty(salesDetailList.size());
			
			SalesDetail summary = null;
			if(queryType != null && !(Integer.valueOf(queryType) == SaleDetailsDao.QUERY_BY_KITCHEN) && !salesDetailList.isEmpty()){
				summary = new SalesDetail();
				Food fb = new Food(0);
				fb.setName("汇总");
				summary.setFood(fb);
				Department dept = new Department(0);
				dept.setName("汇总");
				summary.setDept(dept);
				for(SalesDetail tp : salesDetailList){
					summary.setIncome(summary.getIncome() + tp.getIncome());
					summary.setTasteIncome(summary.getTasteIncome() + tp.getTasteIncome());
					summary.setDiscount(summary.getDiscount() + tp.getDiscount());
					summary.setGifted(summary.getGifted() + tp.getGifted());
					summary.setGiftAmount(summary.getGiftAmount() + tp.getGiftAmount());
					summary.setCost(summary.getCost() + tp.getCost());
					summary.setProfit(summary.getProfit() + tp.getProfit());
					summary.setSalesAmount(summary.getSalesAmount() + tp.getSalesAmount());			
				}
				if(summary.getIncome() != 0.00){
					summary.setProfitRate(summary.getProfit() / summary.getIncome());
					summary.setCostRate(summary.getCost() / summary.getIncome());
				}
				
			}
			salesDetailList = DataPaging.getPagingData(salesDetailList, Boolean.parseBoolean(isPaging), start, limit);
			if(summary != null){
				salesDetailList.add(summary);
			}
			jObject.setRoot(salesDetailList);
			
		} catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally{

			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
}
