package com.wireless.Actions.supplierMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.supplierMgr.Supplier;

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
			
			Supplier.InsertBuilder supplier = new Supplier.InsertBuilder();
			
			supplier.setRestaurantId(staff.getRestaurantId());
			
			supplier.setName(supplierName);
			
			if(tele != null){
				supplier.setTele(tele);
			}
			
			if(addr != null){
				supplier.setAddr(addr);
			}
			
			if(contact != null){
				supplier.setContact(contact);
			}
			
			if(comment != null){
				supplier.setComment(comment);
			}
			
			SupplierDao.insert(supplier);
			jobject.initTip(true, "添加成功!");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	} 
	
}
