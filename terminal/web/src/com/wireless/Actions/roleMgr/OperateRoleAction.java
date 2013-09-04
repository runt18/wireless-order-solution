package com.wireless.Actions.roleMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.PrivilegeDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Role.Category;
import com.wireless.pojo.staffMgr.Role.InsertBuilder;
import com.wireless.pojo.staffMgr.Role.UpdateRoleBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperateRoleAction extends DispatchAction{
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, BusinessException{
		response.setCharacterEncoding("UTF-8");
		String pin = (String) request.getAttribute("pin");
		String roleName = request.getParameter("roleName");
		String roleCate = request.getParameter("roleCate");
		String modelId = request.getParameter("modelId");
		JObject jobject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			InsertBuilder builder = new InsertBuilder();
			builder.setCategoty(Category.valueOf(Integer.parseInt(roleCate)));
			builder.setName(roleName);
			builder.setRestaurantId(staff.getRestaurantId());
			
			if(modelId != null && !modelId.trim().isEmpty()){
				Role model = RoleDao.getRoleById(staff, Integer.parseInt(modelId));
				builder.getPrivileges().addAll(model.getPrivileges());
			}

			
			RoleDao.insertRole(staff, builder);
			
			jobject.initTip(true, "添加成功");
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, BusinessException{
		response.setCharacterEncoding("UTF-8");
		String pin = (String) request.getAttribute("pin");
		String roleName = request.getParameter("roleName");
		String roleId = request.getParameter("roleId");

		JObject jobject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			UpdateRoleBuilder builder = new UpdateRoleBuilder();
			builder.setRoleId(Integer.parseInt(roleId));
			builder.setName(roleName);
		
			RoleDao.updateRole(staff, builder.build());
			jobject.initTip(true, "添加成功");
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	public ActionForward updatePrivilege(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, BusinessException{
		response.setCharacterEncoding("UTF-8");
		String pin = (String) request.getAttribute("pin");
		String roleId = request.getParameter("roleId");
		String privileges = request.getParameter("privileges");
		String discounts = request.getParameter("discounts");
		JObject jobject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
			List<Privilege> privilegeList = PrivilegeDao.getPrivileges(staff, null, null);
			List<String> newPId = new ArrayList<String>(); ;
			
			Role role = RoleDao.getRoleById(staff, Integer.parseInt(roleId));
			role.clearPrivilege();
			if(privileges != null){
				String[] privilegeArray = privileges.split(",");
				for (String string : privilegeArray) {
					newPId.add(string);
				}
				if(discounts != null && !discounts.trim().isEmpty()){
					for (Privilege privilege : privilegeList) {
						if(privilege.getCode() == Code.DISCOUNT){
							if( newPId.indexOf(privilege.getId()+"") < 0){
								newPId.add(privilege.getId()+"");
							}
						}
					}
				}
				for (String pID : newPId) {
					
					int index = privilegeList.indexOf(new Privilege(Integer.parseInt(pID)));
					if(index >= 0){

						if(privilegeList.get(index).getCode() == Code.DISCOUNT){
							privilegeList.get(index).setAllDiscount();
							String[] discountArray = discounts.split(",");
							for (String dID : discountArray) {
								Discount disc = DiscountDao.getPureDiscount(staff, " AND DIST.discount_id = " + dID, null).get(0);
								privilegeList.get(index).addDiscount(disc);
							}
						}
						role.addPrivilege(privilegeList.get(index));
					}
				}
			}

			
			RoleDao.updateRole(staff, role);
			jobject.initTip(true, "修改成功");
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		String roleId = request.getParameter("roleId");
		JObject jobject = new JObject();
		try{
			RoleDao.deleteRole(Integer.parseInt(roleId));
			jobject.initTip(true, "删除成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
}
