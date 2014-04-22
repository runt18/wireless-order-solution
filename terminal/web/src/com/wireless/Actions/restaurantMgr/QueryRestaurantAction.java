package com.wireless.Actions.restaurantMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.SMStat;
import com.wireless.util.DataPaging;

public class QueryRestaurantAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		
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
		String extraCond = "", orderClause = " ORDER BY id";
		List<Jsonable> resList = new ArrayList<Jsonable>();
		DBCon dbCon = new DBCon();
		try{
			if(name != null && !name.trim().isEmpty()){
				extraCond += (" AND (restaurant_name like '%" + name + "%' OR account like '%" + name + "%') ");
			}else if(account != null && !account.trim().isEmpty()){
				extraCond = " AND account = '" + account + "' " ;
			}else if((expireDate != null && !expireDate.isEmpty()) || (alive != null && !alive.isEmpty())){
				if(alive == null || alive.isEmpty()){
					orderClause = (" ORDER BY expire_date" );
				}else if(expireDate == null || expireDate.isEmpty()){
					orderClause = (" ORDER BY liveness" );
				}else if(expireDate != null && alive != null){
					orderClause = (" ORDER BY expire_date, liveness" );
				}
			}
			dbCon.connect();
			if(Boolean.parseBoolean(byId)){
				restaurant = RestaurantDao.getById(dbCon, Integer.parseInt((String) request.getAttribute("restaurantID")));
				list.add(restaurant);
			}else{
				list = RestaurantDao.getByCond(extraCond, orderClause);
				if(!list.isEmpty()){
					for (final Restaurant smsr : list) {
						//为restaurant加上短信条数
						final SMStat sms = SMStatDao.get(dbCon, StaffDao.getAdminByRestaurant(dbCon, smsr.getId()));
						Jsonable j = new Jsonable() {
							
							@Override
							public Map<String, Object> toJsonMap(int flag) {
								Map<String, Object> jm = new HashMap<String, Object>();
								
								jm.putAll(smsr.toJsonMap(0));
								jm.put("smsRemain", sms.getRemaining());
								return Collections.unmodifiableMap(jm);
							}
							
							@Override
							public List<Object> toJsonList(int flag) {
								return null;
							}
						};
						resList.add(j);
					}
					jobject.setTotalProperty(list.size());
					resList = DataPaging.getPagingData(resList, isPaging, start, limit);
				}
			}
			jobject.setRoot(resList);
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
}
