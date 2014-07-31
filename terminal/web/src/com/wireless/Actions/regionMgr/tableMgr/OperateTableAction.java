package com.wireless.Actions.regionMgr.tableMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperateTableAction extends DispatchAction{

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
			String pin = (String)request.getAttribute("pin");
			String name = request.getParameter("name");
			String alias = request.getParameter("alias");
			String minimumCost = request.getParameter("minimumCost");
			String serviceRate = request.getParameter("serviceRate");
			String regionId = request.getParameter("regionId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Table.InsertBuilder builder = new Table.InsertBuilder(Integer.parseInt(alias), staff.getRestaurantId(),  Short.parseShort(regionId))
					.setMiniCost(Integer.valueOf(minimumCost))
					.setServiceRate(Float.valueOf(serviceRate))
					.setTableName(name);
			TableDao.insert(staff, builder);
			jobject.initTip(true, "操作成功, 已添加新餐台信息.");
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
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String regionId = request.getParameter("regionId"); 
			String minimumCost = request.getParameter("minimumCost");
			
			Table.UpdateBuilder builder = new Table.UpdateBuilder(Integer.valueOf(id)).setMiniCost(Integer.valueOf(minimumCost))
					.setRegionId(Short.valueOf(regionId))
					.setTableName(name);
			TableDao.updateById(StaffDao.verify(Integer.parseInt(pin)), builder.build());
			jobject.initTip(true, "操作成功, 已修改餐台信息.");
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
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			TableDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除餐台信息.");
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
	
}
