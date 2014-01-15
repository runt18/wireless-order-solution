package com.wireless.Actions.init;

import java.beans.PropertyVetoException;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;

import com.wireless.db.DBCon;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.OSSParams;
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
			OSSParams.init(getServletConfig().getInitParameter("oss_access_id"),
					   	   getServletConfig().getInitParameter("oss_access_key"), 
					   	   getServletConfig().getInitParameter("oss_inner_point"), 
					   	   getServletConfig().getInitParameter("oss_outer_point"));
			/**/
			OSSUtil.init(OSSParams.instance(), getServletConfig().getInitParameter("oss_bucket_image"));
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		ServerConnector.instance().setNetAddr(getServletConfig().getInitParameter("socket_host"));
		ServerConnector.instance().setNetPort(Integer.parseInt(getServletConfig().getInitParameter("socket_port")));
		
		super.init();
	}
}
