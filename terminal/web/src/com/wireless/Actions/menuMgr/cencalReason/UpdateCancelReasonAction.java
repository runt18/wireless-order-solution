package com.wireless.Actions.menuMgr.cencalReason;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateCancelReasonAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		JSONObject centent = null;
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String cancelReason = request.getParameter("cancelReason");
			centent = JSONObject.fromObject(cancelReason);
			CancelReason cr = (CancelReason) JSONObject.toBean(centent, CancelReason.class);
			MenuDao.updateCancelReason(cr);
			jobject.initTip(true, "操作成功, 已修改退菜原因信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			centent = JSONObject.fromObject(jobject);
			response.getWriter().print(centent.toString());
		}
		return null;
	}

}
