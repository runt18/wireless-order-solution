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
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.WebParams;

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
			String regionId = request.getParameter("regionId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Table.InsertBuilder builder = new Table.InsertBuilder(Integer.parseInt(alias), Region.RegionId.valueOf(Short.parseShort(regionId)))
					.setMiniCost(Integer.valueOf(minimumCost))
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
												   .setRegionId(Region.RegionId.valueOf(Short.valueOf(regionId)))
												   .setTableName(name);
			TableDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
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
	
	/**
	 * 批量增加
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward batch(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String beginAlias = request.getParameter("beginAlias");
			String endAlias = request.getParameter("endAlias");
			String skips = request.getParameter("skips");
			String regionId = request.getParameter("regionId");
			
			Table.BatchInsertBuilder insertBuilder = new Table.BatchInsertBuilder(Integer.parseInt(beginAlias), Integer.parseInt(endAlias), Region.RegionId.valueOf(Integer.parseInt(regionId)));
			if(skips != null && !skips.isEmpty()){
				String skipNum[] = skips.split(","); 
				for (String num : skipNum) {
					if(num.equals("4")){
						insertBuilder.setSkip4(true);
					}else if(num.equals("7")){
						insertBuilder.setSkip7(true);
					}
				}
			}
			
			TableDao.insert(StaffDao.verify(Integer.parseInt(pin)), insertBuilder);
			
			jobject.initTip(true, "操作成功, 已批量添加餐台信息.");
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
