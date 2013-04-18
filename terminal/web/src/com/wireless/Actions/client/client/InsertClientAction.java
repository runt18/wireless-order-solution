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

public class InsertClientAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject= new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
//			String clientID = request.getParameter("clientID");
			String clientName = request.getParameter("clientName");
			String clientType = request.getParameter("clientType");
			String clientSex = request.getParameter("clientSex");
			String clientMobile = request.getParameter("clientMobile");
			String clientTele = request.getParameter("clientTele");
			String clientBirthday = request.getParameter("clientBirthday");
			String clietnIDCard = request.getParameter("clietnIDCard");
			String clientCompany = request.getParameter("clientCompany");
			String clientTastePref = request.getParameter("clientTastePref");
			String clietTaboo = request.getParameter("clietTaboo");
			String clientContactAddress = request.getParameter("clientContactAddress");
			String clientComment = request.getParameter("clientComment");
			
			Client c = new Client();
			c.setRestaurantID(Integer.valueOf(restaurantID));
			c.setName(clientName.trim());
			c.setClientType(Integer.valueOf(clientType), null, 0, Integer.valueOf(restaurantID));
			c.setSex(Integer.valueOf(clientSex));
			c.setMobile(clientMobile);
			c.setTele(clientTele);
			c.setBirthday(clientBirthday);
			c.setIDCard(clietnIDCard);
			c.setCompany(clientCompany);
			c.setTastePref(clientTastePref);
			c.setTaboo(clietTaboo);
			c.setContactAddress(clientContactAddress);
			c.setComment(clientComment);
			
			ClientDao.insertClient(c);
			jobject.initTip(true, "操作成功, 已添加新客户资料.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
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
