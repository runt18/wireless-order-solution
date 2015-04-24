package com.wireless.Actions.staffMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.staffMgr.Staff.UpdateBuilder;

public class UpdateStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		JObject jobject = new JObject();
		try {
			// get parameter
			
			String pin = (String)request.getAttribute("pin");
			
			String staffId = request.getParameter("staffId");
			String staffName = request.getParameter("staffName");
			String oldPwd = request.getParameter("oldPwd");
			String staffPwd = request.getParameter("staffPwd");
			
			String roleId = request.getParameter("roleId");
			
			if(oldPwd != null && !oldPwd.trim().isEmpty()){
				Staff staff = StaffDao.verify(Integer.parseInt(staffId));
				if(!staff.getPwd().equals(oldPwd)){
					throw new BusinessException(StaffError.VERIFY_PWD);
				}
			}
			Staff.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(staffId))
										.setStaffPwd(staffPwd);
			
			
			if(roleId != null && !roleId.isEmpty()){
				builder.setRoleId(Integer.parseInt(roleId));
			}
			
			if(staffName != null && !staffName.isEmpty()){
				builder.setStaffName(staffName);
			}
			
			StaffDao.update(StaffDao.verify(Integer.parseInt(pin)),builder);
			
			jobject.initTip(true, "修改成功");
			
		}catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);

		}catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);

		}finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
