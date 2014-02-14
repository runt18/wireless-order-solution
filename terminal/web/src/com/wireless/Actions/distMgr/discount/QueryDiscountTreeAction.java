package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.Discount;

public class QueryDiscountTreeAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		DBCon dbCon = new DBCon();
		StringBuffer tsb = new StringBuffer();
		try{
			dbCon.connect();
			String pin = (String)request.getAttribute("pin");
			
			int i = 0;
			for(Discount discount : DiscountDao.getPureAll(dbCon, StaffDao.getStaffById(Integer.parseInt(pin)))){
				tsb.append(i > 0 ? "," : "");
				tsb.append("{");
				tsb.append("leaf:true");
				tsb.append(",");
				tsb.append("text:'" + discount.getName() + "'" );
				tsb.append(",");
				tsb.append("discountID:" + discount.getId());
				tsb.append(",");
				tsb.append("discountName:'" + discount.getName() + "'");
				tsb.append(",");
				tsb.append("restaurantID:" + discount.getRestaurantId());
				tsb.append(",");
				tsb.append("status:" + discount.getStatus().getVal());
				tsb.append(",type:" + discount.getType().getVal());
				tsb.append("}");
				i++;
			}
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print("[" + tsb.toString() + "]");
		}
		return null;
	}
	
	
	
}
