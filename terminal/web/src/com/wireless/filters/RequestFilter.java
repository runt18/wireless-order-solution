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

	private static final String SKIP_VERIFY_LIST = "skipVerify";
	private final Map<String, String> skipVerifyList = new HashMap<String, String>();
	
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
		//Check to see whether the request is contained in while list.
		if(skipVerifyList.get(requestPath.substring(requestPath.lastIndexOf("/"))) != null){
			//If the request is contained in white list, do chain directly.
			String pin =  (String) request.getSession().getAttribute("pin");
			String restaurantId = (String) request.getSession().getAttribute("restaurantID");
			if(pin != null){
				request.setAttribute("pin", pin);
			}
			if(restaurantId != null){
				request.setAttribute("restaurantID", restaurantId);
			}
			chain.doFilter(request, response);
			
		}else{
			//If the request is NOT contained in white list, check to see whether the session is expired.
			String pin = (String) request.getSession().getAttribute("pin");
			if(pin == null){
				//If the session is expired, tell the request caller about this.
				JObject jObject = new JObject();
				jObject.initTip(new BusinessException(SystemError.REQUEST_EXPIRED_OR_NOT_IN_WHITE_LIST));
				servletResponse.getWriter().print(jObject.toString());
				if (request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {  
                    response.setHeader("session_status", "timeout");
                    response.addHeader("root_path",	request.getContextPath());
                }

			}else{
				//if the session is OK, just do chain...go!!!
				request.setAttribute("pin", pin);
				request.setAttribute("restaurantID", (String) request.getSession().getAttribute("restaurantID"));
				chain.doFilter(request, response);
			}
				
		}
		String ct = request.getContentType();
		if(ct != null && ct.split(";")[0].equalsIgnoreCase("multipart/form-data")){
			response.setContentType("text/plain; charset=utf-8");
		}else{
			response.setContentType("text/json; charset=utf-8");
		}
		
	}

	@Override
	public void init(FilterConfig init) throws ServletException {
		//Read the skip verify list and put them to hash map.
		String skipVerifys = init.getInitParameter(SKIP_VERIFY_LIST);
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
