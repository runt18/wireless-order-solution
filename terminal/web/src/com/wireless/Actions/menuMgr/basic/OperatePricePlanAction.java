package com.wireless.Actions.menuMgr.basic;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.PricePlan;

public class OperatePricePlanAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String name = request.getParameter("name");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try{
			PricePlanDao.insert(StaffDao.verify(Integer.parseInt(pin)), new PricePlan.InsertBuilder(name));
			jobject.initTip(true, "添加成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
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
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String id = request.getParameter("id");
		String name = request.getParameter("name");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try{
			PricePlanDao.update(StaffDao.verify(Integer.parseInt(pin)), new PricePlan.UpdateBuilder(Integer.parseInt(id)).setName(name));
			jobject.initTip(true, "修改成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
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
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String id = request.getParameter("id");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try{
			PricePlanDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(id));
			jobject.initTip(true, "删除成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
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

}
