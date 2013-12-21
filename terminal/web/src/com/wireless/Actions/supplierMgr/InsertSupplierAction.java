package com.wireless.Actions.supplierMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertSupplierAction extends Action {
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String supplierName = request.getParameter("supplierName");
			String tele = request.getParameter("tele");
			String addr = request.getParameter("addr");
			String contact = request.getParameter("contact");
			String comment = request.getParameter("comment");
			
			Supplier supplier = new Supplier(staff.getRestaurantId(), supplierName, tele, addr, contact, comment);
			SupplierDao.insert(supplier);
			jobject.initTip(true, "添加成功!");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), e.getCode(), e.getDesc());

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json);
		}
		return null;
	} 
	
}
