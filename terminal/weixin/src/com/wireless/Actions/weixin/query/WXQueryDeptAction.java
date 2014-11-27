package com.wireless.Actions.weixin.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class WXQueryDeptAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String fid = request.getParameter("fid");
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			
			Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			List<Department> depts = DepartmentDao.getByType(dbCon, staff, Department.Type.NORMAL);
			List<Kitchen> kitchens = KitchenDao.getByType(dbCon, staff, Kitchen.Type.NORMAL);
			
			List<Map<String, Object>> deptList = new ArrayList<Map<String,Object>>();
			List<Kitchen> tempKitchenList = null;
			for(Department td : depts){
				tempKitchenList = new ArrayList<Kitchen>();
				for(Kitchen tk : kitchens){
					if(tk.getDept().getId() == td.getId()){
						tempKitchenList.add(tk);
					}
				}
				if(tempKitchenList != null && !tempKitchenList.isEmpty()){
					LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(td.toJsonMap(0));
					map.put("kitchen", tempKitchenList);
					deptList.add(map);					
				}
			}
			depts = null;
			kitchens = null;
			tempKitchenList = null;
//			jobject.getExtra().put("dept", deptList);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward kitchen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String fid = request.getParameter("fid");
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			
			Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			
			List<Kitchen> list = new ArrayList<>(); 
			
			String extraCond = " AND FOOD.restaurant_id = " + rid, orderClause = " ORDER BY FOOD.food_alias";
			extraCond += " AND (FOOD.status & " + Food.SELL_OUT + ") = 0";
			extraCond += " AND (FOOD.status & " + Food.RECOMMEND + ") <> 0";
			extraCond += " AND (FOOD.oss_image_id <> 0) ";
			List<Food> foods = FoodDao.getPureByCond(extraCond, orderClause);
			if(!foods.isEmpty()){
				Kitchen star = new Kitchen(-10);
				star.setName("明星菜");
				list.add(star);
			}
			
			list.addAll(KitchenDao.getByCond(staff, new KitchenDao.ExtraCond().setContainsImage(true), null));
			
			jobject.setRoot(list);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
		
}
