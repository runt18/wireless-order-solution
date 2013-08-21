package com.wireless.Actions.menuMgr.pricePlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodPricePlan;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateFoodPricePlanAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JSONObject content = null;
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			content = JSONObject.fromObject(request.getParameter("foodPricePlan"));
			FoodPricePlan foodPricePlan = new FoodPricePlan(content.getInt("planId"),
					content.getInt("foodId"), 
					content.getInt("restaurantID"),
					Float.valueOf(content.getString("unitPrice"))
			);
			MenuDao.updateFoodPricePlan(foodPricePlan);
			jobject.initTip(true, "操作成功, 已修改菜品价格信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			content = JSONObject.fromObject(jobject);
			response.getWriter().print(content.toString());
		}
		return null;
	}

}
