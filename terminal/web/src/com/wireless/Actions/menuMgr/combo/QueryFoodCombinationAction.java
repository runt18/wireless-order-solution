package com.wireless.Actions.menuMgr.combo;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodCombinationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.FoodCombo;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.WebParams;

public class QueryFoodCombinationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		
		List<FoodCombo> root = null;
		JObject jobject = new JObject();
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin), Privilege.Code.BASIC);
			
			String foodID = request.getParameter("foodID");
			String restaurantID = request.getParameter("restaurantID");
			String extraCondition = "and B.food_id = " + foodID + " and B.restaurant_id = " + restaurantID;
			root = FoodCombinationDao.getFoodCombination(extraCondition);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setRoot(root);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
