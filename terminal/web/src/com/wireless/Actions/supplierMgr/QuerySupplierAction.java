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
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.supplierMgr.Supplier;

public class QuerySupplierAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		try{
			//String pin = (String)request.getAttribute("pin");

			String pin = null;
			if(request.getSession().getAttribute("pin") != null){
				pin = (String)request.getAttribute("pin");
			}
			
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			String name = request.getParameter("name");
			String op = request.getParameter("op");
			String tele = request.getParameter("tele");
			String contact = request.getParameter("contact");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "";
			if(start !=  null && limit != null){
				if(op != null && op.equals("e")){
					extraCond = " AND name LIKE '%" + (name != null ? name : " ") + 
							"%' AND tele LIKE '%" + (tele != null ? tele : "") + 
							"%' AND contact LIKE '%" + (contact != null ? contact : "") + "%'" +
							"ORDER BY " +
							"supplier_id LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit) + "";
				}else{
					extraCond = "ORDER BY " +
							"supplier_id LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit) + "";
				}
										
			}
			int roots = SupplierDao.getSupplierCount(staff, extraCond);
			
			List<Supplier> root = SupplierDao.getSuppliers(staff, extraCond, null);
			
		    jobject.setTotalProperty(roots);
		    jobject.setRoot(root);
		    
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), e.getCode(), e.getDesc());

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
