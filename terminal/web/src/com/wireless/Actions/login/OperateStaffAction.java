package com.wireless.Actions.login;

import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.token.TokenDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.token.Token;


public class OperateStaffAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		final String pin = request.getParameter("pin");
		final String pwd = request.getParameter("pwd");
		final String account = request.getParameter("account");
		final String token = request.getParameter("token");
		final JObject jObject = new JObject();
		try{
			
			final Staff	staff = StaffDao.verify(Integer.parseInt(pin));
			 
			if(staff.getPwd().equals(pwd)){
				//再次验证token
				String nextEncryptedToken = TokenDao.verify(new Token.VerifyBuilder(account, token));
				
				Cookie cookie = new Cookie(request.getServerName() + "_digie_token", nextEncryptedToken);
				cookie.setMaxAge(365 * 24 * 3600);
				cookie.setPath("/web-term/touch/");
				response.addCookie(cookie);
				
				final HttpSession session = request.getSession();
				session.setAttribute("pin", Integer.toString(staff.getId()));
				session.setAttribute("restaurantID", staff.getRestaurantId()+"");
				session.setAttribute("dynamicKey", System.currentTimeMillis() % 100000);
				jObject.initTip(true, "登陆成功");				
				
			}else{
//				tokenContent = null;
				jObject.initTip(false, "密码输入错误");
			}
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("staff", staff, 1);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});			

		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
}
