package com.wireless.Actions.orderMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class QueryDetailAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String beginDate = request.getParameter("beginDate");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String endDate = request.getParameter("endDate");
		final String orderID = request.getParameter("orderID");
		final String tableID = request.getParameter("tableID");
		final String deptID = request.getParameter("deptID");
		final String kitchenId = request.getParameter("kitchenID");
		final String queryType = request.getParameter("queryType");
		final String staffId = request.getParameter("staffID");
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String foodId = request.getParameter("foodId");
		final String regionId = request.getParameter("regionId");
		final String calcByDuty = request.getParameter("calcByDuty");
		final JObject jObject = new JObject();

		try{

			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			if(branchId != null && !branchId.isEmpty() && Integer.parseInt(branchId) > 0){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			List<OrderFood> list = null;

			if (queryType.equalsIgnoreCase("TodayByTbl")) {
				list = OrderFoodDao.getSingleDetailByTableId(staff, Integer.parseInt(tableID));
				if(orderID != null && !orderID.trim().isEmpty()){
					final float totalPrice = OrderDao.getById(staff, Integer.parseInt(orderID), DateType.TODAY).calcTotalPrice();
					jObject.setExtra(new Jsonable() {
						
						@Override
						public JsonMap toJsonMap(int flag) {
							JsonMap jm = new JsonMap();
							jm.putFloat("detailTotalPrice", totalPrice);
							return jm;
						}
						
						@Override
						public void fromJsonMap(JsonMap jsonMap, int flag) {
							
						}
					});
				}
			}else{
				final OrderFoodDao.ExtraCond extraCond;
				if (queryType.equalsIgnoreCase("today")) {
					extraCond = new OrderFoodDao.ExtraCond(DateType.TODAY);
				}else{
					extraCond = new OrderFoodDao.ExtraCond(DateType.HISTORY);
				}
				
				if(foodId != null && !foodId.isEmpty()){
					extraCond.setFood(Integer.parseInt(foodId));
				}
				
				if(kitchenId != null && !kitchenId.isEmpty() && Integer.parseInt(kitchenId) > 0){
					extraCond.setKitchen(Integer.parseInt(kitchenId));
				}
				
				if(regionId != null && !regionId.isEmpty() && Integer.parseInt(regionId) > 0){
					extraCond.setRegionId(Region.RegionId.valueOf(Integer.parseInt(regionId)));
				}
				
				if(staffId != null && !staffId.isEmpty() && Integer.parseInt(staffId) > 0){
					extraCond.setStaffId(Integer.parseInt(staffId));
				}
				
				if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
					if(calcByDuty != null && !calcByDuty.isEmpty() && Boolean.parseBoolean(calcByDuty)){
						DutyRange range = DutyRangeDao.exec(staff, beginDate, endDate);
						if(range != null){
							extraCond.setDutyRange(range);
						}else{
							extraCond.setDutyRange(beginDate, endDate);
						}
					}else{
						extraCond.setDutyRange(beginDate, endDate);
					}
				}
				
				if(opening != null && !opening.isEmpty() && ending != null && !ending.isEmpty()){
					HourRange range = new HourRange(opening, ending);
					extraCond.setHourRange(range);
				}
				
				if(deptID != null && !deptID.isEmpty() && !deptID.equals("-1")){
					extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptID)));
				}
				
				if(orderID != null && !orderID.isEmpty()){
					extraCond.setOrder(Integer.parseInt(orderID));
				}
				
				list = OrderFoodDao.getSingleDetail(staff, extraCond, " ORDER BY OF.order_date ");
			}

			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				list = DataPaging.getPagingData(list, Boolean.parseBoolean(isPaging), start, limit);
			}
			jObject.setRoot(list);

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
