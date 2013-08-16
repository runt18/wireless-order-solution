package com.wireless.Actions.printScheme;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.WebParams;

public class QueryPrintFuncAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<PrintFunc> root = new ArrayList<PrintFunc>();
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin), Privilege.Code.BASIC);
			
			String printerId = request.getParameter("printerId");
			root = PrintFuncDao.getFuncByPrinterId(Integer.parseInt(printerId));
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
