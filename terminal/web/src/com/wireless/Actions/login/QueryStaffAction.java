package com.wireless.Actions.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.QueryStaff;
import com.wireless.protocol.Staff;

public class QueryStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			int restaurantID = Integer.parseInt(request.getParameter("restaurantID"));
			Staff[] staffs = QueryStaff.exec(restaurantID);
			
			if(staffs.length != 0){
				jsonResp = jsonResp.replace("$(result)", "true");
				/**
				 * The json staff format looks like below
				 * {[1,"李颖宜","d7a7b87838c6e3853f3f6d3bdc836a7c"]，[2,"张宁远","fe01ce2a7fbac8fafaed7c982a04e229"]}
				 * Note that the password to each staff is in the form of md5
				 */
				StringBuffer value = new StringBuffer();
				for (int i = 0; i < staffs.length; i++) {
					String jsonStaff = "[$(pin),\"$(name)\",\"$(pwd)\"]";
					jsonStaff = jsonStaff.replace("$(pin)", Integer.toString(staffs[i].pin));
					jsonStaff = jsonStaff.replace("$(name)", staffs[i].name);
					jsonStaff = jsonStaff.replace("$(pwd)", staffs[i].pwd);
					// pub each json staff info to the value
					value.append(jsonStaff);
					// the string is separated by comma
					if (i != staffs.length - 1) {
						value.append("，");
					}
				}
				jsonResp = jsonResp.replace("$(value)", value.toString());
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", "您的餐厅还没有任何员工信息，请在会员中心中添加员工");	
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			
		}finally{
			//Just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
}
