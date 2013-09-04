package com.wireless.Actions.roleMgr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.PrivilegeDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class QueryPrivilegeAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Privilege> root = null;
		String pin = (String) request.getAttribute("pin");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			root = PrivilegeDao.getPrivileges(staff, null, null);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
				
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		String pin = (String) request.getAttribute("pin");
		StringBuilder tree = new StringBuilder();
		List<Privilege> root = null;
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			root = PrivilegeDao.getPrivileges(staff, null, " AND status != " + Discount.Status.MEMBER_TYPE.getVal());
			if(root.size() > 0){
				tree.append("[");
				for (int i = 0; i < root.size(); i++) {
					if(i > 0){
						tree.append(",");
					}
					tree.append("{");
					if(root.get(i).getCode() == Code.DISCOUNT){
						tree.append("leaf:false");
						StringBuilder children = new StringBuilder();
						
						for (int j = 0; j < root.get(i).getDiscounts().size(); j++) {
							if(j>0){
								children.append(",");
							}
							children.append("{");
							children.append("leaf:true");
							children.append(",checked:false");
							children.append(",text:'" + root.get(i).getDiscounts().get(j).getName() + "'");
							children.append(",discountId:'" + root.get(i).getDiscounts().get(j).getId() + "'");
							children.append(",isDiscount:true");
							children.append("}");
						}
						tree.append(",children : [" + children.toString() + "]");
					}else{
						tree.append("leaf:true");
					}
					tree.append(",checked:false");
					
					tree.append(",text:'" + root.get(i).getCode().getDesc() + "'");
					tree.append(",pId:'" + root.get(i).getId() + "'");
					tree.append("}");
				}
				tree.append("]");
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
		
	}
	
	public ActionForward roleTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		String pin = (String) request.getAttribute("pin");
		String roleId = request.getParameter("roldId");
		StringBuilder tree = new StringBuilder();
		List<Privilege> root = null;
		List<Privilege> rolePrivilege = new ArrayList<Privilege>();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Role role = RoleDao.getRoleById(staff, Integer.parseInt(roleId));
			if(role != null){
				rolePrivilege = role.getPrivileges();
			}
			//获取所有权限和不是会员类型的所有折扣
			root = PrivilegeDao.getPrivileges(staff, null, " AND status != " + Discount.Status.MEMBER_TYPE.getVal());
			if(root.size() > 0){
				tree.append("[");
				for (int i = 0; i < root.size(); i++) {
					if(i > 0){
						tree.append(",");
					}
					tree.append("{");

					int index = rolePrivilege.indexOf(root.get(i));
					if(index >= 0){
						if(root.get(i).getCode() == Code.DISCOUNT){
							tree.append("leaf:false");
							StringBuilder children = new StringBuilder();
							
							for (int j = 0; j < root.get(i).getDiscounts().size(); j++) {
								if(j>0){
									children.append(",");
								}
								children.append("{");
								children.append("leaf:true");
								children.append(",text:'" + root.get(i).getDiscounts().get(j).getName() + "'");
								int disIndex = rolePrivilege.get(index).getDiscounts().indexOf(root.get(i).getDiscounts().get(j));
								if(disIndex >= 0){
									children.append(",checked:true");
								}else{
									children.append(",checked:false");
								}
								children.append(",isDiscount:true");
								children.append(",discountId:'" + root.get(i).getDiscounts().get(j).getId() + "'");
								children.append("}");
							}
							tree.append(",children : [" + children.toString() + "]");
						}else{
							tree.append("leaf:true");
						}
						tree.append(",checked:true");
					}else{
						//如果选中的角色没有此权限,则前面的checkbox不勾选
						if(root.get(i).getCode() == Code.DISCOUNT){
							tree.append("leaf:false");
							StringBuilder children = new StringBuilder();
							
							for (int j = 0; j < root.get(i).getDiscounts().size(); j++) {
								if(j>0){
									children.append(",");
								}
								children.append("{");
								children.append("leaf:true");
								children.append(",text:'" + root.get(i).getDiscounts().get(j).getName() + "'");
								children.append(",checked:false");
								children.append(",discountId:'" + root.get(i).getDiscounts().get(j).getId() + "'");
								children.append(",isDiscount:true");
								children.append("}");
							}
							tree.append(",children : [" + children.toString() + "]");
						}else{
							tree.append("leaf:true");
						}
						tree.append(",checked:false");
					}
					
					
					tree.append(",text:'" + root.get(i).getCode().getDesc() + "'");
					tree.append(",pId:'" + root.get(i).getId() + "'");
					tree.append("}");
				}
				tree.append("]");
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
		
	}
}
