package com.wireless.Actions.init;

import java.beans.PropertyVetoException;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.sccon.ServerConnector;

public class InitServlet extends ActionServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7899564175504837996L;

	@Override
	public void init() throws ServletException{

		Params.setDatabase(getServletConfig().getInitParameter("db_name"));
		Params.setDbHost(getServletConfig().getInitParameter("db_host"));
		Params.setDbPort(Integer.parseInt(getServletConfig().getInitParameter("db_port")));
		Params.setDbUser(getServletConfig().getInitParameter("db_user"));
		Params.setDbPwd(getServletConfig().getInitParameter("db_pwd"));
		try {
			DBCon.init(Params.dbHost, Integer.toString(Params.dbPort), Params.dbName, Params.dbUser, Params.dbPwd);
		} catch (PropertyVetoException e) {
			throw new ServletException(e);
		}
		ServerConnector.instance().setNetAddr(getServletConfig().getInitParameter("socket_host"));
		ServerConnector.instance().setNetPort(Integer.parseInt(getServletConfig().getInitParameter("socket_port")));
		super.init();
	}
}
