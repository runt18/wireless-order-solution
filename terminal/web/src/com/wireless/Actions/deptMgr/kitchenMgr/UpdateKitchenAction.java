package com.wireless.Actions.deptMgr.kitchenMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateKitchenAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String kitchenID = request.getParameter("kitchenID");
			String kitchenName = request.getParameter("kitchenName");
			String deptID = request.getParameter("deptID");
			String isAllowTemp = request.getParameter("isAllowTemp");
			
			Kitchen kitchen = new Kitchen();
			kitchen.setRestaurantId(Integer.valueOf(restaurantID));
			kitchen.setId(Integer.valueOf(kitchenID));
			kitchen.setName(kitchenName.trim());
			kitchen.getDept().setId(Short.valueOf(deptID));
			kitchen.setAllowTemp(isAllowTemp);
			
			MenuDao.updateKitchen(kitchen);
			
			jobject.initTip(true, "操作成功,已修改厨房信息.");
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}

		return null;
	}

}
