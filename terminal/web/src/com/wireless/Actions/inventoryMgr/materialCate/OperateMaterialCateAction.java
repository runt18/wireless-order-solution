package com.wireless.Actions.inventoryMgr.materialCate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;

public class OperateMaterialCateAction extends DispatchAction {
	
	/**
	 * 添加原料类别
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
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String) request.getAttribute("restaurantID");
			String name = request.getParameter("name");
			MaterialCate mc = new MaterialCate(Integer.valueOf(restaurantID), name);
			MaterialCateDao.insert(mc);
			jobject.initTip(true, "操作成功, 已添加新原料类别信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 修改原料类别
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
			String restaurantID = (String) request.getAttribute("restaurantID");
			String cateId = request.getParameter("cateId");
			String cateType = request.getParameter("cateType");
			String name = request.getParameter("name");
			MaterialCate mc = new MaterialCate(Integer.valueOf(cateId), Integer.valueOf(restaurantID), name);
			if(cateType != null && !cateType.isEmpty()){
				mc.setType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			MaterialCateDao.update(mc);
			jobject.initTip(true, "操作成功, 已修改原料类别信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 删除原料类别
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
		String pin = (String)request.getAttribute("pin");
		try{
			String cateId = request.getParameter("cateId");
			MaterialCateDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(cateId));
			jobject.initTip(true, "操作成功, 已删除原料类别信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
