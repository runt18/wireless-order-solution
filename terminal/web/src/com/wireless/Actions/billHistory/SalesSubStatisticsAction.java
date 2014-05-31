package com.wireless.Actions.billHistory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond;
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
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class SalesSubStatisticsAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		List<SalesDetail> salesDetailList = null;
		String isPaging = request.getParameter("isPaging");
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		JObject jobject = new JObject();
		String dataType = request.getParameter("dataType");
		String queryType = request.getParameter("queryType");
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
			String pin = (String)request.getAttribute("pin");
			String restaurantId = (String)request.getAttribute("restaurantID");		
			String dateBeg = request.getParameter("dateBeg");
			String dateEnd = request.getParameter("dateEnd");
			
			String businessHourBeg = request.getParameter("opening");
			String businessHourEnd = request.getParameter("ending");
			String orderType = request.getParameter("orderType");
			String deptID = request.getParameter("deptID");
			String foodName = request.getParameter("foodName");
			String region = request.getParameter("region");
			
			
			
			pin = pin != null && pin.length() > 0 ? pin.trim() : "";
			restaurantId = restaurantId != null && restaurantId.length() > 0 ? restaurantId.trim() : "";
			dataType = dataType != null && dataType.length() > 0 ? dataType.trim() : "1";
			queryType = queryType != null && queryType.length() > 0 ? queryType.trim() : "0";
			orderType = orderType != null && orderType.length() > 0 ? orderType.trim() : "1";
			deptID = deptID != null && deptID.length() > 0 ? deptID.trim() : "-1";
			
			Integer qt = Integer.valueOf(queryType), ot = (orderType != null && !orderType.isEmpty()) ? Integer.parseInt(orderType) : SaleDetailsDao.ORDER_BY_SALES;
			DateType dt = DateType.valueOf(Integer.valueOf(dataType));
			
			ExtraCond extraConds = new ExtraCond(dt);
				
			if(dt.isHistory()){
				dateBeg = dateBeg != null && dateBeg.length() > 0 ? dateBeg.trim() + " 00:00:00" : "";
				dateEnd = dateEnd != null && dateEnd.length() > 0 ? dateEnd.trim() + " 23:59:59" : "";
			}
			
			DutyRange dutyRange = new DutyRange(dateBeg, dateEnd);
			
			if(region != null && !region.equals("-1")){
				extraConds.setRegion(RegionId.valueOf(Integer.parseInt(region)));
				
			}
			
			if(businessHourBeg != null && !businessHourBeg.isEmpty()){
				HourRange hr = new HourRange(businessHourBeg, businessHourEnd, DateUtil.Pattern.HOUR);
				extraConds.setHourRange(hr);
			}
			if(qt == SaleDetailsDao.QUERY_BY_DEPT){
				
				salesDetailList = SaleDetailsDao.execByDept(
						StaffDao.verify(Integer.parseInt(pin)), 
						dutyRange,
						extraConds);
				
			}else if(qt == SaleDetailsDao.QUERY_BY_FOOD){
				extraConds.setFoodName(foodName);
				if(deptID != null && !deptID.equals("-1")){
					extraConds.setDept(Department.DeptId.valueOf(Integer.parseInt(deptID)));
				}
				salesDetailList = SaleDetailsDao.getByFood(StaffDao.verify(Integer.parseInt(pin)), dutyRange, extraConds, ot);
			}else if(qt == SaleDetailsDao.QUERY_BY_KITCHEN){
				
				salesDetailList = SaleDetailsDao.getByKitchen(
						StaffDao.verify(Integer.parseInt(pin)), 
						dutyRange,
						extraConds);
			}
				
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		} finally{
			jobject.setTotalProperty(salesDetailList.size());
			
			SalesDetail sum = null;
			if(queryType != null && !(Integer.valueOf(queryType) == SaleDetailsDao.QUERY_BY_KITCHEN) && !salesDetailList.isEmpty()){
				sum = new SalesDetail();
				Food fb = new Food(0);
				fb.setName("汇总");
				sum.setFood(fb);
				Department dept = new Department(0);
				dept.setName("汇总");
				sum.setDept(dept);
				for(SalesDetail tp : salesDetailList){
					sum.setIncome(sum.getIncome() + tp.getIncome());
					sum.setDiscount(sum.getDiscount() + tp.getDiscount());
					sum.setGifted(sum.getGifted() + tp.getGifted());
					sum.setCost(sum.getCost() + tp.getCost());
					sum.setProfit(sum.getProfit() + tp.getProfit());
					sum.setSalesAmount(sum.getSalesAmount() + tp.getSalesAmount());				
				}
				if(sum.getIncome() != 0.00){
					sum.setProfitRate(sum.getProfit() / sum.getIncome());
					sum.setCostRate(sum.getCost() / sum.getIncome());
				}
				
			}
			salesDetailList = DataPaging.getPagingData(salesDetailList, isPaging, start, limit);
			if(sum != null){
				salesDetailList.add(sum);
			}
			jobject.setRoot(salesDetailList);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
}
