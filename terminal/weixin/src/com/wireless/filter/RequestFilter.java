package com.wireless.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestFilter implements Filter{

	
	@Override
	public void destroy() {
		
	} 

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		chain.doFilter(request, response);
		
		String ct = request.getContentType();
		if(ct != null && ct.split(";")[0].equalsIgnoreCase("multipart/form-data")){
			response.setContentType("text/plain; charset=utf-8");
		}else{
			response.setContentType("text/json; charset=utf-8");
		}
		
	}

	@Override
	public void init(FilterConfig init) throws ServletException {
		
	}
	
}
