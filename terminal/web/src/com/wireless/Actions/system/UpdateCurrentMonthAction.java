package com.wireless.Actions.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Terminal;
import com.wireless.json.JObject;
import com.wireless.util.WebParams;

public class UpdateCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			Terminal mTerminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			SystemDao.updateCurrentMonth(mTerminal);
			jobject.initTip(true, "操作成功, 已经月结.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		} finally {
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
