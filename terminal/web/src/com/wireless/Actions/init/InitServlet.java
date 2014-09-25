package com.wireless.Actions.init;

import java.beans.PropertyVetoException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;

import com.wireless.db.DBCon;
import com.wireless.db.oss.OSSParams;
import com.wireless.pojo.oss.OssImage;
import com.wireless.sccon.ServerConnector;

public class InitServlet extends ActionServlet {
	
	private static ServletConfig sc;
	
	public static ServletConfig getConfig() {
		return sc;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7899564175504837996L;

	@Override
	public void init() throws ServletException{
		sc = getServletConfig();
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
			OssImage.Params.init(getServletConfig().getInitParameter("oss_bucket"), OSSParams.instance());
			/**/
			//OSSUtil.init(OSSParams.instance(), getServletConfig().getInitParameter("oss_bucket_image"));
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		ServerConnector.instance().setNetAddr(getServletConfig().getInitParameter("socket_host"));
		ServerConnector.instance().setNetPort(Integer.parseInt(getServletConfig().getInitParameter("socket_port")));
		
		super.init();
	}
}
