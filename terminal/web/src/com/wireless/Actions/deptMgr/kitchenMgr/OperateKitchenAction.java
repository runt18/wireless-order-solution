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

	public ActionForward swap(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String kitchenA = request.getParameter("kitchenA");
		String kitchenB = request.getParameter("kitchenB");
		
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		Kitchen.MoveBuilder builder = new MoveBuilder(Integer.parseInt(kitchenA), Integer.parseInt(kitchenB));
		
		JObject jobject = new JObject();
		try{
			KitchenDao.move(staff, builder);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
	
	public ActionForward addKitchen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			String kitchenName = request.getParameter("kitchenName");
			String deptID = request.getParameter("deptID");
			String isAllowTemp = request.getParameter("isAllowTemp");
			String pin = (String) request.getAttribute("pin");
			
			KitchenDao.add(StaffDao.verify(Integer.parseInt(pin)), 
							 new Kitchen.AddBuilder(kitchenName, Department.DeptId.valueOf(Integer.parseInt(deptID)))
								.setAllowTmp(Boolean.parseBoolean(isAllowTemp)));
			
			jobject.initTip(true, "操作成功,已添加厨房信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward updateKitchen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			String kitchenID = request.getParameter("kitchenID");
			String kitchenName = request.getParameter("kitchenName");
			String deptID = request.getParameter("deptID");
			String isAllowTemp = request.getParameter("isAllowTemp");
			String pin = (String) request.getAttribute("pin");
			
			KitchenDao.update(StaffDao.verify(Integer.parseInt(pin)), 
							 new UpdateBuilder(Integer.valueOf(kitchenID))
									.setName(kitchenName)
									.setDeptId(Department.DeptId.valueOf(Integer.parseInt(deptID)))
									.setAllowTmp(Boolean.parseBoolean(isAllowTemp)));
			
			jobject.initTip(true, "操作成功,已修改厨房信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward removeKitchen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			String kitchenID = request.getParameter("kitchenID");
			String pin = (String) request.getAttribute("pin");
			
			KitchenDao.remove(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(kitchenID));
			
			jobject.initTip(true, "操作成功,已修改厨房信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
