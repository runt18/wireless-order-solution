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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wireless.exception.BusinessException;
import com.wireless.exception.SystemError;
import com.wireless.json.JObject;

public class RequestFilter implements Filter{

	private static final String SKIPVERIFY = "skipVerify";
	private Map<String, String> skipVerifyList = new HashMap<String, String>();
	
	private boolean check(String path){
		for (String skip : skipVerifyList.values()) {
			if(path.indexOf(skip) > -1){
				return true;
			}
		}
		return false;
	}
	@Override
	public void destroy() {
		
	} 

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		String requestPath = request.getRequestURI();
		if(check(requestPath)){
			String pin =  (String) request.getSession().getAttribute("pin");
			String restaurantID =  (String) request.getSession().getAttribute("restaurantID");
			if(pin != null){
				request.setAttribute("pin", pin);
			}
			if(restaurantID != null){
				request.setAttribute("restaurantID", restaurantID);
			}
			chain.doFilter(request, response);
			
		}else{
			String pin = null;
			pin = (String) request.getSession().getAttribute("pin");
			if(pin == null){
				JObject jObject = new JObject();
				jObject.initTip(new BusinessException(SystemError.NOT_PASS_WHITELIST));
				servletResponse.getWriter().print(jObject.toString());
				if (request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {  
                    response.setHeader("session_status", "timeout");
                    response.addHeader("root_path",	request.getContextPath());
                }
//				else{
//                	if(comeFrom != null){
//                		response.sendRedirect(request.getContextPath() + Staff.RequestSource.valueOf(Integer.parseInt(comeFrom.getValue())).getRedirect()
//                							  + "?" + Encrypt.strEncode("restaurantID="+params.get("restaurantID"), KEYS, null, null));
//                	}
//                }

			}else{
				request.setAttribute("pin", pin);
				request.setAttribute("restaurantID", (String) request.getSession().getAttribute("restaurantID"));
				chain.doFilter(request, response);
			}
				
		}
//		if(!requestPath.contains("/ImageFileUpload.do")){
//			response.setContentType("text/json;charset=utf-8");
//		}
		String ct = request.getContentType();
		if(ct != null && ct.split(";")[0].equalsIgnoreCase("multipart/form-data")){
			response.setContentType("text/plain; charset=utf-8");
		}else{
			response.setContentType("text/json; charset=utf-8");
		}
		
	}

	@Override
	public void init(FilterConfig init) throws ServletException {
		String skipVerifys = init.getInitParameter(SKIPVERIFY);
		if(!skipVerifys.trim().isEmpty()){
			if(skipVerifys.indexOf(",") != -1){
				for (String path : skipVerifys.split(",")) {
					skipVerifyList.put(path.trim(), path.trim());
				}
			}else{
				skipVerifyList.put(skipVerifys, skipVerifys);
			}
		}
	}
	


}
