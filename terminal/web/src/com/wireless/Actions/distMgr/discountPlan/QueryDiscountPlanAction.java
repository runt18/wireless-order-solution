package com.wireless.Actions.distMgr.discountPlan;

import java.util.List;

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
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryDiscountPlanAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		List<DiscountPlan> list = null;
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String)request.getAttribute("restaurantID");
			String discountID = request.getParameter("discountID");
			String kitchenName = request.getParameter("kitchenName");
			String extraCond = "";
			extraCond += " AND B.restaurant_id = " + restaurantID;
			if(discountID != null && !discountID.trim().equals("") && !discountID.trim().equals("-1"))
				extraCond += " AND B.discount_id = " + discountID;
			if(kitchenName != null && !kitchenName.trim().equals(""))
				extraCond += " AND K.name like '%" + kitchenName.trim() + "%'";
			
			list = DiscountDao.getDiscountPlan(extraCond, " ORDER BY A.dist_plan_id,K.kitchen_alias");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
