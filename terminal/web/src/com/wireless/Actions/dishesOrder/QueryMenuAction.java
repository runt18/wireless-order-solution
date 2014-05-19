package com.wireless.Actions.dishesOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.menuMgr.Kitchen.Type;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryMenuAction extends DispatchAction {
	/**
	 * 使用DepartmentTree得到foodList
	 */
	public ActionForward foodList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		final DepartmentTree deptTree = new DepartmentTree.Builder(FoodDao.getPureFoods(StaffDao.verify(Integer.parseInt(pin)))).build();
		jobject.setRoot(deptTree.asDeptNodes());
		jobject.setOther(new HashMap<Object, Object>(){
			private static final long serialVersionUID = 1L;

		{put("foodList", new FoodList(deptTree.asFoodList()));}});
		response.getWriter().print(jobject.toString());
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
	public ActionForward foods(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<Food> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String pinyin = request.getParameter("pinyin");
		try {
			String pin = (String)request.getAttribute("pin");
			String cond = "";
			String orderBy = null;
			
			orderBy = " ORDER BY FOOD.food_alias";
			String kitchenAlias = request.getParameter("kitchenAlias");
			String foodName = request.getParameter("foodName");
			String foodAlias = request.getParameter("foodAlias");
			
			
			if(kitchenAlias != null && !kitchenAlias.trim().isEmpty() && !kitchenAlias.equals("-1")){
				cond += (" AND FOOD.kitchen_id = " + kitchenAlias);
			}
			if(foodName != null && !foodName.trim().isEmpty()){
				cond += (" AND FOOD.name like '%" + foodName.trim() + "%'");
			}
			if(foodAlias != null && !foodAlias.trim().isEmpty()){
				cond += (" AND FOOD.food_alias like '" + foodAlias.trim() + "%'");
			}
			root = new FoodList(FoodDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), cond, orderBy));

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(root != null){
				List<Food> result = new ArrayList<Food>();
				if(pinyin != null && !pinyin.trim().isEmpty()){
					for (Food f : root) {
						if(f.getPinyinShortcut().contains(pinyin.toLowerCase())){
							result.add(f);
						}
					}
					root = result;
				}
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
		
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		response.setContentType("text/json;charset=utf-8");
		try{
			String restaurantID = (String)request.getAttribute("restaurantID");
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
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = (String)request.getAttribute("pin");
			root = KitchenDao.getByType(StaffDao.verify(Integer.parseInt(pin)), Type.NORMAL);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String restaurantID = (String)request.getAttribute("restaurantID");
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
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward isAllowTempKitchen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = new ArrayList<Jsonable>();
		try{
			String pin = (String)request.getAttribute("pin");
			root = KitchenDao.getByAllowTemp(StaffDao.verify(Integer.parseInt(pin)));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
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
	public ActionForward stop(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = new ArrayList<Jsonable>();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String extraCond = " AND (FOOD.status & " + Food.SELL_OUT + ") <> 0";
			root = FoodDao.getPureByCond(staff, extraCond, null);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
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
	public ActionForward unStop(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<? extends Jsonable> root = new ArrayList<Jsonable>();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String extraCond = " AND (FOOD.status & " + Food.SELL_OUT + ") = 0";
			root = FoodDao.getPureByCond(staff, extraCond, null);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
