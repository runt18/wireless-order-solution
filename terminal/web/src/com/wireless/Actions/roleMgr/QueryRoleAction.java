package com.wireless.Actions.roleMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

public class QueryRoleAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String pin = (String) request.getAttribute("pin");
		String branchId = request.getParameter("branchId");
		
		List<Role> root = null;
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			root = RoleDao.getRoles(staff, null, " ORDER BY cate");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
}
