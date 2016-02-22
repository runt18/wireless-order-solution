package com.wireless.Actions.system;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.BusinessHourDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.BusinessHour;
import com.wireless.pojo.util.DateUtil;

public class OperateBusinessHourAction extends DispatchAction{

	/**
	 * 新增市别
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String name = request.getParameter("name");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");

		final JObject jObject = new JObject();
		try{
			
			final BusinessHour.InsertBuilder builder = new BusinessHour.InsertBuilder(name, new HourRange(opening, ending, DateUtil.Pattern.HOUR));

			BusinessHourDao.insert(StaffDao.verify(Integer.parseInt(pin)), builder);
			jObject.initTip(true, "操作成功, 已添加市别信息.");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
				
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 修改市别
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final JObject jObject = new JObject();
		try{
			
			final BusinessHour.UpdateBuilder builder = new BusinessHour.UpdateBuilder(Integer.parseInt(id));
			if(name != null && !name.isEmpty()){
				builder.setName(name);
			}
			
			if(opening != null && !opening.isEmpty() && ending != null && !ending.isEmpty()){
				builder.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			BusinessHourDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jObject.initTip(true, "操作成功, 已市别信息.");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 删除市别
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String id = request.getParameter("id");
		final JObject jObject = new JObject();
		try{
			BusinessHourDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(id));
			jObject.initTip(true, "操作成功, 已删除市别.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}

	/**
	 * 获取市别数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String id = request.getParameter("id");
		final JObject jObject = new JObject();
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final BusinessHourDao.ExtraCond extraCond = new BusinessHourDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			jObject.setRoot(BusinessHourDao.getByCond(staff, extraCond, null));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
