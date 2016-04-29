package com.wireless.Actions.inventoryMgr.materialCate;

import java.sql.SQLException;

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
import com.wireless.pojo.staffMgr.Staff;

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
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String name = request.getParameter("name");
		final String type = request.getParameter("type");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final MaterialCate.InsertBuilder builder = new MaterialCate.InsertBuilder();
			if(name != null && !name.isEmpty()){
				builder.setName(name);
			}
			
			if(type != null && !type.isEmpty() && Integer.valueOf(type) > 0){
				builder.setType(MaterialCate.Type.valueOf(Integer.valueOf(type)));
			}else{
				builder.setType(MaterialCate.Type.MATERIAL);
			}
			
			MaterialCateDao.insert(staff, builder);
			jObject.initTip(true, "操作成功, 已添加新原料类别信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String cateId = request.getParameter("cateId");
		final String cateType = request.getParameter("cateType");
		final String name = request.getParameter("name");
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MaterialCate.UpdateBuilder builder = new MaterialCate.UpdateBuilder(Integer.valueOf(cateId));
			if(cateType != null && !cateType.isEmpty() && Integer.valueOf(cateType) > 0){
				builder.setType(MaterialCate.Type.valueOf(Integer.valueOf(cateType)));
			}
			
			if(name != null && !name.isEmpty()){
				builder.setName(name);
			}
			
			MaterialCateDao.update(staff, builder);
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
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String cateId = request.getParameter("cateId");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MaterialCateDao.deleteByCond(staff, new MaterialCateDao.ExtraCond().setId(Integer.valueOf(cateId)));
			jObject.initTip(true, "操作成功, 已删除原料类别信息.");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}
