package com.wireless.Actions.deptMgr.kitchenMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.Kitchen.MoveBuilder;
import com.wireless.pojo.menuMgr.Kitchen.UpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class OperateKitchenAction extends DispatchAction{

	/**
	 * 获取厨房信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		
		
		final String pin = (String) request.getAttribute("pin");
		final String id = request.getParameter("id");
		final JObject jobject = new JObject();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final KitchenDao.ExtraCond extraCond = new KitchenDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			jobject.setRoot(KitchenDao.getByCond(staff, extraCond, null));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	/**
	 * 厨房互换
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward swap(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		
		final String kitchenA = request.getParameter("kitchenA");
		final String kitchenB = request.getParameter("kitchenB");
		
		final String pin = (String) request.getAttribute("pin");
		final JObject jobject = new JObject();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final Kitchen.MoveBuilder builder = new MoveBuilder(Integer.parseInt(kitchenA), Integer.parseInt(kitchenB));
			KitchenDao.move(staff, builder);
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	/**
	 * 增加厨房
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward addKitchen(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String kitchenName = request.getParameter("kitchenName");
		final String deptID = request.getParameter("deptID");
		final String isAllowTemp = request.getParameter("isAllowTemp");
		final String pin = (String) request.getAttribute("pin");		
		try{
			
			KitchenDao.add(StaffDao.verify(Integer.parseInt(pin)), 
							 new Kitchen.AddBuilder(kitchenName, Department.DeptId.valueOf(Integer.parseInt(deptID)))
								.setAllowTmp(Boolean.parseBoolean(isAllowTemp)));
			
			jObject.initTip(true, "操作成功,已添加厨房信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 修改厨房
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateKitchen(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String kitchenId = request.getParameter("kitchenID");
		final String kitchenName = request.getParameter("kitchenName");
		final String deptId = request.getParameter("deptID");
		final String isAllowTemp = request.getParameter("isAllowTemp");
		final String pin = (String) request.getAttribute("pin");
		
		try{
			
			KitchenDao.update(StaffDao.verify(Integer.parseInt(pin)), 
							 new UpdateBuilder(Integer.valueOf(kitchenId))
									.setName(kitchenName)
									.setDeptId(Department.DeptId.valueOf(Integer.parseInt(deptId)))
									.setAllowTmp(Boolean.parseBoolean(isAllowTemp)));
			
			jObject.initTip(true, "操作成功,已修改厨房信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 删除厨房
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward removeKitchen(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		final String kitchenId = request.getParameter("kitchenID");
		final String pin = (String) request.getAttribute("pin");
		
		try{
			
			KitchenDao.remove(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(kitchenId));
			
			jobject.initTip(true, "操作成功,已修改厨房信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
