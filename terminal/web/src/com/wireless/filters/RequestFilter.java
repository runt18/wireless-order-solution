package com.wireless.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wireless.util.Encrypt;

public class RequestFilter implements Filter{

	private static final String SKIPVERIFY = "skipVerify";
	private List<String> skipVerifyList = new ArrayList<String>();
	private static final String DEFREDIRECT = "./pages/PersonLoginTimeout.html";
	
	private boolean check(String path){
		if(skipVerifyList == null || skipVerifyList.size() <= 0){
			return false;
		}
		for (String skip : skipVerifyList) {
			if(path.indexOf(skip) > -1){
				return true;
			}
		}
		return false;
	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	} 

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		String requestPath = request.getRequestURI();
		if(check(requestPath)){
			String pin =  (String) request.getSession().getAttribute("pin");
			if(pin != null){
				request.setAttribute("pin", pin);
			}
			chain.doFilter(request, response);
		}else{
			//FIXME 
			//The code below just for print scheme port, should be removed in future
			String url = requestPath + "?" + request.getQueryString();
			if(url.contains("/OperatePrinter.do?skipVerify&dataSource=port") ||
			   url.contains("/OperatePrintFunc.do?skipVerify&dataSource=port")){
				chain.doFilter(request, response);
				return;
			}
			//-----------------------------------------------------------------------
			
			String isCookie = null;
			Map<String, String> params = new HashMap<String, String>();
			
			if(request.getQueryString() != null && (requestPath.indexOf(".do") < 0)){
				//获取url带的参数
				String query = request.getQueryString();
				String des = Encrypt.strDecode(query, "mi", null, null);
				
				String[] urlParam = des.split("&");
				//分解参数
				for (int i = 0; i < urlParam.length; i++) {
					int num = urlParam[i].indexOf("=");
					if (num > 0) {
						String name = urlParam[i].substring(0, num);
						String value = urlParam[i].substring(num + 1);
						params.put(name, value);
					}
				}
				
			}else{
				//获取ajax参数
				isCookie = request.getParameter("isCookie");
			}
			
			String pin = null;
			Cookie c = null;
			Cookie[] cookies = request.getCookies();
			if(cookies != null){
				for (Cookie cookie : cookies) {
					if(cookie.getName().equals("pin")){
						c = cookie;
					}
				}
			}
			//是否用cookie
			if(isCookie == null){
				pin = (String) request.getSession().getAttribute("pin");
				if(pin == null){
					if (request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {  
	                    response.addHeader("sessionstatus", "timeout");  
	                }else{
	                	response.sendRedirect(DEFREDIRECT + "?" + Encrypt.strEncode("restaurantID="+params.get("restaurantID"), "mi", null, null));
	
	                }
					if(c!=null){
						c.setMaxAge(0);
						response.addCookie(c);
					}
                	
					
				}else{
					request.setAttribute("pin", pin);
					chain.doFilter(request, response);
				}
				
			}else{
				/*Cookie[] cookies = request.getCookies();
				if(cookies != null){
					for (Cookie cookie : cookies) {
						if(cookie.getName().equalsIgnoreCase("pin")){
							pin = cookie.getValue();
							request.setAttribute("pin", pin);
							chain.doFilter(request, response);
						}
					}
				}*/
				pin = c.getValue();
				request.setAttribute("pin", pin);
				chain.doFilter(request, response);
			}
		}
		
	}

	@Override
	public void init(FilterConfig init) throws ServletException {
		String skipVerifys = init.getInitParameter(SKIPVERIFY);
		if(!skipVerifys.trim().isEmpty()){
			if(skipVerifys.indexOf(",") != -1){
				for (String path : skipVerifys.split(",")) {
					skipVerifyList.add(path.trim());
				}
			}else{
				skipVerifyList.add(skipVerifys);
			}
		}
	}
	


}
