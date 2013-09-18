package com.wireless.Actions.distMgr.discount;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
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
	public ActionForward role(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String) request.getAttribute("pin")));
			Role role = new Role(Integer.valueOf(request.getParameter("roleId")));
			List<Discount> root = DiscountDao.getDiscountByRole(staff, role);
			jobject.setRoot(root);
			jobject.setTotalProperty(root.size());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		try{
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("");
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
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		DBCon dbCon = new DBCon();
		StringBuffer tsb = new StringBuffer();
		try{
			dbCon.connect();
			String pin = (String)request.getAttribute("pin");
			
			List<Discount> discounts = DiscountDao.getPureDiscount(dbCon, 
					StaffDao.getStaffById(Integer.parseInt(pin)), 
					" AND DIST.status <> " + Discount.Status.MEMBER_TYPE.getVal(), 
					" ORDER BY DIST.level DESC");
			
			int i = 0;
			for(Discount discount : discounts){
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
				tsb.append("level:" + discount.getLevel());
				tsb.append(",");
				tsb.append("restaurantID:" + discount.getRestaurantId());
				tsb.append(",");
				tsb.append("isDefault:" + (discount.isDefault() || discount.isDefaultReserved()));
				tsb.append(",");
				tsb.append("status:" + discount.getStatus().getVal());
				tsb.append("}");
				i++;
			}
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print("[" + tsb.toString() + "]");
		}
		return null;
	}
	
}
