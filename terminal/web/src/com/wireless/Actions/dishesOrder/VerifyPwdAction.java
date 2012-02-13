package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.VerifyPwd;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class VerifyPwdAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & type=1 & pwd = fc875df90919bb4a8ac1b0795df4025c"
			 * 
			 * pin : the pin the this terminal
			 * 
			 * type : "1" means password_1
			 * 		  "2" means password_2
			 * 		  "3" means password_3
			 * 
			 * pwd : the password to verify, which is in the form of MD5
			 */
			String pin = request.getParameter("pin");
			
			int type = VerifyPwd.PASSWORD_2;
			if(request.getParameter("type") != null){
				type = Integer.parseInt(request.getParameter("type"));
			}
			
			String pwd;
			if(request.getParameter("pwd") != null){
				pwd = request.getParameter("pwd");
			}else{
				pwd = "";
			}
			
			boolean isMatch = VerifyPwd.exec(Long.parseLong(pin), Terminal.MODEL_STAFF, type, pwd);
			jsonResp = jsonResp.replace("$(result)", isMatch ? "true" : "false");
			jsonResp = jsonResp.replace("$(value)", isMatch ? "密码验证通过" : "密码验证失败");	
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");	
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "密码验证失败，请重新确认");	
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			
		}finally{
			//just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
}
