package com.wireless.Actions.dishesOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodTasteDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.menuMgr.Kitchen.Type;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class QueryMenuAction extends DispatchAction {
	/**
	 * 使用DepartmentTree得到foodList
	 */
	public ActionForward foodList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final DepartmentTree deptTree = new DepartmentTree.Builder(FoodDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), null, null)).build();
		jObject.setRoot(deptTree.asDeptNodes());
		
		response.getWriter().print(jObject.toString());
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
			FoodDao.ExtraCond cond = new FoodDao.ExtraCond();
			String orderBy = null;
			
			orderBy = " ORDER BY FOOD.food_alias";
			String kitchenAlias = request.getParameter("kitchenAlias");
			String foodName = request.getParameter("foodName");
			String foodAlias = request.getParameter("foodAlias");
			
			
			if(kitchenAlias != null && !kitchenAlias.trim().isEmpty() && !kitchenAlias.equals("-1")){
				cond.setKitchen(Integer.parseInt(kitchenAlias));
			}
			if(foodName != null && !foodName.trim().isEmpty()){
				cond.setName(foodName);
			}
			if(foodAlias != null && !foodAlias.trim().isEmpty()){
				cond.setAlias(Integer.parseInt(foodAlias));
			}
			root = new FoodList(FoodDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), cond, orderBy), Food.BY_ALIAS);

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(root != null){
				List<Food> result;
				if(pinyin != null && !pinyin.trim().isEmpty()){
					result = new ArrayList<Food>();
					for (Food f : root) {
						if(f.getPinyinShortcut().contains(pinyin.toLowerCase())){
							result.add(f);
						}
					}
					root = new FoodList(result, Food.BY_ALIAS);
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
			String restaurantId = (String)request.getAttribute("restaurantID");
			root = FoodTasteDao.getFoodTaste(Integer.parseInt(restaurantId));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			jobject.initTip4Exception(e);
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
			String restaurantId = (String)request.getAttribute("restaurantID");
			root = DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId)), Department.Type.NORMAL);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			jobject.initTip4Exception(e);
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
			
			root = FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().setSellout(true), null);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
	public ActionForward stopAndLimit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<Food> root = new ArrayList<Food>();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			root.addAll(FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().setSellout(true), null));
			root.addAll(FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().setLimit(true), null));
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			
			root = FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().setSellout(false), null);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
	public ActionForward getFoodPrices(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		List<Jsonable> root = new ArrayList<Jsonable>();
		try{
			String pin = (String)request.getAttribute("pin");
			String foodId = request.getParameter("foodId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			FoodDao.ExtraCond4Price extraCond = new FoodDao.ExtraCond4Price(new Food(Integer.parseInt(foodId)));
			
			final Map<PricePlan, Float> map = FoodDao.getPricePlan(staff, extraCond);
			
			Set<PricePlan> set = map.keySet();

			for (final PricePlan key : set) {

				Jsonable j = new Jsonable() {
					
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable(key, 0);
						jm.putFloat("price", map.get(key) != null?  map.get(key) : -1);
						return jm;
					}
					
					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				};
				root.add(j);

			}
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			jobject.setRoot(root);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}		
	
	
	public ActionForward getMultiPrices(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String foodId = request.getParameter("foodId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Food food = FoodDao.getById(staff, Integer.parseInt(foodId));
			
			List<FoodUnit> units = food.getFoodUnits();

			jobject.setRoot(units);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}		
	
	
}
