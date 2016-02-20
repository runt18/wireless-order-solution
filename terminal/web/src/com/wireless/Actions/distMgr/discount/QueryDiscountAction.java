package com.wireless.Actions.distMgr.discount;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

public class QueryDiscountAction extends DispatchAction{
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward role(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String) request.getAttribute("pin")));
			Role role = new Role(Integer.valueOf(staff.getRole().getId()));
			List<Discount> root = DiscountDao.getByRole(staff, role);
			jObject.setRoot(root);
			jObject.setTotalProperty(root.size());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByMemberType(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String memberType = request.getParameter("memberTypeId");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String) request.getAttribute("pin")));
			List<Discount> root = DiscountDao.getByMemberType(staff, new MemberType(Integer.parseInt(memberType)));
			jObject.setRoot(root);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final StringBuffer tsb = new StringBuffer();
		try{
			
			int i = 0;
			for(Discount discount : DiscountDao.getPureAll(StaffDao.getById(Integer.parseInt(pin)))){
				tsb.append(i > 0 ? "," : "");
				tsb.append("{");
				tsb.append("leaf:true");
				tsb.append(",");
				tsb.append("text:'" + discount.getName() + "'" );
				tsb.append(",");
				tsb.append("discountID:" + discount.getId());
				tsb.append(",");
				tsb.append("discountName:'" + discount.getName() + "'");
				tsb.append(",");
				tsb.append("restaurantID:" + discount.getRestaurantId());
				tsb.append(",");
				tsb.append("status:" + discount.getStatus().getVal());
				tsb.append(",type:" + discount.getType().getVal());
				tsb.append("}");
				i++;
			}
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + tsb.toString() + "]");
		}
		return null;
	}
	
}
