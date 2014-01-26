package com.wireless.Actions.deptMgr.kitchenMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen.UpdateBuilder;

public class UpdateKitchenAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			String kitchenID = request.getParameter("kitchenID");
			String kitchenName = request.getParameter("kitchenName");
			String deptID = request.getParameter("deptID");
			String isAllowTemp = request.getParameter("isAllowTemp");
			String pin = (String) request.getAttribute("pin");
			
			KitchenDao.update(StaffDao.verify(Integer.parseInt(pin)), 
							 new UpdateBuilder(Integer.valueOf(kitchenID))
									.setName(kitchenName)
									.setDeptId(Department.DeptId.valueOf(Integer.parseInt(deptID)))
									.setAllowTmp(Boolean.parseBoolean(isAllowTemp)));
			
			jobject.initTip(true, "操作成功,已修改厨房信息.");
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
