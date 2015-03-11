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
import com.wireless.pojo.system.BillBoard.Status;

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
			String title = request.getParameter("title");
			String desc = request.getParameter("desc");
			String rid = request.getParameter("rid");
			String expired = request.getParameter("expired");
			
			BillBoard.InsertBuilder insertBuilder;
			if(rid != null && !rid.trim().isEmpty()){
				insertBuilder = BillBoard.InsertBuilder.build4Restaurant(title, Integer.parseInt(rid), expired);
			}else{
				insertBuilder = BillBoard.InsertBuilder.build4System(title, expired);
			}
			
			insertBuilder.setBody(desc);
			
			BillBoardDao.insert(insertBuilder);
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
			String title = request.getParameter("title");
			String desc = request.getParameter("desc");
			String expired = request.getParameter("expired");
			String status = request.getParameter("status");
			
			BillBoard.UpdateBuilder updateBuilder = new BillBoard.UpdateBuilder(Integer.parseInt(id));
			
			if(title != null && !title.trim().isEmpty()){
				updateBuilder.setTitle(title);
			}
			if(desc != null && !desc.trim().isEmpty()){
				updateBuilder.setBody(desc);
			}
			if(expired != null && !expired.trim().isEmpty()){
				updateBuilder.setExpired(expired);
			}
			if(status != null && !status.trim().isEmpty()){
				updateBuilder.setStatus(Status.valueOf(Integer.parseInt(status)));
			}
			
			BillBoardDao.update(updateBuilder);
			
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
			BillBoardDao.deleteById(Integer.valueOf(id));
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
