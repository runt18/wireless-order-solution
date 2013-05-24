package com.wireless.Actions.dishesOrder;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryMenuAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		List root = new ArrayList();
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try {
			String restaurantID = request.getParameter("restaurantID");
			String type = request.getParameter("type");
			String cond = null;
			String orderBy = null;
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & type=1 
			 * pin : the pin the this terminal
			 * type : "1" means to query foods 
			 * 		  "2" means to query tastes
			 * 		  "3" means to query kitchens
			 */
			try{
				Integer.parseInt(restaurantID);
				Integer.parseInt(type);
			}catch(NumberFormatException e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号或操作类型失败.");
				return null;
			}
			
			if(type.trim().equals("1")){
				orderBy = " ORDER BY A.food_alias";
				cond = " AND A.restaurant_id = " + restaurantID;
				String searchType = request.getParameter("searchType");
				String searchValue = request.getParameter("searchValue");
				if(searchType != null && searchValue != null){
					if(searchType.equals("0")){
						if(searchValue.equals("254")){
							cond += "";
						}else{
							cond += " AND A.kitchen_alias = " + searchValue;
						}
					}else if(searchType.equals("1")){
						cond += " AND A.name like '%" + searchValue.trim() + "%'";
					}else if(searchType.equals("2")){
						cond += " AND A.pinyin like '%" + searchValue.trim() + "%'";
					}else if(searchType.equals("3")){
						cond += " AND A.food_alias like '" + searchValue.trim() + "%'";
					}
				}
				root = MenuDao.getFood(cond, orderBy);
			}else if(type.trim().equals("2")){
				root = MenuDao.getFoodTaste(Integer.parseInt(restaurantID));
			}else if(type.trim().equals("3")){
				cond = (" AND A.restaurant_id = " + restaurantID);
				cond += (" AND A.kitchen_alias <> 253 AND A.kitchen_alias <> 255 ");
				String isAllowTemp = request.getParameter("isAllowTemp");
				if(isAllowTemp != null && !isAllowTemp.trim().isEmpty()){
					cond += (" AND A.is_allow_temp = " + isAllowTemp);
				}
				root = MenuDao.getKitchen(cond, null);
			}else if(type.trim().equals("4")){
				root = MenuDao.getDepartment(Integer.parseInt(restaurantID));
			}
						
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		}finally{
			if(isPaging != null && isPaging.trim().equals("true") && start != null && limit != null){
				int pageSize = Integer.parseInt(limit);
				int index = Integer.parseInt(start);
				List tempRoot = new ArrayList();
				pageSize = (index + pageSize) > root.size() ? (pageSize - ((index + pageSize) - root.size())) : pageSize;
				for(int i = 0; i < pageSize; i++){
					tempRoot.add(root.get(index + i));
				}
				jobject.setTotalProperty(root.size());
				jobject.setRoot(tempRoot);
			}else{
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}
