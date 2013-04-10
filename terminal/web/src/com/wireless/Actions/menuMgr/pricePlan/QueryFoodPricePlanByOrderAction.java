package com.wireless.Actions.menuMgr.pricePlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodPricePlan;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryFoodPricePlanByOrderAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		JSONObject content = null;
		List<LinkedHashMap<String, Object>> root = new ArrayList<LinkedHashMap<String, Object>>();
		try{
			Map<Object, Object> params = new HashMap<Object, Object>();
			String restaurantID = request.getParameter("restaurantID");
//			String idList = request.getParameter("idList");
			String extra = "", orderBy = null;
			List<PricePlan> pricePlan = null;
//			List<FoodPricePlan> foodPricePlan = null;
			
			extra += (" AND A.restaurant_id = " + restaurantID);
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			pricePlan = MenuDao.getPricePlan(params);
			
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
			
			for(PricePlan temp : pricePlan){
				LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
				item.put("id", temp.getId());
				item.put("name", temp.getName());
				item.put("status", temp.getStatus());
				item.put("items", new ArrayList<FoodPricePlan>());
				root.add(item);
			}
//			for(FoodPricePlan fpp : foodPricePlan){
//				for(LinkedHashMap<String, Object> pp : root){
//					if(Integer.valueOf(pp.get("id").toString()) == fpp.getPlanID()){
//						fpp.setPricePlan(null);
//						((List<FoodPricePlan>)pp.get("items")).add(fpp);
//					}
//				}
//			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setRoot(root);
			content = JSONObject.fromObject(jobject);
			response.getWriter().print(content.toString());
		}
		return null;
	}

}
