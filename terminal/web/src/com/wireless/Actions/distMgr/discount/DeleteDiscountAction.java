package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteDiscountAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		
		try{
			String resturantID = request.getParameter("restaurantID");
			String discountID = request.getParameter("discountID");
			
			Discount pojo = new Discount();
			pojo.setRestaurantId(Integer.valueOf(resturantID));
			pojo.setId(Integer.valueOf(discountID));
			
			DiscountDao.deleteDiscount(pojo);
			
			jobject.initTip(true, "操作成功, 已删除选中方案信息.");
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_WARNING, 9997, "操作失败, 该方案下有分厨折扣,不允许删除!");
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
