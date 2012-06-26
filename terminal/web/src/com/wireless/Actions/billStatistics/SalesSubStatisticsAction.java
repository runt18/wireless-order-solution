package com.wireless.Actions.billStatistics;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class SalesSubStatisticsAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		List l = new ArrayList();
		
		try{
			System.out.println("SalesSubStatisticsAction");
		} catch(Exception e){
			
		} finally{
			JSONArray json = JSONArray.fromObject(l);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
	
}
