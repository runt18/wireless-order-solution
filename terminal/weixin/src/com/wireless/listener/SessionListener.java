package com.wireless.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener{

	public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
	
	@Override
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		sessions.put(httpSessionEvent.getSession().getId(), httpSessionEvent.getSession());
		//System.out.println("session created : " + httpSessionEvent.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		sessions.remove(httpSessionEvent.getSession().getId());
		//System.out.println("session destroyed : " + httpSessionEvent.getSession().getId());
	}

}
