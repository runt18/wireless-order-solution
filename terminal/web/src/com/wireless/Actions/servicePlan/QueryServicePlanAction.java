package com.wireless.Actions.servicePlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.serviceRate.ServicePlan;

public class QueryServicePlanAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String pin = (String) request.getAttribute("pin");
		
		StringBuilder spTree = new StringBuilder();
		try{
			
			int i = 0;
			for (ServicePlan sp : ServicePlanDao.getAll(StaffDao.verify(Integer.parseInt(pin)))) {
				spTree.append(i>0? "," : "");
				spTree.append("{");
				spTree.append("leaf : true");
				spTree.append(",text:'" + sp.getName() + "'");
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

}
