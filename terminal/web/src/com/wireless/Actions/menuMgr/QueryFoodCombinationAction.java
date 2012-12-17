package com.wireless.Actions.menuMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodCombinationDao;
import com.wireless.pojo.menuMgr.FoodCombination;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryFoodCombinationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		String foodID = request.getParameter("foodID");
		String restaurantID = request.getParameter("restaurantID");
		List<FoodCombination> root = new ArrayList<FoodCombination>();
		FoodCombination[] list = new FoodCombination[0];
		JObject jobject = new JObject();
		
		try{
			String extraCondition = "and B.food_id = " + foodID + " and B.restaurant_id = " + restaurantID;
			list = FoodCombinationDao.getFoodCombination(extraCondition);
			root = Arrays.asList(list);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setRoot(root);
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
}
