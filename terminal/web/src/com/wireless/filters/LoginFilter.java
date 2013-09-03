package com.wireless.filters;

import java.io.IOException;
import java.util.HashMap;
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
public class LoginFilter implements Filter{

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest)servletRequest; 
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		//获取请求连接
		String path = request.getRequestURI();
		Map<String, String> params = new HashMap<String, String>();
		
		//FIXME 
		//The code below just for print scheme port, should be removed in future
		String url = path + "?" + request.getQueryString();
		if(url.startsWith("/WirelessOrderWeb/OperatePrinter.do?skipVerify&dataSource=port") ||
		   url.startsWith("/WirelessOrderWeb/OperatePrintFunc.do?skipVerify&dataSource=port")){
			chain.doFilter(request, response);
			return;
		}
		//-----------------------------------------------------------------------
			
		String skipVerify = null;
		String isCookie = null;
		
		if(request.getQueryString() != null && (path.indexOf(".do") < 0)){
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
			skipVerify = request.getParameter("skipVerify"); 
			isCookie = request.getParameter("isCookie");
		}
		
		
		//过滤不需要验证的网页
		if(path.indexOf("/PersonLogin.html") > -1){
			chain.doFilter(request, response);
			return;
		}
		if(path.indexOf("/PersonLoginTimeout.html") > -1){
			chain.doFilter(request, response);
			return;
		}

		
		//通过ajax参数判断action是否需要验证
		if(skipVerify == null){
			String pin = null;
			//是否用cookie
			if(isCookie == null){
				pin = (String)request.getSession().getAttribute("pin");
				if(pin == null){
					if (request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {  
	                    response.addHeader("sessionstatus", "timeout");  
	                }else{
	                	response.sendRedirect("/WirelessOrderWeb/pages/PersonLoginTimeout.html?" + Encrypt.strEncode("restaurantID="+params.get("restaurantID"), "mi", null, null));
	                }
					
				}else{
					request.setAttribute("pin", pin);
					chain.doFilter(request, response);
				}
				
			}else{
				Cookie[] cookies = request.getCookies();
				if(cookies != null){
					for (Cookie cookie : cookies) {
						if(cookie.getName().equalsIgnoreCase("pin")){
							pin = cookie.getValue();
							request.setAttribute("pin", pin);
							chain.doFilter(request, response);
						}
					}
				}
			}
			
		}else{
			chain.doFilter(request, response);
		}
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
