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
import com.wireless.pojo.staffMgr.Staff;

public class OperatePricePlanAction extends DispatchAction{

	/**
	 * 增加价格方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String name = request.getParameter("name");
		final String pin = (String) request.getAttribute("pin");
		
		final JObject jObject = new JObject();
		try{
			PricePlanDao.insert(StaffDao.verify(Integer.parseInt(pin)), new PricePlan.InsertBuilder(name));
			jObject.initTip(true, "添加成功");
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}

		return null;
	}
	
	/**
	 * 更新价格方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		final String pin = (String) request.getAttribute("pin");
		
		final JObject jObject = new JObject();
		try{
			PricePlanDao.update(StaffDao.verify(Integer.parseInt(pin)), new PricePlan.UpdateBuilder(Integer.parseInt(id)).setName(name));
			jObject.initTip(true, "修改成功");
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}

		return null;
	}	
	
	/**
	 * 删除价格方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String id = request.getParameter("id");
		final String pin = (String) request.getAttribute("pin");
		
		final JObject jObject = new JObject();
		try{
			PricePlanDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(id));
			jObject.initTip(true, "删除成功");
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}

		return null;
	}	

	/**
	 * 获取价格方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String id = request.getParameter("pricePlanId");
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final PricePlanDao.ExtraCond extraCond = new PricePlanDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			jObject.setRoot(PricePlanDao.getByCond(staff, extraCond));
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
}
