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
		//FIX ME!!!
		//The parameters should be read from the configuration file
		Params.setDatabase("wireless_order_db");
		Params.setDbHost("192.168.123.130");
		Params.setDbPort(3306);
		Params.setDbUser("yzhang");
		Params.setDbPwd("HelloZ315");
		Params.setSocketHost("127.0.0.1");
		Params.setSocketPort(55555);
		super.init();
	}
}
