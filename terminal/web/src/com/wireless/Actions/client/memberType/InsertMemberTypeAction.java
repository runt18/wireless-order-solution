package com.wireless.Actions.client.memberType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberType;
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
			mt.setExchangeRate(Double.valueOf(exchangeRate));
			mt.setChargeRate(Double.valueOf(chargeRate));
			mt.setAttribute(Integer.valueOf(attr));
			
			if(discountType.equals(String.valueOf(MemberType.DISCOUNT_TYPE_ENTIRE))){
				if(discountRate == null || discountRate.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9972, "操作失败, 获取折扣率信息失败.");
					return null;
				}
				mt.setDiscountRate(Double.valueOf(discountRate));				
			}else if(discountType.equals(String.valueOf(MemberType.DISCOUNT_TYPE_DISCOUNT))){
				if(discountID == null || discountID.trim().isEmpty()){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9971, "操作失败, 获取折扣方案信息失败.");
					return null;
				}
				mt.setDiscountID(Integer.valueOf(discountID));
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9973, "操作失败, 折扣信息不完整, 请检查\"折扣方式\"相关信息.");
				return null;
			}
			
			MemberDao.insertMemberType(mt);
			
			jobject.initTip(true, "操作成功, 已添加新会员类型.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.errCode, e.getMessage());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
			e.printStackTrace();
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
