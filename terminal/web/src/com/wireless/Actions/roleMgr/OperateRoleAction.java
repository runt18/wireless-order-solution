package com.wireless.Actions.roleMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Role.InsertBuilder;
import com.wireless.pojo.staffMgr.Role.UpdateBuilder;
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
				Role model = RoleDao.getyById(staff, Integer.parseInt(modelId));
				builder.getPrivileges().addAll(model.getPrivileges());
			}

			
			RoleDao.insert(staff, builder);
			
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
			
			UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(roleId))
										.setName(roleName);
		
			RoleDao.update(staff, builder);
			jobject.initTip(true, "修改成功");
			
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
		String pricePlans = request.getParameter("pricePlans");
		JObject jobject = new JObject();
		
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
			Role.UpdateBuilder updateBuilder = new Role.UpdateBuilder(Integer.parseInt(roleId));
			
			if(privileges != null){
				String[] privilegeArray = privileges.split(",");
				for (String string : privilegeArray) {
					if(!string.isEmpty()){
						updateBuilder.addPrivilege(Code.valueOf(Integer.parseInt(string)));
					}
				}
			}
			
			if(discounts != null && !discounts.trim().isEmpty()){
				String[] discountArray = discounts.split(",");
				for (String distId : discountArray) {
					updateBuilder.addDiscount(DiscountDao.getById(staff, Integer.parseInt(distId)));
				}
			}
			
			if(pricePlans != null && !pricePlans.isEmpty()){
				String[] pricePlanArray = pricePlans.split(",");
				for (String planId : pricePlanArray) {
					updateBuilder.addPricePlan(new PricePlan(Integer.parseInt(planId)));
				}				
			}
			
			RoleDao.update(staff, updateBuilder);
			jobject.initTip(true, "修改成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
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
			List<Staff> list = StaffDao.getByRestaurant(staff.getRestaurantId());
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
