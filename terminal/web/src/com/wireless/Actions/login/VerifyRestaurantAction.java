package com.wireless.Actions.login;

import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.token.TokenDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.token.Token;

public class VerifyRestaurantAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final JObject jObject = new JObject();
		final String account = request.getParameter("account");
		final String encryptedToken = request.getParameter("token");
		final String nextEncryptedToken;
		try {
			jObject.setRoot(RestaurantDao.getByAccount(account));
			
			nextEncryptedToken = TokenDao.verify(new Token.VerifyBuilder(account, encryptedToken));
			
			Cookie cookie = new Cookie(request.getServerName() + "_digie_token", nextEncryptedToken);
			cookie.setMaxAge(365 * 24 * 3600);
			cookie.setPath("/web-term/touch/");
			response.addCookie(cookie);
			
		}catch (BusinessException | SQLException e) {
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}		
		return null;
	}
}
