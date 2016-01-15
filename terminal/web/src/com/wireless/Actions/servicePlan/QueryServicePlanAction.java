package com.wireless.Actions.servicePlan;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.serviceRate.ServicePlanDao.ShowType;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.serviceRate.ServiceRate;
import com.wireless.pojo.staffMgr.Staff;

public class QueryServicePlanAction extends DispatchAction{
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final JObject jobject = new JObject();
		
		try{
			jobject.setRoot(ServicePlanDao.getAll(staff));
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward planTree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String pin = (String) request.getAttribute("pin");
		
		StringBuilder spTree = new StringBuilder();
		
		try{
			
			int i = 0;
			for (ServicePlan sp : ServicePlanDao.getAll(StaffDao.verify(Integer.parseInt(pin)))) {
				spTree.append(i > 0 ? "," : "");
				spTree.append("{");
				spTree.append("leaf : true");
				spTree.append(",text:'" + sp.getName() + "'");
				spTree.append(",planName:'" + sp.getName() + "'");
				spTree.append(",planId :" + sp.getPlanId());
				spTree.append(",type:" + sp.getType().getVal());
				spTree.append(",status:" + sp.getStatus().getVal());
				spTree.append(",rates:" + sp.getRates());
				spTree.append("}");
				i++;
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + spTree.toString() + "]");
		}
		
		return null;
	}
	
	public ActionForward getRates(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		String planId = request.getParameter("planId");
		
		JObject jobject = new JObject();
		
		try{
			ServicePlan plan = ServicePlanDao.getById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(planId), ShowType.BY_REGION);
			
			List<ServiceRate> rates = plan.getRates();	
			
			jobject.setTotalProperty(rates.size());
			
			jobject.setRoot(rates);
			
			
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
