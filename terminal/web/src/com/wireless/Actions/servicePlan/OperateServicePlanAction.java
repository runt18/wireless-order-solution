package com.wireless.Actions.servicePlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.staffMgr.Staff;

public class OperateServicePlanAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		String pin = (String)request.getAttribute("pin");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String name = request.getParameter("name");
			String isDeafault = request.getParameter("isDeafault");
			
			ServicePlan.InsertBuilder build = new ServicePlan.InsertBuilder(name);
			
			if(Boolean.parseBoolean(isDeafault)){
				build.setStatus(ServicePlan.Status.DEFAULT);
			}
			
			ServicePlanDao.insert(staff, build);
			
			jobject.initTip(true, "添加方案成功");
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		String pin = (String)request.getAttribute("pin");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String name = request.getParameter("name");
			String servicePlanId = request.getParameter("servicePlanId");
			String isDeafault = request.getParameter("isDeafault");
			
			ServicePlan.UpdateBuilder builder = new ServicePlan.UpdateBuilder(Integer.parseInt(servicePlanId));
			
			builder.setName(name);
			
			if(Boolean.parseBoolean(isDeafault)){
				builder.setStatus(ServicePlan.Status.DEFAULT);
			}			
			
			ServicePlanDao.update(staff, builder);
			
			
			jobject.initTip(true, "操作成功,已修改");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		String pin = (String)request.getAttribute("pin");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String servicePlanId = request.getParameter("planId");
			
			ServicePlanDao.delete(staff, Integer.parseInt(servicePlanId));
			
			jobject.initTip(true, "操作成功,已删除");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}	
	
	public ActionForward operateRate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		JObject jobject = new JObject();
		String pin = (String)request.getAttribute("pin");
		String regionId = request.getParameter("regionId");
		String rate = request.getParameter("rate");
		String planId = request.getParameter("planId");
		
		try{
			ServicePlan.UpdateBuilder builder = new ServicePlan.UpdateBuilder(Integer.parseInt(planId));
			builder.addRate(Integer.parseInt(regionId), Float.parseFloat(rate));
			
			ServicePlanDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true, "操作成功");
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	} 
	
	public ActionForward updateAllRate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String servicePlanId = request.getParameter("planId");
			String rate = request.getParameter("rate");
			
			
			ServicePlan.UpdateBuilder builder = new ServicePlan.UpdateBuilder(Integer.parseInt(servicePlanId));
			
			for (Region.RegionId region : Region.RegionId.values()) {
				builder.addRate(region.getId(), Float.parseFloat(rate));
			}
			
			ServicePlanDao.update(staff, builder);
			
			jobject.initTip(true, "操作成功, 已修改该方案下所有服务费率.");
		
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	

}
