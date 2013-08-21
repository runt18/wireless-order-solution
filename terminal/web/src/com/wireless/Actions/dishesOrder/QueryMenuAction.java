package com.wireless.Actions.dishesOrder;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.json.JObject;
import com.wireless.json.Jsonable;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

/**
 * The parameters looks like below.
 * e.g. pin=0x1 & type=1 
 * pin : the pin the this terminal
 * type : "1" means to query foods 
 * 		  "2" means to query tastes
 * 		  "3" means to query kitchens
 * 		  "4" means to query depts
 */

public class QueryMenuAction extends DispatchAction {
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward foods(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try {
			
			String restaurantID = request.getParameter("restaurantID");
			String cond = null;
			String orderBy = null;
			
			try{
				Integer.parseInt(restaurantID);
			}catch(NumberFormatException e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号失败.");
				return null;
			}
			
			orderBy = " ORDER BY A.food_alias";
			cond = " AND A.restaurant_id = " + restaurantID;
			String kitchenAlias = request.getParameter("kitchenAlias");
			String foodName = request.getParameter("foodName");
			String pinyin = request.getParameter("pinyin");
			String foodAlias = request.getParameter("foodAlias");
			if(kitchenAlias != null && !kitchenAlias.trim().isEmpty() && !kitchenAlias.equals("-1")){
				cond += (" AND A.kitchen_alias = " + kitchenAlias);
			}
			if(foodName != null && !foodName.trim().isEmpty()){
				cond += (" AND A.name like '%" + foodName.trim() + "%'");
			}
			if(pinyin != null && !pinyin.trim().isEmpty()){
				cond += (" AND A.pinyin like '%" + pinyin.trim() + "%'");
			}
			if(foodAlias != null && !foodAlias.trim().isEmpty()){
				cond += (" AND A.food_alias like '" + foodAlias.trim() + "%'");
			}
			root = MenuDao.getFood(cond, orderBy);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tastes(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			try{
				Integer.parseInt(restaurantID);
			}catch(NumberFormatException e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号失败.");
				return null;
			}
			root = MenuDao.getFoodTaste(Integer.parseInt(restaurantID));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward kitchens(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String cond = "";
			try{
				Integer.parseInt(restaurantID);
			}catch(NumberFormatException e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号失败.");
				return null;
			}
			cond = (" AND A.restaurant_id = " + restaurantID);
			cond += (" AND A.kitchen_alias <> 253 AND A.kitchen_alias <> 255 ");
			String isAllowTemp = request.getParameter("isAllowTemp");
			if(isAllowTemp != null && !isAllowTemp.trim().isEmpty()){
				cond += (" AND A.is_allow_temp = " + isAllowTemp);
			}
			root = MenuDao.getKitchen(cond, null);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward depts(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			try{
				Integer.parseInt(restaurantID);
			}catch(NumberFormatException e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号失败.");
				return null;
			}
			root = MenuDao.getDepartment(Integer.parseInt(restaurantID));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
