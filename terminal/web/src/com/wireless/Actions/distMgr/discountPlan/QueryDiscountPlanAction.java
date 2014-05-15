package com.wireless.Actions.distMgr.discountPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.distMgr.DiscountDao.ShowType;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.DiscountPlan;

public class QueryDiscountPlanAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		List<DiscountPlan> list = null;
		List<Jsonable> jList = new ArrayList<Jsonable>();
		try{
			String pin = (String)request.getAttribute("pin");
			String discountID = request.getParameter("discountID");
			
			Discount d = DiscountDao.getById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(discountID), ShowType.BY_KITCHEN);
			list = d.getPlans();
			
			for (final DiscountPlan plan : list) {
				Jsonable j = new Jsonable() {
					@Override
					public Map<String, Object> toJsonMap(int flag) {
						Map<String, Object> jm = new LinkedHashMap<String, Object>();
						jm.put("id", plan.getId());
						jm.put("discount", plan.getDiscount());
						jm.put("rate", plan.getRate());
						if(plan.getKitchen() != null){
							plan.getKitchen().setDept(null);
							jm.put("kitchen", plan.getKitchen());
						}
						if(plan.getKitchen() != null){
							jm.put("dept", plan.getKitchen().getDept());
						}
						
						return Collections.unmodifiableMap(jm);
					}
					
					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				};
				jList.add(j);
			}
	
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(jList);
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
