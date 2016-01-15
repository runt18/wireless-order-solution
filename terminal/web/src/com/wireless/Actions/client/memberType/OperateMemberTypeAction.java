package com.wireless.Actions.client.memberType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.MemberType.Attribute;
import com.wireless.pojo.menuMgr.PricePlan;
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
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
			String pricePlanId = request.getParameter("pricePlanId");
			String exchangeRate = request.getParameter("exchangeRate");
			String initialPoint = request.getParameter("initialPoint");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			String desc = request.getParameter("desc");
			
			String memberDiscountCheckeds = request.getParameter("memberDiscountCheckeds");
			String memberPricePlanCheckeds = request.getParameter("memberPricePlanCheckeds");
			
			String[] mDiscountCheckedList = null;
			
			if(!memberDiscountCheckeds.trim().isEmpty()){
				mDiscountCheckedList = memberDiscountCheckeds.split(",");
			}else{
				mDiscountCheckedList = new String[0];
			}
			
			MemberType.InsertBuilder insert = new MemberType.InsertBuilder(typeName.trim(), new Discount(Integer.valueOf(discountID)));
			insert.setAttribute(Attribute.valueOf(Integer.valueOf(attr)));
			insert.setChargeRate(Float.valueOf(chargeRate));
			insert.setExchangeRate(Float.valueOf(exchangeRate));
			insert.setInitialPoint(Integer.valueOf(initialPoint));
			insert.setDesc(desc);
			
			for (String s : mDiscountCheckedList) {
				insert.addDiscount(new Discount(Integer.parseInt(s)));
			}
			
			if(!memberPricePlanCheckeds.trim().isEmpty()){
				for (String s : memberPricePlanCheckeds.split(",")) {
					insert.addPrice(new PricePlan(Integer.parseInt(s)));
				}
				
				insert.setDefaultPrice(new PricePlan(Integer.parseInt(pricePlanId)));
			}			
			
			MemberTypeDao.insert(staff, insert);
			jobject.initTip(true, "操作成功, 已添加新会员类型.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String typeID = request.getParameter("typeID");
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
			String pricePlanId = request.getParameter("pricePlanId");
			String initialPoint = request.getParameter("initialPoint");
			String exchangeRate = request.getParameter("exchangeRate");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			String desc = request.getParameter("desc");
			
			String memberDiscountCheckeds = request.getParameter("memberDiscountCheckeds");
			String memberPricePlanCheckeds = request.getParameter("memberPricePlanCheckeds");
			
			String[] mDiscountCheckedList = null;
			
			if(!memberDiscountCheckeds.trim().isEmpty()){
				mDiscountCheckedList = memberDiscountCheckeds.split(",");
			}else{
				mDiscountCheckedList = new String[0];
			}
			
			MemberType.UpdateBuilder update = new MemberType.UpdateBuilder(Integer.valueOf(typeID));
			update.setDesc(desc);
			
			if(attr != null && !attr.trim().isEmpty()){
				update.setAttribute(Attribute.valueOf(Integer.valueOf(attr)));
			}
			if(exchangeRate != null && !exchangeRate.trim().isEmpty()){
				update.setExchangeRate(Float.valueOf(exchangeRate));
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
			
			if(!memberPricePlanCheckeds.trim().isEmpty()){
				for (String s : memberPricePlanCheckeds.split(",")) {
					update.addPrice(new PricePlan(Integer.parseInt(s)));
				}
				
				update.setDefaultPrice(new PricePlan(Integer.parseInt(pricePlanId)));
			}else{
				update.setEmptyPrice();
			}
			
			MemberTypeDao.update(staff, update);
			jobject.initTip(true, "操作成功, 已修改会员类型信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
