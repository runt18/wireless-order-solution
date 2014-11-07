package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.distMgr.Discount;

public class OperateDiscountAction extends DispatchAction{

	public ActionForward setDiscount(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws BusinessException, Exception{
		String orderId = request.getParameter("orderId");
		String discountId = request.getParameter("discountId");
		String memberId = request.getParameter("memberId");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try {
			Order.DiscountBuilder builder = new Order.DiscountBuilder(Integer.parseInt(orderId), Integer.parseInt(discountId));
			if(memberId != null && !memberId.isEmpty()){
				builder.setMember(Integer.parseInt(memberId));
			}
			OrderDao.discount(StaffDao.verify(Integer.parseInt(pin)), builder);
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		
		return null;
	}
	
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			
			String discountName = request.getParameter("discountName");
			String rate = request.getParameter("rate");
			String isDefault = request.getParameter("isDefault");
			
			Discount.InsertBuilder builder = new Discount.InsertBuilder(discountName)
												.setRate(Float.valueOf(rate));
			
			if(isDefault != null && !isDefault.isEmpty()){
				builder.setDefault();
			}
			
			DiscountDao.insert(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true,  "操作成功, 已添加新折扣方案!");
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			
			String discountID = request.getParameter("discountID");
			String discountName = request.getParameter("discountName");
			String isDefault = request.getParameter("isDefault");
			
			Discount.UpdateBuilder builder = new Discount.UpdateBuilder(Integer.valueOf(discountID))
													.setName(discountName.trim());
			if(isDefault != null && !isDefault.isEmpty()){
				builder.setDefault();
			}
			
			DiscountDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true,  "操作成功, 已修改折扣方案!");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}	
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getAttribute("pin");
			String discountID = request.getParameter("discountID");
			
			DiscountDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(discountID));
			
			jobject.initTip(true, "操作成功, 已删除选中方案信息.");
			
		}catch(BusinessException e){
			jobject.initTip(e);
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
