package com.wireless.Actions.distMgr.discount;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Staff;

public class OperateDiscountAction extends DispatchAction{

	/**
	 * 获取折扣方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String id = request.getParameter("discountId");
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final DiscountDao.ExtraCond extraCond = new DiscountDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setDiscountId(Integer.parseInt(id));
			}
			
			final List<Discount> root = DiscountDao.getByCond(staff, extraCond, DiscountDao.ShowType.BY_PLAN);
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
	 * 会员注入和设置折扣
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward setDiscount(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final int orderId = Integer.parseInt(request.getParameter("orderId"));
		final String discountId = request.getParameter("discountId");
		final String pricePlanId = request.getParameter("pricePlan");
		final String memberId = request.getParameter("memberId");
		final String pin = (String) request.getAttribute("pin");
		
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		final JObject jObject = new JObject();
		
		final int discount;
		if(discountId != null && !discountId.isEmpty()){
			discount = Integer.parseInt(discountId);
		}else{
			discount = 0;
		}
		
		try {
			final Order.DiscountBuilder builder;
			if(memberId != null && !memberId.isEmpty()){
				final int pricePlan;
				if(pricePlanId != null && !pricePlanId.trim().isEmpty()){
					pricePlan = Integer.parseInt(pricePlanId);
				}else{
					pricePlan = 0;
				}
				builder = Order.DiscountBuilder.build4Member(orderId, MemberDao.getById(staff, Integer.parseInt(memberId)), discount, pricePlan);
			}else{
				builder = Order.DiscountBuilder.build4Normal(orderId, discount);
			}
			OrderDao.discount(staff, builder);
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 新建折扣方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String discountName = request.getParameter("discountName");
		final String rate = request.getParameter("rate");
		final String isDefault = request.getParameter("isDefault");		
		final JObject jObject = new JObject();
		try{
			
			final Discount.InsertBuilder builder = new Discount.InsertBuilder(discountName)
												.setRate(Float.valueOf(rate));
			
			if(isDefault != null && !isDefault.isEmpty()){
				builder.setDefault();
			}
			
			DiscountDao.insert(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jObject.initTip(true,  "操作成功, 已添加新折扣方案!");
			
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
	
	/**
	 * 更新折扣方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String discountId = request.getParameter("discountID");
		final String discountName = request.getParameter("discountName");
		final String isDefault = request.getParameter("isDefault");
		final JObject jObject = new JObject();
		
		try{
			final Discount.UpdateBuilder builder = new Discount.UpdateBuilder(Integer.parseInt(discountId));
			
			if(discountName != null && !discountName.trim().isEmpty()){
				builder.setName(discountName.trim());
			}
			if(isDefault != null && !isDefault.isEmpty()){
				builder.setDefault();
			}
			
			DiscountDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jObject.initTip(true,  "操作成功, 已修改折扣方案!");
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}	
	
	/**
	 * 删除折扣方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String discountId = request.getParameter("discountID");
		final JObject jObject = new JObject();
		try{
			DiscountDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(discountId));
			
			jObject.initTip(true, "操作成功, 已删除选中方案信息.");
			
		}catch(BusinessException e){
			jObject.initTip(e);
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}

}
