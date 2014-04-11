package com.wireless.Actions.restaurantMgr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.ModuleDao;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Module;

public class QueryModuleAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		List<Module> modulelist = new ArrayList<Module>();
		try{
			modulelist = ModuleDao.getAll();
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setRoot(modulelist);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
