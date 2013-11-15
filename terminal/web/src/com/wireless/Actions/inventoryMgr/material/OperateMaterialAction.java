package com.wireless.Actions.inventoryMgr.material;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperateMaterialAction extends DispatchAction {
	
	/**
	 * 添加原料
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String restaurantID = request.getParameter("restaurantID");
			String name = request.getParameter("name");
			String cateId = request.getParameter("cateId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Material m = new Material(Integer.valueOf(restaurantID), name, Integer.valueOf(cateId), staff.getName(), Material.Status.NORMAL.getValue());
			MaterialDao.insert(m);
			jobject.initTip(true, "操作成功, 已添加新原料信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 修改原料
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String restaurantID = request.getParameter("restaurantID");
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String cateId = request.getParameter("cateId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Material m = new Material(Integer.valueOf(id), Integer.valueOf(restaurantID), Integer.valueOf(cateId), name, staff.getName());
			MaterialDao.update(m);
			jobject.initTip(true, "操作成功, 已修改原料信息.");
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 删除原料
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String id = request.getParameter("id");
			MaterialDao.delete(Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除原料信息.");
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward monthSettleMaterial(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String editData = request.getParameter("editData");
		try{
			String pin = (String)request.getAttribute("pin");
			String restaurantID =  (String) request.getAttribute("restaurantID");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String[] materialRecords = editData.split("<li>");
			for (String record : materialRecords) {
				
				String material[] = record.split(",");
				Material m = new Material();
				m.setId(Integer.parseInt(material[0]));
				m.setDelta(Float.parseFloat(material[1]));
				m.setLastModStaff(staff.getName());
				m.setRestaurantId(Integer.parseInt(restaurantID));
				MaterialDao.update(m);
			}
		}catch(SQLException e){	
			e.printStackTrace();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public ActionForward cancelMonthSettle(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try{
			String restaurantID =  (String) request.getAttribute("restaurantID");
			Material m = new Material();
			m.setId(-10);
			m.setRestaurantId(Integer.parseInt(restaurantID));
			MaterialDao.update(m);
			
		}catch(SQLException e){	
			e.printStackTrace();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
}
