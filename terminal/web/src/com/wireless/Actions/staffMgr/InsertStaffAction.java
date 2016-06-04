package com.wireless.Actions.staffMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.staffMgr.Staff.InsertBuilder;

public class InsertStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		
		// get the query condition
		final String staffName = request.getParameter("staffName");
		final String staffPwd = request.getParameter("staffPwd");
		final String roleId = request.getParameter("roleId");
		final String tele = request.getParameter("tele");
		
		final JObject jobject = new JObject();
		try {
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final InsertBuilder builder = new InsertBuilder(staffName, staffPwd, new Role(Integer.parseInt(roleId))).setMobile(tele);
			
			StaffDao.insert(staff, builder);
			jobject.initTip(true, "添加成功");

		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);

		}finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
