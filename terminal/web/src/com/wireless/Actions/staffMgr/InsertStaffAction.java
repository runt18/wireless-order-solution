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
import com.wireless.util.WebParams;

public class InsertStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		JObject jobject = new JObject();
		try {
			// 解决后台中文传到前台乱码
			

			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			// get the query condition
			String staffName = request.getParameter("staffName");
			String staffPwd = request.getParameter("staffPwd");
			String roleId = request.getParameter("roleId");
			String tele = request.getParameter("tele");
			
			InsertBuilder builder = new InsertBuilder(staffName, staffPwd, new Role(Integer.parseInt(roleId))).setMobile(tele);
			
			StaffDao.insert(staff, builder);
			jobject.initTip(true, "添加成功");

		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);

		}finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
