package com.wireless.Actions.distMgr.discountPlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.QueryDiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateDiscountPlanRateAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String discountID = request.getParameter("discountID");
			String rate = request.getParameter("rate");
			
			DiscountPlan pojo = new DiscountPlan();
			pojo.setRate(Float.valueOf(rate));
			pojo.getDiscount().setId(Integer.valueOf(discountID));
			pojo.getDiscount().setRestaurantID(Integer.valueOf(restaurantID));
			
			QueryDiscountDao.updateDiscountPlanRate(pojo);
			
			jobject.initTip(true, "操作成功, 已修改该方案下所有分厨折扣信息.");
		
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getMessage());
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
