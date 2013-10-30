package com.wireless.Actions.init;

import java.beans.PropertyVetoException;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;

import com.wireless.db.DBCon;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.OSSUtil;

public class InitServlet extends ActionServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7899564175504837996L;

	@Override
	public void init() throws ServletException{

		try {
			DBCon.init(getServletConfig().getInitParameter("db_host"),
					   getServletConfig().getInitParameter("db_port"), 
					   getServletConfig().getInitParameter("db_name"),
					   getServletConfig().getInitParameter("db_user"), 
					   getServletConfig().getInitParameter("db_pwd"));
			
		} catch (PropertyVetoException e) {
			throw new ServletException(e);
		}
		try {
			OSSUtil.init(getServletConfig().getInitParameter("OSS_ACCESS_ID"),
					getServletConfig().getInitParameter("OSS_ACCESS_KEY"), 
					getServletConfig().getInitParameter("OSS_BUCKET_IMAGE"));
			OSSUtil.initClient(getServletConfig().getInitParameter("OSS_INNER_POINT"),
					getServletConfig().getInitParameter("OSS_OUTER_POINT"));
		} catch (Exception e) {
			throw new ServletException(e);
		}
		ServerConnector.instance().setNetAddr(getServletConfig().getInitParameter("socket_host"));
		ServerConnector.instance().setNetPort(Integer.parseInt(getServletConfig().getInitParameter("socket_port")));
		super.init();
	}
}
