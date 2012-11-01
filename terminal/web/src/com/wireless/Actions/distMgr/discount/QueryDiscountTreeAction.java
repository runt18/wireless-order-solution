package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.VerifyPin;
import com.wireless.db.distMgr.QueryDiscountDao;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.protocol.Terminal;

public class QueryDiscountTreeAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		DBCon dbCon = new DBCon();
		StringBuffer tsb = new StringBuffer();
		
		try{
			dbCon.connect();
			
			String pin = request.getParameter("pin");
			
			DiscountPojo[] discount = QueryDiscountDao.execPureDiscount(dbCon, 
					(VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF)), 
					null, 
					" ORDER BY DIST.level DESC");
			
			for(int i = 0; i < discount.length; i++){
				tsb.append(i > 0 ? "," : "");
				tsb.append("{");
				tsb.append("leaf:true");
				tsb.append(",");
				tsb.append("text:'" + discount[i].getName() + "'" );
				tsb.append(",");
				tsb.append("discountID:" + discount[i].getId());
				tsb.append(",");
				tsb.append("discountName:'" + discount[i].getName() + "'");
				tsb.append(",");
				tsb.append("level:" + discount[i].getLevel());
				tsb.append(",");
				tsb.append("restaurantID:" + discount[i].getRestaurantID());
				tsb.append(",");
				tsb.append("isDefault:" + discount[i].isDefaultOrDefaultReserved());
				tsb.append(",");
				tsb.append("status:" + discount[i].getStatus());
				tsb.append("}");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print("[" + tsb.toString() + "]");
		}
		return null;
	}
	
	
	
}
