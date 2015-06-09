package com.wireless.Actions.system;

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
import com.wireless.pojo.system.BusinessHour;
import com.wireless.pojo.util.DateUtil;

public class OperateBusinessHourAction extends DispatchAction{

	/**
	 * 
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
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getAttribute("pin");
			String name = request.getParameter("name");
			String opening = request.getParameter("opening");
			String ending = request.getParameter("ending");
			
			BusinessHour.InsertBuilder insert = new BusinessHour.InsertBuilder(name, new HourRange(opening, ending, DateUtil.Pattern.HOUR));

			BusinessHourDao.insert(StaffDao.verify(Integer.parseInt(pin)), insert);
			jobject.initTip(true, "操作成功, 已添加市别信息.");
			
		}catch(BusinessException e){
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
	 * 
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
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getAttribute("pin");
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String opening = request.getParameter("opening");
			String ending = request.getParameter("ending");
			
			BusinessHour.UpdateBuilder update = new BusinessHour.UpdateBuilder(Integer.parseInt(id));
			update.setName(name);
			update.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			
			BusinessHourDao.update(StaffDao.verify(Integer.parseInt(pin)),update);
			
			jobject.initTip(true, "操作成功, 已市别信息.");
			
		}catch(BusinessException e){
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
	 * 
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
		JObject jobject = new JObject();
		try{
			String id = request.getParameter("id");
			BusinessHourDao.delete(Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除市别.");
		}catch(BusinessException e){
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

}
