package com.wireless.Actions.client.clientType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.ClientDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.ClientType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class InsertClientTypeAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String restaurantID = request.getParameter("restaurantID");
			String typeName = request.getParameter("typeName");
			String typeParentID = request.getParameter("typeParentID");
			
			ClientType ct = new ClientType(0, typeName.trim(), Integer.valueOf(typeParentID), Integer.valueOf(restaurantID));
			
			ClientDao.insertClientType(ct);
			
			jobject.initTip(true, "操作成功, 已添加新客户类型.");
		
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.errCode, e.getMessage());
			e.printStackTrace();
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
