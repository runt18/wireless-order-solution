package com.wireless.Actions.distMgr.discount;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.pojo.distMgr.Discount;
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
			
			List<Discount> discounts = DiscountDao.getPureDiscount(dbCon, 
					(VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF)), 
					" AND DIST.status <> " + Discount.Status.MEMBER_TYPE.getVal(), 
					" ORDER BY DIST.level DESC");
			
			int i = 0;
			for(Discount discount : discounts){
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
				tsb.append("level:" + discount.getLevel());
				tsb.append(",");
				tsb.append("restaurantID:" + discount.getRestaurantId());
				tsb.append(",");
				tsb.append("isDefault:" + (discount.isDefault() || discount.isDefaultReserved()));
				tsb.append(",");
				tsb.append("status:" + discount.getStatus().getVal());
				tsb.append("}");
				i++;
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
