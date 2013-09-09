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
import com.wireless.pojo.staffMgr.Staff.StaffUpdateBuilder;
import com.wireless.util.WebParams;

public class UpdateStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		JObject jobject = new JObject();
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");

			// get parameter
			String staffId = request.getParameter("staffId");
			String staffName = request.getParameter("staffName");
			String staffPwd = request.getParameter("staffPwd");
			
			String roleId = request.getParameter("roleId");
			String tele = request.getParameter("tele");
			
			StaffUpdateBuilder builder = new StaffUpdateBuilder(Integer.parseInt(staffId))
											 .setStaffName(staffName)
											 .setStaffPwd(staffPwd)
											 .setMobile(tele)
											 .setRoleId(Integer.parseInt(roleId));
			
			StaffDao.updateStaff(builder);
			
			jobject.initTip(true, "修改成功");
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);

		}finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
