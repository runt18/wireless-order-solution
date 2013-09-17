package com.wireless.Actions.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class QueryStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isName =request.getParameter("isName");
		String name = request.getParameter("name");
		String cate = request.getParameter("cate");
		
		JObject jobject = new JObject();
		List<Staff> staffList = new ArrayList<Staff>();
		Staff staff = new Staff();
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}
		

		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		// 是否combo
		//String isCombo = request.getParameter("isCombo");
		Map<Object, Object> other = new HashMap<Object, Object>();
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			
			out = response.getWriter();
			String restaurantID ;
				
			restaurantID = request.getParameter("restaurantID");
			if(pin != null){
				staff = StaffDao.verify(Integer.parseInt(pin));
			}
			if(isName != null){
				other.put("staff", staff);
			}else {
				if(name != null && !name.trim().isEmpty()){
					staffList = StaffDao.getStaffsByName(staff, name);
				}else if(cate != null && !cate.trim().isEmpty()){
					staffList = StaffDao.getStaffsByRoleId(staff, Integer.parseInt(cate));
				}else{
					staffList = StaffDao.getStaffs(Integer.parseInt(restaurantID));
				}
				
				Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(restaurantID));
				other.put("restaurant", restaurant);
				jobject.setTotalProperty(staffList.size());
				staffList = DataPaging.getPagingData(staffList, isPaging, index, pageSize);
				jobject.setRoot(staffList);
				jobject.setOther(other);
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
			jobject.setOther(other);
			out.write(jobject.toString());
		}

		return null;
	}
}
