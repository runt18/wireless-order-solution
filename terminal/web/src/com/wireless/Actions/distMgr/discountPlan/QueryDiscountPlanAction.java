package com.wireless.Actions.distMgr.discountPlan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryDiscountPlanAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		String restaurantID = request.getParameter("restaurantID");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String discountID = request.getParameter("discountID");
		String kitchenName = request.getParameter("kitchenName");
		
		DiscountPlan[] plan = new DiscountPlan[0];
		List root = new ArrayList();
		
		try{
			
			String extraCond = "";
			extraCond += " AND B.restaurant_id = " + restaurantID;
			if(discountID != null && !discountID.trim().equals("") && !discountID.trim().equals("-1"))
				extraCond += " AND B.discount_id = " + discountID;
			if(kitchenName != null && !kitchenName.trim().equals(""))
				extraCond += " AND K.name like '%" + kitchenName.trim() + "%'";
			
			plan = DiscountDao.getDiscountPlan(extraCond, " ORDER BY A.dist_plan_id,K.kitchen_alias");
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setTotalProperty(plan.length);
			
			if(isPaging != null && isPaging.trim().equals("true")){
				int pageIndex = Integer.valueOf(start);
				int pageSize = Integer.valueOf(limit);
				pageSize = (pageIndex + pageSize) > plan.length ? pageSize - ((pageIndex + pageSize) - plan.length) : pageSize;
				for(int i = 0; i < pageSize; i++){
					root.add(plan[pageIndex + i]);
				}
				jobject.setRoot(root);
			}else{
				jobject.setRoot(Arrays.asList(plan));
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
}
