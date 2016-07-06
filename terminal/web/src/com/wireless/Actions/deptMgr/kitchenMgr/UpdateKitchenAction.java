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
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.Kitchen.MoveBuilder;
import com.wireless.pojo.menuMgr.Kitchen.UpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class UpdateKitchenAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			final String kitchenID = request.getParameter("kitchenID");
			final String kitchenName = request.getParameter("kitchenName");
			final String deptID = request.getParameter("deptID");
			final String isAllowTemp = request.getParameter("isAllowTemp");
			final String pin = (String) request.getAttribute("pin");
			final String move = request.getParameter("move");
			final String kitchenB = request.getParameter("kitchenB");
			final String branchId = request.getParameter("branchId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			KitchenDao.update(staff, 
							 new UpdateBuilder(Integer.valueOf(kitchenID))
									.setName(kitchenName)
									.setDeptId(Department.DeptId.valueOf(Integer.parseInt(deptID)))
									.setAllowTmp(Boolean.parseBoolean(isAllowTemp)));
			
			if(move != null && !move.isEmpty()){
				Kitchen.MoveBuilder builder = new MoveBuilder(Integer.parseInt(kitchenID), Integer.parseInt(kitchenB));
				KitchenDao.move(StaffDao.verify(Integer.parseInt(pin)), builder);
			}
			
			jobject.initTip(true, "操作成功,已修改厨房信息.");
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

}
