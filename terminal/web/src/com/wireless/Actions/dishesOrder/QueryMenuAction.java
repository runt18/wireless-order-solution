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

		JObject jobject = new JObject();
		List root = new ArrayList();
		
		String restaurantID = request.getParameter("restaurantID");
		String type = request.getParameter("type");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String cond = null;
		String orderBy = null;
		
		try {
			
			response.setContentType("text/json; charset=utf-8");
			
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
			}catch(Exception e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号或操作类型失败.");
				return null;
			}
			
			if(type.trim().equals("1")){
				orderBy = " order by A.food_alias";
				cond = " and A.restaurant_id = " + restaurantID;
				String searchType = request.getParameter("searchType");
				String searchValue = request.getParameter("searchValue");
				if(searchType != null && searchValue != null){
					if(searchType.equals("0")){
						if(searchValue.equals("254")){
							cond += "";
						}else{
							cond += " and A.kitchen_alias = " + searchValue;
						}
					}else if(searchType.equals("1")){
						cond += " and A.name like '%" + searchValue.trim() + "%'";
					}else if(searchType.equals("2")){
						cond += " and A.pinyin like '%" + searchValue.trim() + "%'";
					}else if(searchType.equals("3")){
						cond += " and A.food_alias like '" + searchValue.trim() + "%'";
					}
				}
				root = MenuDao.getFood(cond, orderBy);
			}else if(type.trim().equals("2")){
				root = MenuDao.getFoodTaste(Integer.parseInt(restaurantID));
			}else if(type.trim().equals("3")){
				root = MenuDao.getKitchen(Integer.parseInt(restaurantID));
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
