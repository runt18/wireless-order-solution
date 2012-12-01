package com.wireless.Actions.client.memberType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.MemberDao;
import com.wireless.pojo.client.MemberType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteMemberTypeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String typeID = request.getParameter("typeID");
			String discountType = request.getParameter("discountType");
			String discountID = request.getParameter("discountID");
			
			if(restaurantID == null || typeID == null || discountID == null || discountType == null){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9970, "操作失败, 会员类型相关信息不完整.");
				return null;
			}
			
			MemberType mt = new MemberType();
			
			mt.setRestaurantID(Integer.valueOf(restaurantID));
			mt.setTypeID(Integer.valueOf(typeID));
			mt.setDiscountType(Integer.valueOf(discountType));
			mt.setDiscountID(Integer.valueOf(discountID));
			
			MemberDao.deleteMemberType(mt);
			
			jobject.initTip(true, "操作成功, 已删除会员类型相关信息.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
