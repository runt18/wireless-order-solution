package com.wireless.Actions.distMgr.discountPlan;

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
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteDiscountPlanAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String planID = request.getParameter("planID");
			
			DiscountPlan pojo = new DiscountPlan(Integer.valueOf(planID), 0,0,0);
			
			DiscountDao.deleteDiscountPlan(pojo);
			
			jobject.initTip(true, "操作成功, 已删除分厨折扣信息.");
			
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
