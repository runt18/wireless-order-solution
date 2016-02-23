package com.wireless.Actions.login;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class QueryStaffAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String restaurantId = request.getParameter("restaurantID");
		final String branchId = request.getParameter("branchId");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String isPaging = request.getParameter("isPaging");
		final String isName =request.getParameter("isName");
		final String name = request.getParameter("name");
		final String cate = request.getParameter("cate");
		final String privileges = request.getParameter("privileges");
		final String checkPrivilege = request.getParameter("checkPrivilege");
		final String hasDetail = request.getParameter("hasDetail");
		
		final JObject jObject = new JObject();

		final JsonMap extra = new JsonMap();
		
		try {
			
			Staff staff;
			if(restaurantId != null && !restaurantId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
			}else{
				staff = StaffDao.verify(Integer.parseInt(pin));
				if(branchId != null && !branchId.isEmpty()){
					staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				}
			}
			
			if(isName != null){
				extra.putJsonable("staff", staff, Staff.ST_PARCELABLE_SIMPLE);
				extra.putJsonable("restaurant", RestaurantDao.getById(staff.getRestaurantId()), Restaurant.RESTAURANT_PARCELABLE_SIMPLE);
				
			}else {
				
				final StaffDao.ExtraCond extraCond = new StaffDao.ExtraCond();
				if(name != null && !name.trim().isEmpty()){
					extraCond.setName(name);
					
				}else if(cate != null && !cate.trim().isEmpty()){
					extraCond.setRole(Integer.parseInt(cate));
					
				}else if(privileges != null && !privileges.isEmpty()){
					for (String pc : privileges.split(",")) {
						extraCond.addPrivilegeCode(Privilege.Code.valueOf(Integer.parseInt(pc)));
					}
				}
				
				List<Staff> staffList = StaffDao.getByCond(staff, extraCond);
				
				if(staffList.contains(staff)){
					extra.putBoolean("havePrivileges", true);
				}
				
				if(checkPrivilege != null && !checkPrivilege.isEmpty()){
					staffList.clear();
				}
				
				extra.putJsonable("restaurant", RestaurantDao.getById(staff.getRestaurantId()), Restaurant.RESTAURANT_PARCELABLE_COMPLEX);					
				jObject.setTotalProperty(staffList.size());
				
				if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
					staffList = DataPaging.getPagingData(staffList, isPaging, Integer.parseInt(start), Integer.parseInt(limit));
				}
				jObject.setRoot(staffList);
			}

			jObject.setMsg("normal");
			

		}catch (BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);

		} finally {
			jObject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					return extra;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			response.getWriter().write(jObject.toString(hasDetail != null ? Staff.ST_PARCELABLE_COMPLEX : Staff.ST_PARCELABLE_SIMPLE));
		}

		return null;
	}
}
