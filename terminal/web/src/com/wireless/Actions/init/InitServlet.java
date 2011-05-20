package com.wireless.Actions.init;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;

import com.wireless.db.Params;

public class InitServlet extends ActionServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7899564175504837996L;

	public void init() throws ServletException{

		Params.setDatabase(getServletConfig().getInitParameter("db_name"));
		Params.setDbHost(getServletConfig().getInitParameter("db_host"));
		Params.setDbPort(Integer.parseInt(getServletConfig().getInitParameter("db_port")));
		Params.setDbUser(getServletConfig().getInitParameter("db_user"));
		Params.setDbPwd(getServletConfig().getInitParameter("db_pwd"));
		super.init();
	}
}
