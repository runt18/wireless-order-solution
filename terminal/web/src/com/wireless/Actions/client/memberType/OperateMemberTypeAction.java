package com.wireless.Actions.client.memberType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.client.MemberType.DiscountType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

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
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = request.getParameter("restaurantID");
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
			String discountRate  =request.getParameter("discountRate");
			String discountType = request.getParameter("discountType");
			String exchangeRate = request.getParameter("exchangeRate");
			String initialPoint = request.getParameter("initialPoint");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			
			MemberType mt = new MemberType();
			
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
				mt.getDiscount().setId(Integer.valueOf(discountID));
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9973, "操作失败, 折扣信息不完整, 请检查\"折扣方式\"相关信息.");
				return null;
			}
			
			MemberTypeDao.insertMemberType(mt);
			jobject.initTip(true, "操作成功, 已添加新会员类型.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
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
			String restaurantID = request.getParameter("restaurantID");
			String typeID = request.getParameter("typeID");
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
			String discountRate  =request.getParameter("discountRate");
			String discountType = request.getParameter("discountType");
			String initialPoint = request.getParameter("initialPoint");
			String exchangeRate = request.getParameter("exchangeRate");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			
			MemberType mt = new MemberType();
			
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
				mt.getDiscount().setId(Integer.valueOf(discountID));
				mt.setDiscountRate(Float.valueOf(discountRate));				
			}else if(DiscountType.valueOf(Integer.parseInt(discountType)) == DiscountType.DISCOUNT_PLAN){
				if(discountID == null || discountID.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9971, "操作失败, 获取\"折扣方案\"信息失败.");
					return null;
				}
				mt.getDiscount().setId(Integer.valueOf(discountID));
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9973, "操作失败, 折扣信息不完整, 请检查\"折扣方式\"相关信息.");
				return null;
			}
			
			MemberTypeDao.updateMemberType(mt);
			jobject.initTip(true, "操作成功, 已修改会员类型信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
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
			String restaurantID = request.getParameter("restaurantID");
			String typeID = request.getParameter("typeID");
			String discountType = request.getParameter("discountType");
			String discountID = request.getParameter("discountID");
			
			if(restaurantID == null || typeID == null || discountID == null || discountType == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9970, "操作失败, 会员类型相关信息不完整.");
				return null;
			}
			
			MemberType mt = new MemberType();
			mt.setRestaurantId(Integer.valueOf(restaurantID));
			mt.setTypeId(Integer.valueOf(typeID));
			mt.setDiscountType(Integer.valueOf(discountType));
			mt.getDiscount().setId(Integer.valueOf(discountID));
			
			MemberTypeDao.deleteMemberType(mt);
			jobject.initTip(true, "操作成功, 已删除会员类型相关信息.");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}
