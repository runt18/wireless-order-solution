package com.wireless.Actions.weixin.query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.BillBoardDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.BillBoard;

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
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(request.getParameter("fid"));
			String extra = " AND BB.restaurant_id = " + rid + " AND BB.type = " + BillBoard.Type.WX_INFO.getVal() + " ORDER BY created DESC LIMIT 0,1 ";
			jobject.setRoot(BillBoardDao.get(extra));
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
			int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(request.getParameter("fid"));
			OssImage oss = WeixinRestaurantDao.get(StaffDao.getAdminByRestaurant(restaurantId)).getWeixinLogo();
			String logo;
			if(oss == null){
				logo = getServlet().getInitParameter("imageBrowseDefaultFile");
			}else{
				logo = oss.getObjectUrl();
			}
			
			final Restaurant rInfo = RestaurantDao.getById(restaurantId);
			
			final String logoPath = logo;
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("logo", logoPath);
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
