package com.wireless.Actions.client.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.client.ClientDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Client;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteClientAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject= new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String clientID = request.getParameter("clientID");
			
			Client c = new Client();
			c.setRestaurantID(Integer.valueOf(restaurantID));
			c.setClientID(Integer.valueOf(clientID));
			
			ClientDao.deleteClient(c);
			jobject.initTip(true, "操作成功, 已删除客户相关资料.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();	
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
