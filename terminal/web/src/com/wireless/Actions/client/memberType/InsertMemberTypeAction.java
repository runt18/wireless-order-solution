package com.wireless.Actions.client.memberType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.client.MemberType.DiscountType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertMemberTypeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String typeName = request.getParameter("typeName");
			String discountID = request.getParameter("discountID");
			String discountRate  =request.getParameter("discountRate");
			String discountType = request.getParameter("discountType");
			String exchangeRate = request.getParameter("exchangeRate");
			String chargeRate = request.getParameter("chargeRate");
			String attr = request.getParameter("attr");
			
			MemberType mt = new MemberType();
			
			mt.setRestaurantID(Integer.valueOf(restaurantID));
			mt.setName(typeName == null ? "" : typeName.trim());
			mt.setDiscountType(Integer.valueOf(discountType));
			mt.setExchangeRate(Float.valueOf(exchangeRate));
			mt.setChargeRate(Float.valueOf(chargeRate));
			mt.setAttribute(Integer.valueOf(attr));
			
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

}
