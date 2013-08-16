package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateDiscountAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin), Privilege.Code.BASIC);
			
			String resturantID = request.getParameter("restaurantID");
			String discountID = request.getParameter("discountID");
			String discountName = request.getParameter("discountName");
			String level = request.getParameter("level");
			String status = request.getParameter("status");
//			String isDefault = request.getParameter("isDefault");
			
			Discount pojo = new Discount();
			pojo.setId(Integer.valueOf(discountID));
			pojo.setName(discountName.trim());
			pojo.setRestaurantId(Integer.valueOf(resturantID));
			pojo.setLevel(Integer.valueOf(level));
			pojo.setStatus(Integer.valueOf(status));
//			pojo.setDefault(Boolean.valueOf(isDefault));
			
			DiscountDao.updateDiscount(pojo);
			
			jobject.initTip(true,  "操作成功, 已修改折扣方案!");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
