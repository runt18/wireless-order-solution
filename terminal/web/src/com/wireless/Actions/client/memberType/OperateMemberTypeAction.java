package com.wireless.Actions.client.memberType;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.MemberType.Attribute;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMemberTypeAction extends DispatchAction{
	
	/**
	 * 新建会员类型
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String typeName = request.getParameter("typeName");
		final String defaultDiscount = request.getParameter("discountID");
		final String defaultPricePlan = request.getParameter("pricePlanId");
		final String exchangeRate = request.getParameter("exchangeRate");
		final String initialPoint = request.getParameter("initialPoint");
		final String chargeRate = request.getParameter("chargeRate");
		final String attr = request.getParameter("attr");
		final String desc = request.getParameter("desc");
		
		final String discounts = request.getParameter("memberDiscountCheckeds");
		final String pricePlans = request.getParameter("memberPricePlanCheckeds");
		
		final JObject jObject = new JObject();
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final MemberType.InsertBuilder builder = new MemberType.InsertBuilder(typeName.trim());
			
			builder.setAttribute(Attribute.valueOf(Integer.valueOf(attr)));
			builder.setChargeRate(Float.valueOf(chargeRate));
			builder.setExchangeRate(Float.valueOf(exchangeRate));
			builder.setInitialPoint(Integer.valueOf(initialPoint));
			builder.setDesc(desc);
			
			if(defaultDiscount != null && !defaultDiscount.isEmpty()){
				builder.setDefaultDiscount(new Discount(Integer.parseInt(defaultDiscount)));
			}
			
			if(discounts != null && !discounts.isEmpty()){
				for (String discountId : discounts.split(",")) {
					builder.addDiscount(new Discount(Integer.parseInt(discountId)));
				}
			}
			
			if(pricePlans != null && !pricePlans.trim().isEmpty()){
				for (String pricePlanId : pricePlans.split(",")) {
					builder.addPrice(new PricePlan(Integer.parseInt(pricePlanId)));
				}
			}			
			
			if(defaultPricePlan != null && !defaultPricePlan.isEmpty()){
				builder.setDefaultPrice(new PricePlan(Integer.parseInt(defaultPricePlan)));
			}
			
			MemberTypeDao.insert(staff, builder);
			jObject.initTip(true, "操作成功, 已添加新会员类型.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 修改会员类型
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String typeID = request.getParameter("typeID");
		final String typeName = request.getParameter("typeName");
		final String defaultDiscount = request.getParameter("discountID");
		final String defaultPricePlan = request.getParameter("pricePlanId");
		final String initialPoint = request.getParameter("initialPoint");
		final String exchangeRate = request.getParameter("exchangeRate");
		final String chargeRate = request.getParameter("chargeRate");
		final String attr = request.getParameter("attr");
		final String desc = request.getParameter("desc");
		final String discounts = request.getParameter("memberDiscountCheckeds");
		final String pricePlans = request.getParameter("memberPricePlanCheckeds");
		
		final String branchDiscountVal = request.getParameter("branchDiscount");
		final String branchPriceVal = request.getParameter("branchPrice");
		
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final MemberType.UpdateBuilder builder = new MemberType.UpdateBuilder(Integer.valueOf(typeID));
			
			if(desc != null && !desc.trim().isEmpty()){
				builder.setDesc(desc);
			}
			
			if(attr != null && !attr.trim().isEmpty()){
				builder.setAttribute(Attribute.valueOf(Integer.valueOf(attr)));
			}
			
			if(exchangeRate != null && !exchangeRate.trim().isEmpty()){
				builder.setExchangeRate(Float.valueOf(exchangeRate));
			}
			
			if(defaultDiscount != null && !defaultDiscount.trim().isEmpty()){
				builder.setDefaultDiscount(new Discount(Integer.valueOf(defaultDiscount)));
			}
			
			if(chargeRate != null && !chargeRate.trim().isEmpty()){
				builder.setChargeRate(Float.valueOf(chargeRate));
			}
			
			if(initialPoint != null && !initialPoint.trim().isEmpty()){
				builder.setInitialPoint(Integer.valueOf(initialPoint));
			}
			
			if(typeName != null && !typeName.trim().isEmpty()){
				builder.setName(typeName);
			}
			
			if(discounts != null && !discounts.isEmpty()){
				for (String discountId : discounts.split(",")) {
					builder.addDiscount(new Discount(Integer.parseInt(discountId)));
				}
			}		
			
			if(pricePlans != null){
				if(pricePlans.trim().isEmpty()){
					builder.setEmptyPrice();
				}else{
					for (String pricePlanId : pricePlans.split(",")) {
						builder.addPrice(new PricePlan(Integer.parseInt(pricePlanId)));
					}
					builder.setDefaultPrice(new PricePlan(Integer.parseInt(defaultPricePlan)));
				}
			}			

			//门店折扣，格式如下"branchId, defaultBranchDiscount, branchDiscount_1, ..., branchDiscount_X"
			if(branchDiscountVal != null && !branchDiscountVal.isEmpty()){
				String[] branchDiscount = branchDiscountVal.split(",");
				//branch restaurant
				Restaurant branch = RestaurantDao.getById(Integer.parseInt(branchDiscount[0]));
				//default branch discount
				Discount defaultBranchDiscount = DiscountDao.getById(StaffDao.getAdminByRestaurant(branch.getId()), Integer.parseInt(branchDiscount[1]));
				MemberType.Discount4Chain chainDiscount = new MemberType.Discount4Chain(branch, defaultBranchDiscount);
				//branch discounts
				if(branchDiscount.length > 2){
					for(int i = 2; i < branchDiscount.length; i++){
						chainDiscount.addDiscount(DiscountDao.getById(StaffDao.getAdminByRestaurant(branch.getId()), Integer.parseInt(branchDiscount[i])));
					}
				}
				builder.addChainDiscount(chainDiscount);
			}
			
			//门店价格方案，格式如下"branchId, defaultBranchPrice, branchPrice_2, ..., branchPrice_X"
			if(branchPriceVal != null && !branchPriceVal.isEmpty()){
				String[] branchPrice = branchPriceVal.split(",");
				//branch restaurant
				Restaurant branch = RestaurantDao.getById(Integer.parseInt(branchPrice[0]));
				//default branch price
				PricePlan defaultBranchPrice = PricePlanDao.getById(StaffDao.getAdminByRestaurant(branch.getId()), Integer.parseInt(branchPrice[1]));
				MemberType.Price4Chain chainPrice =  new MemberType.Price4Chain(branch, defaultBranchPrice);
				//branch prices
				if(branchPrice.length > 2){
					for(int i = 2; i < branchPrice.length; i++){
						chainPrice.addPrice(PricePlanDao.getById(StaffDao.getAdminByRestaurant(branch.getId()), Integer.parseInt(branchPrice[i])));
					}
				}
				builder.addChainPrice(chainPrice);
			}
			
			MemberTypeDao.update(staff, builder);
			
			jObject.initTip(true, "操作成功, 已修改会员类型信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 删除会员类型
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String typeId = request.getParameter("typeID");
		
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MemberTypeDao.deleteById(staff, Integer.parseInt(typeId));
			
			jObject.initTip(true, "操作成功, 已删除会员类型相关信息.");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}
