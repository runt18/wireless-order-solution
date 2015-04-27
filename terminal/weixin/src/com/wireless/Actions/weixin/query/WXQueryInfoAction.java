package com.wireless.Actions.weixin.query;

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
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WXQueryInfoAction extends DispatchAction{
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward restInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String rid = request.getParameter("rid");
		try{
			Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxRestaurant rest = WxRestaurantDao.get(staff);
			jobject.setRoot(rest);
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
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward initData(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		JObject jobject = new JObject();
		try{
			int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(request.getParameter("fid"));
			WxRestaurant wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(restaurantId));
			final String logo;
			if(wxRestaurant.hasWeixinLogo()){
				logo = wxRestaurant.getWeixinLogo().getObjectUrl();
			}else{
				logo = "http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxLogo/default.jpg";					
			}
			
			final Restaurant rInfo = RestaurantDao.getById(restaurantId);
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("logo", logo);
					jm.putJsonable("rInfo", rInfo, flag);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
}
