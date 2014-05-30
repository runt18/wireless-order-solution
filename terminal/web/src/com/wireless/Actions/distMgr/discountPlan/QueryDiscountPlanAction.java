package com.wireless.Actions.distMgr.discountPlan;

import java.util.ArrayList;
import java.util.List;

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
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putInt("id", plan.getId());
						jm.putJsonable("discount", plan.getDiscount(), 0);
						jm.putFloat("rate", plan.getRate());
						if(plan.getKitchen() != null){
							plan.getKitchen().setDept(null);
							jm.putJsonable("kitchen", plan.getKitchen(), 0);
						}
						if(plan.getKitchen() != null){
							jm.putJsonable("dept", plan.getKitchen().getDept(), 0);
						}
						
						return jm;
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
