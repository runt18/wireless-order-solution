package com.wireless.Actions.weixin;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.action.WxKeywordDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.action.WxKeyword;

public class OperateKeywordAction extends DispatchAction {
	
	/**
	 * 新建微信关键字回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String callback = request.getParameter("callback");
		final String keyword = request.getParameter("keyword");
		final String pin = (String)request.getAttribute("pin");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxKeyword.InsertBuilder builder = new WxKeyword.InsertBuilder(keyword, WxKeyword.Type.NORMAL);
			WxKeywordDao.insert(staff, builder);
			jObject.initTip(true, "新建微信关键字回复成功");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(false, e.getMessage());
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		return null;
	}
	
	/**
	 * 获取微信关键字回复信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String callback = request.getParameter("callback");
		final String id = request.getParameter("id");
		final String pin = (String)request.getAttribute("pin");

		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxKeywordDao.ExtraCond extraCond = new WxKeywordDao.ExtraCond();
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			jObject.setRoot(WxKeywordDao.getByCond(staff, extraCond));
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(false, e.getMessage());
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		return null;
	}
	
	/**
	 * 删除微信关键字回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String callback = request.getParameter("callback");
		final String id = request.getParameter("id");
		final String pin = (String)request.getAttribute("pin");

		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxKeywordDao.ExtraCond extraCond = new WxKeywordDao.ExtraCond();
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			WxKeywordDao.deleteByCond(staff, extraCond);
			jObject.initTip(true, "删除微信关键字回复成功");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(false, e.getMessage());
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String callback = request.getParameter("callback");
		final String id = request.getParameter("id");
		final String pin = (String)request.getAttribute("pin");
		final String actionId = request.getParameter("actionId");
		final String keyword = request.getParameter("keyword");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxKeyword.UpdateBuilder builder = new WxKeyword.UpdateBuilder(Integer.parseInt(id));
			if(actionId != null && !actionId.isEmpty()){
				builder.setAction(Integer.parseInt(actionId));
			}
			if(keyword != null && !keyword.isEmpty()){
				builder.setKeyword(keyword);
			}
			WxKeywordDao.update(staff, builder);
			jObject.initTip(true, "修改微信关键字回复成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(false, e.getMessage());
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		return null;
	}
}
