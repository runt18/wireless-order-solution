package com.wireless.Actions.menuMgr.basic;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.FoodList;

public class TestFoodList extends Action {
		public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType("text/json;charset=utf-8");
			JObject jobject = new JObject();
			String pin = (String) request.getAttribute("pin");
			final DepartmentTree deptTree = new DepartmentTree.Builder(FoodDao.getPureFoods(StaffDao.verify(Integer.parseInt(pin)))).build();
			jobject.setRoot(deptTree.asDeptNodes());
			jobject.setOther(new HashMap<Object, Object>(){
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			{put("foodList", new FoodList(deptTree.asFoodList()));}});
			response.getWriter().print(jobject.toString());
			return null;
		}
}
