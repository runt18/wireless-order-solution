package com.wireless.Actions.printScheme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperatePrinterAction extends DispatchAction{

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		String pin = request.getParameter("pin");
		String printerId = request.getParameter("printerId");
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			PrinterDao.deleteById(dbCon, staff, Integer.parseInt(printerId));
			jobject.initTip(true, "操作成功, 已删除打印机");
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
		
	}
	
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		String pin = request.getParameter("pin");
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String printerName = request.getParameter("printerName");
			String printerAlias = request.getParameter("printerAlias");
			int style =Integer.parseInt(request.getParameter("style"));
			
			Printer.InsertBuilder builder = new Printer.InsertBuilder(printerName, PStyle.valueOf(style), staff.getRestaurantId());
			builder.setAlias(printerAlias);
			PrinterDao.insert(dbCon, staff, builder);
			jobject.initTip(true, "操作成功, 已添加打印机");
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		String pin = request.getParameter("pin");
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String printerName = request.getParameter("printerName");
			String printerAlias = request.getParameter("printerAlias");
			int style =Integer.parseInt(request.getParameter("style"));
			String printerId = request.getParameter("printerId");
			String isEnabled = request.getParameter("isEnabled");
			
			Printer.UpdateBuilder builder = new Printer.UpdateBuilder(Integer.parseInt(printerId), printerName, PStyle.valueOf(style));
			builder.setAlias(printerAlias);
			builder.setEnabled(Boolean.parseBoolean(isEnabled));
			
			PrinterDao.update(dbCon, staff, builder);
			
			jobject.initTip(true, "操作成功,已修改打印机");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
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
