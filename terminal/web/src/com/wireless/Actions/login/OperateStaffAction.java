package com.wireless.Actions.login;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;


public class OperateStaffAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		String pin = request.getParameter("pin");
		String pwd = request.getParameter("pwd");
		//String account = request.getParameter("account");
//		String token = request.getParameter("token");
		JObject jobject = new JObject();
		final Staff theStaff;
//		final String tokenContent;
		try{
			Staff staff = null;
			if(pin != null && !pin.trim().isEmpty()){
				staff = StaffDao.verify(Integer.parseInt(pin));
			}
			 
			if(staff != null && staff.getPwd().equals(pwd)){
				//再次验证token
				//int tokenId = TokenDao.verify(new Token.VerifyBuilder(account, token));
				//tokenContent = TokenDao.getById(tokenId).encrypt();
//				tokenContent = token;
				
				pin = staff.getId() + "";
				HttpSession session = request.getSession();
				session.setAttribute("pin", pin);
				session.setAttribute("restaurantID", staff.getRestaurantId()+"");
				session.setAttribute("dynamicKey", System.currentTimeMillis() % 100000);
				theStaff = staff;
				jobject.initTip(true, "登陆成功");				
				
			}else{
				theStaff = null;
//				tokenContent = null;
				jobject.initTip(false, "密码输入错误");
			}
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					if(theStaff != null){
						jm.putJsonable("staff", theStaff, 1);
					}
//					jm.putString("token", tokenContent);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});			

		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
}
