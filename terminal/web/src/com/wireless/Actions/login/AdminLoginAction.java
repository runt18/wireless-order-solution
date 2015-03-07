package com.wireless.Actions.login;

import java.sql.SQLException;
import java.util.List;

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
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;

public class AdminLoginAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		JObject jobject = new JObject();
		final Staff theStaff;
		try{
			Staff staff = null;
				List<Staff> list = StaffDao.getByRestaurant(Restaurant.ADMIN);
				for (Staff s : list) {
					if(s.getName().equals(name)){
						staff = s;
						break;
					}
				}
				if(staff == null){
					jobject.initTip(false, "账号输入错误");
				}
			 
			if(staff != null && staff.getPwd().equals(pwd)){
				HttpSession session = request.getSession();
				session.setAttribute("pin", staff.getId() + "");
				session.setAttribute("restaurantID", staff.getRestaurantId()+"");
				theStaff = staff;
				jobject.initTip(true, "登陆成功");
			}else{
				theStaff = null;
				jobject.initTip(false, "密码输入错误");
			}
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					if(theStaff != null){
						jm.putJsonable("staff", theStaff, 1);
					}
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
