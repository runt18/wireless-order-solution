package com.wireless.Actions.restaurantMgr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.util.DataPaging;

public class QueryRestaurantAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isPaging = request.getParameter("isPaging");
		String name = request.getParameter("name");
		String account = request.getParameter("account");
		String expireDate = request.getParameter("expireDate");
		String alive = request.getParameter("alive");
		String byId = request.getParameter("byId");
		List<Restaurant> list = new ArrayList<Restaurant>();
		Restaurant restaurant= null;
		JObject jobject = new JObject();
		String extraCond = "", orderClause = "";
		try{
			if(name != null && !name.trim().isEmpty()){
				extraCond += (" AND restaurant_name like '%" + name + "%' ");
			}else if(account != null && !account.trim().isEmpty()){
				extraCond = " AND account = '" + account + "' " ;
			}else if(expireDate != null || alive != null){
				if(expireDate != null && alive == null){
					orderClause += (" ORDER BY expire_date" );
				}else if(expireDate == null && alive != null){
					orderClause += (" ORDER BY liveness" );
				}else if(expireDate != null && alive != null){
					orderClause += (" ORDER BY expire_date, liveness" );
				}
			}
			
			if(Boolean.parseBoolean(byId)){
				System.out.println(Integer.parseInt((String) request.getAttribute("restaurantID")));
				restaurant = RestaurantDao.getById(Integer.parseInt((String) request.getAttribute("restaurantID")));
				list.add(restaurant);
			}else{
				list = RestaurantDao.getByCond(extraCond, orderClause);
				if(!list.isEmpty()){
					jobject.setTotalProperty(list.size());
					list = DataPaging.getPagingData(list, isPaging, start, limit);
				}
			}
			jobject.setRoot(list);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
}
