package com.wireless.Actions.menuMgr.pricePlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryFoodPricePlanByOrderAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		JObject jobject = new JObject();
		List<PricePlan> root = null;
		try{
			
			//String pin = (String)request.getAttribute("pin");
			//StaffDao.verify(Integer.parseInt(pin));
			
			Map<Object, Object> params = new HashMap<Object, Object>();
			String restaurantID = (String)request.getAttribute("restaurantID");
//			String idList = request.getParameter("idList");
			String extra = "", orderBy = null;
//			List<FoodPricePlan> foodPricePlan = null;
			
			extra += (" AND A.restaurant_id = " + restaurantID);
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			root = MenuDao.getPricePlan(params);
			
			extra = "";
			params.remove(SQLUtil.SQL_PARAMS_EXTRA);
			params.remove(SQLUtil.SQL_PARAMS_ORDERBY);
			
//			extra += (" AND A.restaurant_id = " + restaurantID);
//			if(idList != null && !idList.trim().isEmpty()){
//				extra += (" AND A.food_id in (" + idList + ")");				
//			}
//			params.put(WebParams.SQL_PARAMS_EXTRA, extra);
//			params.put(WebParams.SQL_PARAMS_ORDERBY, orderBy);
//			foodPricePlan = MenuDao.getFoodPricePlan(params);
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
