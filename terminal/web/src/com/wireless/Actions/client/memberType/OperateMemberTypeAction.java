package com.wireless.Actions.client.memberType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.client.MemberType.Attribute;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMemberTypeAction extends DispatchAction{
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = request.getParameter("restaurantID");
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
/*			String discountRate  =request.getParameter("discountRate");
			String discountType = request.getParameter("discountType");*/
			String exchangeRate = request.getParameter("exchangeRate");
			String initialPoint = request.getParameter("initialPoint");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			String memberDiscountCheckeds = request.getParameter("memberDiscountCheckeds");
			
			String[] mDiscountCheckedList = null;
			
			if(!memberDiscountCheckeds.trim().isEmpty()){
				mDiscountCheckedList = memberDiscountCheckeds.split(",");
			}else{
				mDiscountCheckedList = new String[0];
			}
			
/*			MemberType mt = new MemberType();
			
			mt.setRestaurantId(Integer.valueOf(restaurantID));
			mt.setName(typeName == null ? "" : typeName.trim());
			mt.setDiscountType(Integer.valueOf(discountType));
			mt.setExchangeRate(Float.valueOf(exchangeRate));
			mt.setChargeRate(Float.valueOf(chargeRate));
			mt.setAttribute(Integer.valueOf(attr));
			mt.setInitialPoint(Integer.valueOf(initialPoint));
			
			if(DiscountType.valueOf(Integer.parseInt(discountType)) == DiscountType.DISCOUNT_ENTIRE){
				if(discountRate == null || discountRate.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9972, "操作失败, 获取折扣率信息失败.");
					return null;
				}
				mt.setDiscountRate(Float.valueOf(discountRate));				
			}else if(DiscountType.valueOf(Integer.parseInt(discountType)) == DiscountType.DISCOUNT_PLAN){
				if(discountID == null || discountID.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9971, "操作失败, 获取折扣方案信息失败.");
					return null;
				}
				mt.getDefaultDiscount().setId(Integer.valueOf(discountID));
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9973, "操作失败, 折扣信息不完整, 请检查\"折扣方式\"相关信息.");
				return null;
			}*/
			
			
			MemberType.InsertBuilder insert = new MemberType.InsertBuilder(Integer.valueOf(restaurantID), typeName.trim(), new Discount(Integer.valueOf(discountID)));
			insert.setAttribute(Attribute.valueOf(Integer.valueOf(attr)));
			insert.setChargeRate(Float.valueOf(chargeRate));
			insert.setExchangeRate(Float.valueOf(exchangeRate));
			insert.setInitialPoint(Integer.valueOf(initialPoint));
			
			for (String s : mDiscountCheckedList) {
				insert.addDiscount(new Discount(Integer.parseInt(s)));
			}
			
			
			MemberTypeDao.insert(staff, insert);
			jobject.initTip(true, "操作成功, 已添加新会员类型.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			//String restaurantID = request.getParameter("restaurantID");
			String typeID = request.getParameter("typeID");
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
/*			String discountRate  =request.getParameter("discountRate");
			String discountType = request.getParameter("discountType");*/
			String initialPoint = request.getParameter("initialPoint");
			String exchangeRate = request.getParameter("exchangeRate");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			String memberDiscountCheckeds = request.getParameter("memberDiscountCheckeds");
			
			String[] mDiscountCheckedList = null;
			
			if(!memberDiscountCheckeds.trim().isEmpty()){
				mDiscountCheckedList = memberDiscountCheckeds.split(",");
			}else{
				mDiscountCheckedList = new String[0];
			}
			
			MemberType.UpdateBuilder update = new MemberType.UpdateBuilder(Integer.valueOf(typeID));
			
			if(attr != null && !attr.trim().isEmpty()){
				update.setAttribute(Attribute.valueOf(Integer.valueOf(attr)));
			}
			if(exchangeRate != null && !exchangeRate.trim().isEmpty()){
				update.setChargeRate(Float.valueOf(chargeRate));
			}
			if(discountID != null && !discountID.trim().isEmpty()){
				update.setDefaultDiscount(new Discount(Integer.valueOf(discountID)));
			}
			if(chargeRate != null && !chargeRate.trim().isEmpty()){
				update.setChargeRate(Float.valueOf(chargeRate));
			}
			if(initialPoint != null && !initialPoint.trim().isEmpty()){
				update.setInitialPoint(Integer.valueOf(initialPoint));
			}
			if(typeName != null && !typeName.trim().isEmpty()){
				update.setName(typeName);
			}
			for (String s : mDiscountCheckedList) {
				update.addDiscount(new Discount(Integer.parseInt(s)));
			}
/*			MemberType mt = new MemberType();
			
			mt.setRestaurantId(Integer.valueOf(restaurantID));
			mt.setTypeId(Integer.valueOf(typeID));
			mt.setName(typeName == null ? "" : typeName.trim());
			mt.setDiscountType(Integer.valueOf(discountType));
			mt.setExchangeRate(Float.valueOf(exchangeRate));
			mt.setChargeRate(Float.valueOf(chargeRate));
			mt.setAttribute(Integer.valueOf(attr));
			mt.setInitialPoint(Integer.valueOf(initialPoint));
			
			if(DiscountType.valueOf(Integer.parseInt(discountType)) == DiscountType.DISCOUNT_ENTIRE){
				if(discountRate == null || discountRate.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9972, "操作失败, 获取\"折扣率\"信息失败.");
					return null;
				}
				mt.getDefaultDiscount().setId(Integer.valueOf(discountID));
				mt.setDiscountRate(Float.valueOf(discountRate));				
			}else if(DiscountType.valueOf(Integer.parseInt(discountType)) == DiscountType.DISCOUNT_PLAN){
				if(discountID == null || discountID.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9971, "操作失败, 获取\"折扣方案\"信息失败.");
					return null;
				}
				mt.getDefaultDiscount().setId(Integer.valueOf(discountID));
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9973, "操作失败, 折扣信息不完整, 请检查\"折扣方式\"相关信息.");
				return null;
			}*/
			
			MemberTypeDao.update(staff, update);
			jobject.initTip(true, "操作成功, 已修改会员类型信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String typeID = request.getParameter("typeID");
			MemberTypeDao.deleteById(staff, Integer.parseInt(typeID));
			jobject.initTip(true, "操作成功, 已删除会员类型相关信息.");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
