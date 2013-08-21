package com.wireless.commands;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.chain.commands.ActionCommandBase;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.chain.contexts.ServletActionContext;

public class VerifyLoginCommand extends ActionCommandBase{

	@Override
	public boolean execute(ActionContext context) throws Exception {
		HttpServletRequest request = ((ServletActionContext)context).getRequest();
		String skipVerify = request.getParameter("skipVerify");
		if(skipVerify == null){
			String pin = (String)request.getSession().getAttribute("pin");
			request.setAttribute("pin", pin);
		}
		return false;
	}

}
