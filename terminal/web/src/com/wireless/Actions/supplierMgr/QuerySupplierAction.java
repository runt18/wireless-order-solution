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
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.protocol.Terminal;

public class QuerySupplierAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		StringBuffer jsonSB = new StringBuffer();
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			String extraCond = "";
			String orderClause = "";
			List<Supplier> suppliers = SupplierDao.getSuppliers(term, extraCond, orderClause);
			int index = 0;
			jsonSB.append("{totalProperty:" + suppliers.size() + ",rows:[");

			for(int i = 0;i < suppliers.size(); i++){
				jsonSB.append(index > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("supplierID : '" + suppliers.get(i).getSupplierId() + "'");
				jsonSB.append(",");
				jsonSB.append("name : '" + suppliers.get(i).getName()+ "'");
				jsonSB.append(",");
				jsonSB.append("tele : '" + suppliers.get(i).getTele()+ "'");
				jsonSB.append(",");
				jsonSB.append("addr : '" + suppliers.get(i).getAddr()+ "'");
				jsonSB.append(",");
				jsonSB.append("contact : '"+ suppliers.get(i).getContact() + "'");
				jsonSB.append(",");
				jsonSB.append("comment : '"+ suppliers.get(i).getComment() + "'");
				jsonSB.append("}");
				index++;
			}
			jsonSB.append("]}");
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jsonSB.toString());
		}
		return null;
	}
}
