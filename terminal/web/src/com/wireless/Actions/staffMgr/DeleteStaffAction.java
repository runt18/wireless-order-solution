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
import com.wireless.util.WebParams;

public class DeleteStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		try {
			// get the query condition
			int staffId = Integer.parseInt(request.getParameter("staffId"));
			StaffDao.deleteStaff(staffId);
			jobject.initTip(true, "删除成功");

		}  catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);

		}finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
