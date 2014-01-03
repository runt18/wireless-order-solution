package com.wireless.Actions.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.system.BillBoardDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.system.BillBoard;

public class OperateBillboardAction extends DispatchAction{
	
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
			String type = request.getParameter("type");
			String title = request.getParameter("title");
			String desc = request.getParameter("desc");
			String rid = request.getParameter("rid");
			String expired = request.getParameter("expired");
			
			BillBoard.InsertBuilder insert = new BillBoard.InsertBuilder();
			insert.setTitle(title.trim())
				  .setDesc(desc.trim())
				  .setExpired(Long.valueOf(expired))
				  .setType(Integer.valueOf(type))
				  .setRestaurantId(Integer.valueOf(type) == 1 ? 0 : Integer.valueOf(rid));
			BillBoardDao.insert(insert);
			jobject.initTip(true, "操作成功, 已添加新公告信息.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
			String id = request.getParameter("id");
			String type = request.getParameter("type");
			String title = request.getParameter("title");
			String desc = request.getParameter("desc");
			String rid = request.getParameter("rid");
			String expired = request.getParameter("expired");
			BillBoard.UpdateBuilder update = new BillBoard.UpdateBuilder();
			update.setId(Integer.valueOf(id))
				  .setTitle(title.trim())
				  .setDesc(desc.trim())
				  .setExpired(Long.valueOf(expired))
				  .setType(Integer.valueOf(type))
				  .setRestaurantId(Integer.valueOf(type) == 1 ? 0 : Integer.valueOf(rid));
			BillBoardDao.update(update);
			jobject.initTip(true, "操作成功, 已修改公告信息.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
			BillBoardDao.delete(Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除公告信息.");
		}catch(BusinessException e){
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
}
