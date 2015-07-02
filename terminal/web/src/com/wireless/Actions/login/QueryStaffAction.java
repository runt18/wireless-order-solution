package com.wireless.Actions.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
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
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
//		response.setContentType("text/json;charset=utf-8");
		PrintWriter out = null;
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isName =request.getParameter("isName");
		String name = request.getParameter("name");
		String cate = request.getParameter("cate");
		String privileges = request.getParameter("privileges");
		String checkPrivilege = request.getParameter("checkPrivilege");
		String hasDetail = request.getParameter("hasDetail");
		
		JObject jobject = new JObject();
		List<Staff> staffList = new ArrayList<Staff>();
		Staff staff = new Staff();
		int index = 0;
		int pageSize = 0;
		if (start != null) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}
		

		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		// 是否combo
		//String isCombo = request.getParameter("isCombo");
		final JsonMap extra = new JsonMap();
		
		try {
			
			out = response.getWriter();
			String restaurantID ;
				
			restaurantID = request.getParameter("restaurantID");
			if(pin != null){
				staff = StaffDao.verify(Integer.parseInt(pin));
			}
			if(isName != null){
				extra.putJsonable("staff", staff, Staff.ST_PARCELABLE_SIMPLE);
				Restaurant restaurant;
				if(request.getSession().getAttribute("restaurantID") == null){
					restaurant = new Restaurant();
				}else{
					restaurant = RestaurantDao.getById(Integer.parseInt((String) request.getSession().getAttribute("restaurantID")));
				}
				extra.putJsonable("restaurant", restaurant, Restaurant.RESTAURANT_PARCELABLE_SIMPLE);
			}else {
				if(name != null && !name.trim().isEmpty()){
					staffList = StaffDao.getByName(staff, name);
				}else if(cate != null && !cate.trim().isEmpty()){
					staffList = StaffDao.getByRole(staff, Integer.parseInt(cate));
				}else if(privileges != null && !privileges.isEmpty()){
					String privilegeCodes[] = privileges.split(",");
					StaffDao.ExtraCond extraCond = new StaffDao.ExtraCond();
					
					for (String pc : privilegeCodes) {
						extraCond.addPrivilegeCode(Privilege.Code.valueOf(Integer.parseInt(pc)));
					}
					
					staffList = StaffDao.getByCond(staff, extraCond);
					
					if(staffList.contains(staff)){
						extra.putBoolean("havePrivileges", true);
					}
					if(checkPrivilege != null && !checkPrivilege.isEmpty()){
						staffList.clear();
					}
				}else{
					if(restaurantID == null || restaurantID.isEmpty()){
						restaurantID = (String) request.getSession().getAttribute("restaurantID");
					}
					
					if(restaurantID != null){
						staffList = StaffDao.getByRestaurant(Integer.parseInt(restaurantID));
					}
				}
				if(restaurantID != null){
					Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(restaurantID));
					extra.putJsonable("restaurant", restaurant, Restaurant.RESTAURANT_PARCELABLE_COMPLEX);					
				}
				jobject.setTotalProperty(staffList.size());
				staffList = DataPaging.getPagingData(staffList, isPaging, index, pageSize);
				jobject.setRoot(staffList);
			}

			jobject.setMsg("normal");
			

		}catch (BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);

		} catch (IOException e) {
			e.printStackTrace();
			jobject.initTip(false, "未处理异常");
		} finally {
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					return extra;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			out.write(jobject.toString(hasDetail != null ? Staff.ST_PARCELABLE_COMPLEX : Staff.ST_PARCELABLE_SIMPLE));
		}

		return null;
	}
}
