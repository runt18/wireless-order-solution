package com.wireless.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.*;
import com.wireless.order.LoginFault;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user = request.getParameter("user");
		String pwd = request.getParameter("pwd");
		Gson gson = new Gson();
		Token t = new Token();
		response.setContentType("text/html;charset=GB2312");
		try{
			String token = com.wireless.order.Login.exec(user, pwd);			
			t.token = token;
			t.msg = "验证成功";
			response.getWriter().print(gson.toJson(t));
		}catch(LoginFault e){
			t.token = null;
			if(e.errorType == LoginFault.USER_PWD_NOT_MATCHED){
				t.msg = "验证失败，帐号和密码不匹配";
				
			}else if(e.errorType == LoginFault.DB_ERROR){
				t.msg = "验证失败，未能与数据库通信";
				
			}else{
				t.msg = "验证失败，请确认帐号和密码是否准确";
			}
			response.getWriter().print(gson.toJson(t));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
}

class Token{
	String token;
	String msg;
	Token(){}
}

