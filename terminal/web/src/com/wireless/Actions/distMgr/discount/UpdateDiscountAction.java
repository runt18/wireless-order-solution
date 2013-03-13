package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.QueryDiscountDao;
import com.wireless.pojo.distMgr.Discount;
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
			String resturantID = request.getParameter("restaurantID");
			String discountID = request.getParameter("discountID");
			String discountName = request.getParameter("discountName");
			String level = request.getParameter("level");
			String status = request.getParameter("status");
//			String isDefault = request.getParameter("isDefault");
			
			Discount pojo = new Discount();
			pojo.setId(Integer.valueOf(discountID));
			pojo.setName(discountName.trim());
			pojo.setRestaurantID(Integer.valueOf(resturantID));
			pojo.setLevel(Integer.valueOf(level));
			pojo.setStatus(Integer.valueOf(status));
//			pojo.setDefault(Boolean.valueOf(isDefault));
			
			QueryDiscountDao.updateDiscount(pojo);
			
			jobject.initTip(true,  "操作成功, 已修改折扣方案!");
			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
			e.printStackTrace();
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
