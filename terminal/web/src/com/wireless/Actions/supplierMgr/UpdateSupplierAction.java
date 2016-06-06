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

public class UpdateSupplierAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		JObject jobject = new JObject();
		try{
			String supplierId = request.getParameter("supplierID");
			String name = request.getParameter("supplierName");
			String tele = request.getParameter("tele");
			String addr = request.getParameter("addr");
			String contact = request.getParameter("contact");
			String comment = request.getParameter("comment");
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Supplier.UpdateBuilder supplier = new Supplier.UpdateBuilder(Integer.valueOf(supplierId)).setRestaurantId(staff.getRestaurantId());
//			, staff.getRestaurantId(), name, tele, addr, contact, comment
			
			if(name != null){
				supplier.setName(name);
			}
			
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
			
			SupplierDao.update(staff, supplier);
			jobject.initTip(true, "修改成功");
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
