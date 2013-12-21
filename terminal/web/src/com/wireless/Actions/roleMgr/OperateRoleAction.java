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
import com.wireless.pojo.staffMgr.Role.InsertBuilder;
import com.wireless.pojo.staffMgr.Role.UpdateRoleBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperateRoleAction extends DispatchAction{
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, BusinessException{
		
		String pin = (String) request.getAttribute("pin");
		String roleName = request.getParameter("roleName");
		String modelId = request.getParameter("modelId");
		JObject jobject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			InsertBuilder builder = new InsertBuilder(staff.getRestaurantId(), roleName);
			
			if(modelId != null && !modelId.trim().isEmpty()){
				Role model = RoleDao.getRoleById(staff, Integer.parseInt(modelId));
				builder.getPrivileges().addAll(model.getPrivileges());
			}

			
			RoleDao.insertRole(staff, builder);
			
			jobject.initTip(true, "添加成功");
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_WARNING, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false,  9999, e.getMessage());
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, BusinessException{
		
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
		
		String pin = (String) request.getAttribute("pin");
		String roleId = request.getParameter("roleId");
		String privileges = request.getParameter("privileges");
		String discounts = request.getParameter("discounts");
		String isAllCount = "";
		JObject jobject = new JObject();
		
		
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
			List<Privilege> privilegeList = PrivilegeDao.getPrivileges(staff, null, " AND status != " + Discount.Status.MEMBER_TYPE.getVal());

			
			
			List<String> newPId = new ArrayList<String>(); ;
			
			Role role = RoleDao.getRoleById(staff, Integer.parseInt(roleId));
			role.clearPrivilege();
			if(privileges != null){
				String[] privilegeArray = privileges.split(",");
				for (String string : privilegeArray) {
					if(!string.isEmpty()){
						newPId.add(string);
					}
				}
				if(discounts != null && !discounts.trim().isEmpty()){
					String[] discountArray = discounts.split(",");
					for (Privilege privilege : privilegeList) {
						if(privilege.getCode() == Code.DISCOUNT){
							//判断是否全选了折扣
							if(privilege.getDiscounts().size() == discountArray.length){
								isAllCount = "true";
							}
							//选了部分折扣的情况
							if( newPId.indexOf(privilege.getId()+"") < 0 && isAllCount.trim().isEmpty()){
								newPId.add(privilege.getId()+"");
							}
/*							else if(newPId.indexOf(privilege.getId()+"") > 0 && !isAllCount.trim().isEmpty()){
								newPId.remove(privilege.getId()+"");
							}*/
						}
					}
				}
				for (String pID : newPId) {
					
					int index = privilegeList.indexOf(new Privilege(Integer.parseInt(pID)));
					if(index >= 0){
						if(isAllCount.trim().isEmpty()){
							if(privilegeList.get(index).getCode() == Code.DISCOUNT){
								privilegeList.get(index).setAllDiscount();
								String[] discountArray = discounts.split(",");
								for (String dID : discountArray) {
									Discount disc = DiscountDao.getPureDiscount(staff, " AND DIST.discount_id = " + dID, null).get(0);
									privilegeList.get(index).addDiscount(disc);
								}
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
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage());
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String roleId = request.getParameter("roleId");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<Staff> list = StaffDao.getStaffs(staff.getRestaurantId());
			for (Staff s : list) {
				if((s.getRole().getId()+"").equals(roleId)){
					throw new BusinessException("删除失败, 此角色有员工在使用中");
				}
			}
			
			RoleDao.deleteRole(Integer.parseInt(roleId));
			jobject.initTip(true, "删除成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getMessage());
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
}
