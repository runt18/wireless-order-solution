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
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteSupplierAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");;
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			int supplierId =Integer.valueOf(request.getParameter("supplierId"));
			SupplierDao.deleteById(staff, supplierId);
			jobject.initTip(true, "删除成功!");
			
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
