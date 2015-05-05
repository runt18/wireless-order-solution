package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.DataPaging;

public class WXQueryFoodAction extends DispatchAction{
	
	/**
	 * 明星菜(推荐属性菜品)
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward isRecommend(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String fid = request.getParameter("fid");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			List<Food> list = FoodDao.getPureByCond(StaffDao.getAdminByRestaurant(rid), new FoodDao.ExtraCond().setSellout(false).setRecomment(true).setContainsImage(true), " ORDER BY FOOD.food_alias");
			if(list != null){
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, true, 0, 20);
				jobject.setRoot(list);
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 普通查询
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		List<Food> root = null;
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String fid = request.getParameter("fid");
			String kitchenId = request.getParameter("kitchenId");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			
			root = FoodDao.getPureByCond(dbCon, StaffDao.getAdminByRestaurant(dbCon, rid),
										new FoodDao.ExtraCond().setSellout(false).setContainsImage(true).setKitchen(Integer.parseInt(kitchenId)), 
										" ORDER BY FOOD.food_alias ");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			if(root != null && !root.isEmpty()){
				jobject.setTotalProperty(root.size());
				root = DataPaging.getPagingData(root, isPaging, start, limit);
			}
			jobject.setRoot(root);
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}
