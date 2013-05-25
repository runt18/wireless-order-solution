package com.wireless.Actions.supplierMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertSupplierAction extends Action {
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			String supplierName = request.getParameter("supplierName");
			String tele = request.getParameter("tele");
			String addr = request.getParameter("addr");
			String contact = request.getParameter("contact");
			String comment = request.getParameter("comment");
			
			Supplier supplier = new Supplier(term.restaurantID, supplierName, tele, addr, contact, comment);
			SupplierDao.insert(supplier);
			jobject.initTip(true, "添加成功!");
			
		}catch(Exception e){
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	} 
	
}
