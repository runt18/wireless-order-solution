package com.wireless.Actions.menuMgr.cencalReason;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.crMgr.CancelReason;

public class UpdateCancelReasonAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String id = request.getParameter("id");
			String reason = request.getParameter("reason");
			CancelReason.UpdateBuilder builder = new CancelReason.UpdateBuilder(Integer.parseInt(id), reason);
			CancelReasonDao.update(builder);
			jobject.initTip(true, "操作成功, 已修改退菜原因信息.");
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
