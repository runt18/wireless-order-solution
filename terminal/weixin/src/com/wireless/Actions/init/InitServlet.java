package com.wireless.Actions.init;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.ComponentAccessToken;
import org.marker.weixin.auth.ComponentVerifyTicket;
import org.marker.weixin.auth.PreAuthCode;

import com.wireless.db.DBCon;
import com.wireless.json.JObject;
import com.wireless.pojo.oss.OSSParams;
import com.wireless.pojo.oss.OssImage;
import com.wireless.sccon.ServerConnector;

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
					   getServletConfig().getInitParameter("db_pwd"),
					   false);
			
		} catch (PropertyVetoException e) {
			throw new ServletException(e);
		}
		
		OssImage.Params.init(getServletConfig().getInitParameter("oss_bucket"), 
							 OSSParams.init(getServletConfig().getInitParameter("oss_access_id"),
									 	    getServletConfig().getInitParameter("oss_access_key"), 
									 	    getServletConfig().getInitParameter("oss_inner_point"), 
									 	    getServletConfig().getInitParameter("oss_outer_point")));
		
		ServerConnector.instance().setMaster(new ServerConnector.Connector(getServletConfig().getInitParameter("socket_host"), Integer.parseInt(getServletConfig().getInitParameter("socket_port"))));
		
		AuthParam.APP_ID = getServletConfig().getInitParameter("component_app_id");
		AuthParam.APP_SECRET = getServletConfig().getInitParameter("component_app_secret");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("/home/yzhang/www/wx-term/ticket.txt"));
	        StringBuilder sb = new StringBuilder();
	        String temp = br.readLine();
	        while(temp != null){
	        	sb.append(temp);
	            temp = br.readLine();
	        }
	        br.close();
	        
	        AuthParam.COMPONENT_VERIFY_TICKET = JObject.parse(ComponentVerifyTicket.JSON_CREATOR, 0, sb.toString());
	        AuthParam.COMPONENT_ACCESS_TOKEN = ComponentAccessToken.newInstance(AuthParam.COMPONENT_VERIFY_TICKET);
	        AuthParam.PRE_AUTH_CODE = PreAuthCode.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN);
		    
		} catch (IOException e) {
			e.printStackTrace();
		}

	         
		super.init();
	}
}
