package com.wireless.Actions.distMgr.discountPlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.DiscountPlan;

public class UpdateDiscountPlanRateAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = request.getParameter("restaurantID");
			String discountID = request.getParameter("discountID");
			String rate = request.getParameter("rate");
			
			DiscountPlan pojo = new DiscountPlan(0);
			pojo.setRate(Float.valueOf(rate));
			pojo.getDiscount().setId(Integer.valueOf(discountID));
			pojo.getDiscount().setRestaurantId(Integer.valueOf(restaurantID));
			
			DiscountDao.updateDiscountPlanRate(pojo);
			
			jobject.initTip(true, "操作成功, 已修改该方案下所有分厨折扣信息.");
		
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
