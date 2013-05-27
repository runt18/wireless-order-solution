package com.wireless.Actions.supplierMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.json.JObject;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.protocol.Terminal;
import com.wireless.util.WebParams;

public class QuerySupplierAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = null;
		try{
			String pin = request.getParameter("pin");
			int start = Integer.parseInt(request.getParameter("start").toString());
			int limit = Integer.parseInt(request.getParameter("limit").toString());
			String name = request.getParameter("name");
			String op = request.getParameter("op");
			String tele = request.getParameter("tele");
			String contact = request.getParameter("contact");
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			String extraCond;
			if(op != null && op.equals("e")){
				extraCond = " AND name LIKE '%" + (name != null ? name : " ") + 
						"%' AND tele LIKE '%" + (tele != null ? tele : "") + 
						"%' AND contact LIKE '%" + (contact != null ? contact : "") + "%'"  ;
			}else{
				extraCond = "";
			}
			int roots = SupplierDao.getSupplierCount(term, extraCond);
			extraCond = extraCond + 
					"ORDER BY " +
					"supplier_id LIMIT " + start + ", " + limit + "";
			List<Supplier> root = SupplierDao.getSuppliers(term, extraCond, null);
			
		    jobject = new JObject(roots, root);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			//JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
