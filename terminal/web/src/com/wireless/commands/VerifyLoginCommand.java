package com.wireless.commands;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.chain.commands.ActionCommandBase;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.chain.contexts.ServletActionContext;

public class VerifyLoginCommand extends ActionCommandBase{

	@Override
	public boolean execute(ActionContext context) throws Exception {
		HttpServletRequest request = ((ServletActionContext)context).getRequest();
		String skipVerify = request.getParameter("skipVerify");
		String isCookie = request.getParameter("isCookie");
		boolean bool = false;
		if(skipVerify == null){
			String pin = null;
			if(isCookie == null){
				pin = (String)request.getSession().getAttribute("pin");
				if(pin == null){
					bool = true;
				}
				
			}else{
				Cookie[] cookies = request.getCookies();
				if(cookies != null){
					for (Cookie cookie : cookies) {
						if(cookie.getName().equalsIgnoreCase("pin")){
							pin = cookie.getValue();
						}
					}
				}
			}
			request.setAttribute("pin", pin);
		}
		//false : continue
		return bool;
	}

}
