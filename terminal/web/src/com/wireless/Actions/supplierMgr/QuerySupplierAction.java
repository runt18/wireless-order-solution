package com.wireless.Actions.supplierMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.supplierMgr.Supplier;

public class QuerySupplierAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		String pin = (String)request.getAttribute("pin");
		if(request.getSession().getAttribute("pin") != null){
			pin = (String)request.getAttribute("pin");
		}
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String name = request.getParameter("name");
		final String op = request.getParameter("op");
		final String tele = request.getParameter("tele");
		final String contact = request.getParameter("contact");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jObject = new JObject();
		try{

			SupplierDao.ExtraCond extraCond = new SupplierDao.ExtraCond();
			if(start !=  null && limit != null){
				if(op != null && op.equals("e")){
					extraCond.setContact(contact);
					extraCond.setName(name);
					extraCond.setTelePhone(tele);
				}
				extraCond.setLimit(Integer.parseInt(start), Integer.parseInt(limit));
										
			}
			int roots = SupplierDao.getSupplierCount(staff, extraCond, " ORDER BY supplier_id ");
			
			List<Supplier> root = SupplierDao.getByCond(staff, extraCond, " ORDER BY supplier_id ");
			
		    jObject.setTotalProperty(roots);
		    jObject.setRoot(root);
		    
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
