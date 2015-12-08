package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.DataPaging;

public class WxQueryFoodAction extends DispatchAction{
	
	/**
	 * 明星菜(推荐属性菜品)
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward isRecommend(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		try{
			final String fid = request.getParameter("fid");
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			List<Food> list = FoodDao.getPureByCond(StaffDao.getAdminByRestaurant(rid), new FoodDao.ExtraCond().setSellout(false).setRecomment(true).setContainsImage(true), " ORDER BY FOOD.food_alias");
			if(list != null){
				jObject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, true, 0, 20);
				jObject.setRoot(list);
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward normal(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		List<Food> root = null;
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			
			String fid = request.getParameter("fid");
			String kitchenId = request.getParameter("kitchenId");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			root = FoodDao.getByCond(StaffDao.getAdminByRestaurant(rid),
										new FoodDao.ExtraCond().setSellout(false).setContainsImage(true).setKitchen(Integer.parseInt(kitchenId)), 
										" ORDER BY FOOD.food_alias ");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(root != null && !root.isEmpty()){
				jObject.setTotalProperty(root.size());
				root = DataPaging.getPagingData(root, isPaging, start, limit);
			}
			jObject.setRoot(root);
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
}
