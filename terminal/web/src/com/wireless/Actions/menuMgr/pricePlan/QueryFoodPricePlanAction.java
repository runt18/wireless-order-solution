package com.wireless.Actions.menuMgr.pricePlan;

import java.util.HashMap;
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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodPricePlan;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryFoodPricePlanAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		JObject jobject = new JObject();
		JSONObject content = null;
		List<FoodPricePlan> list = null;
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String extra = "", orderBy = null;
			orderBy = " ORDER BY B.food_alias ";
			String restaurantID = request.getParameter("restaurantID");
			String searchType = request.getParameter("searchType");
			String searchOperator = request.getParameter("searchOperator");
			String searchValue = request.getParameter("searchValue");
			String searchPrciePlan = request.getParameter("searchPrciePlan");
			String ope =  "= ";
			
			extra += (" AND A.restaurant_id = " + restaurantID);
			if(searchPrciePlan != null && !searchPrciePlan.trim().isEmpty()){
				extra += (" AND A.price_plan_id = " + searchPrciePlan);
			}
			
			if(searchType != null && !searchType.trim().isEmpty() && !searchType.trim().equals("0") 
					&& searchValue != null && !searchValue.trim().isEmpty()){
				searchType = searchType.trim();
				searchValue = searchValue.trim();
				if(searchType.equals("1") || searchType.equals("2")){
					if(searchOperator != null && !searchOperator.trim().isEmpty()){
						searchOperator = searchOperator.trim();
						if(searchOperator.equals("1")){
							ope = " = ";
						}else if(searchOperator.equals("2")){
							ope = " >= ";
						}else if(searchOperator.equals("3")){
							ope = " <= ";
						}else{
							ope = " = ";
						}
					}
				}
				if(searchType.equals("1")){
					extra += (" AND B.food_alias " + ope + searchValue);
				}else if(searchType.equals("2")){
					extra += (" AND A.unit_price " + ope + searchValue);
				}else if(searchType.equals("3")){
					extra += (" AND B.name like '%" + searchValue.trim() + "%'");
				}else if(searchType.equals("4")){
					if(!searchValue.equals("") || !searchValue.equals("-1"))
						extra += (" AND C.kitchen_id = " + searchValue);
				}else if(searchType.equals("5")){
					extra += (" AND B.food_id " + ope + searchValue);
				}
			}
			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			list = MenuDao.getFoodPricePlan(params);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				jobject.setRoot(list);
				list = null;
			}
			content = JSONObject.fromObject(jobject);
			response.getWriter().print(content.toString());
		}
		return null;
	}

}
