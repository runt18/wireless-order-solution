package com.wireless.Actions.weixin.operate;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class WxOperateRestaurantAction extends DispatchAction {
	
	/**
	 * 获取微信餐厅的简介
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward intro(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		try{
			final int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(request.getParameter("fid"));
			final String intro = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(restaurantId)).getWeixinInfo();
			jObject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("intro", intro);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			jObject.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 获取微信餐厅的餐厅信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward detail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String fid = request.getParameter("fid");
		final JObject jObject = new JObject();
		try{
			final int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			jObject.setRoot(RestaurantDao.getById(restaurantId));
			
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
