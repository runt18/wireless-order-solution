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
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.token.Token;

public class VerifyRestaurantAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		final String account = request.getParameter("account");
		final String restaurantId = request.getParameter("restaurantID");
		final String encryptedToken = request.getParameter("token");
		final String nextEncryptedToken;
		try {
			Restaurant r;
			if(account == null || account.trim().isEmpty()){
				r = RestaurantDao.getById(Integer.parseInt(restaurantId));
			}else{
				r = RestaurantDao.getByAccount(account);
			}
			
			jobject.setRoot(r);
			
			//FIXME 过渡阶段
			if(encryptedToken == null || encryptedToken.trim().isEmpty()){
				int tokenId = TokenDao.insert(new Token.InsertBuilder(r));
				nextEncryptedToken = TokenDao.generate(new Token.GenerateBuilder(r.getAccount(), TokenDao.getById(tokenId).getCode()));
			}else{
				nextEncryptedToken = TokenDao.verify(new Token.VerifyBuilder(account, encryptedToken));
			}
			
			Cookie cookie = new Cookie(request.getServerName() + "_digie_token", nextEncryptedToken);
			cookie.setMaxAge(365*24*60*60);
			cookie.setPath("/web-term/touch/");
			response.addCookie(cookie);
			
			//FIXME
			Cookie cookieToken = new Cookie(request.getServerName() + "_digie_token", null);
			cookieToken.setMaxAge(0);
			cookieToken.setPath("/web-term/touch");
			response.addCookie(cookieToken);
			
		}catch (BusinessException e) {
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}
}
