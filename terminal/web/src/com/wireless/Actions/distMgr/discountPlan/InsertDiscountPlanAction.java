package com.wireless.Actions.distMgr.discountPlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertDiscountPlanAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String discountID = request.getParameter("discountID");
			String kitchenID = request.getParameter("kitchenID");
			String rate = request.getParameter("rate");
			
			DiscountPlan pojo = new DiscountPlan(0, Integer.valueOf(discountID), Integer.valueOf(kitchenID), Float.valueOf(rate));
			
			DiscountDao.insertDiscountPlan(pojo);
			
			jobject.initTip(true, "操作成功, 已添加分厨折扣.");
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_WARNING, 9996, "操作失败, 该方案已包含该分厨折扣!");
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
