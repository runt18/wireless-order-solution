package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertDiscountAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String resturantID = request.getParameter("restaurantID");
			String discountName = request.getParameter("discountName");
			String level = request.getParameter("level");
			String rate = request.getParameter("rate");
			String isAuto = request.getParameter("isAuto");
//			String status = request.getParameter("status");
			String isDefault = request.getParameter("isDefault");
			
			Discount pojo = new Discount();
			pojo.setName(discountName.trim());
			pojo.setRestaurantId(Integer.valueOf(resturantID));
			pojo.setLevel(Integer.valueOf(level));
//			pojo.setStatus(Integer.valueOf(status));
			if(isDefault != null && Boolean.valueOf(isDefault))
				pojo.setStatus(Discount.Status.DEFAULT);
			
			DiscountPlan plan = null;
			
			if(isAuto != null && isAuto.trim().equals("true")){
				plan = new DiscountPlan(0);
				plan.setRate(Float.valueOf(rate));
			}
			
			DiscountDao.insertDiscount(pojo, plan);
			
			jobject.initTip(true,  "操作成功, 已添加新折扣方案!");
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

}
