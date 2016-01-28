package com.wireless.Actions.weixin.beeCloud;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.beeCloud.BeeCloud;
import com.wireless.beeCloud.Bill;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class WxBeeCloudAction extends DispatchAction {
	
	/**
	 * 开始微信支付（JSAPI）
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String fromId = request.getParameter("fid");
		final String totalFee = request.getParameter("totalFee");
		try{
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			Restaurant restaurant = RestaurantDao.getById(rid);
			if(restaurant.hasBeeCloud()){
				BeeCloud app = BeeCloud.registerApp(restaurant.getBeeCloudAppId(), restaurant.getBeeCloudAppSecret());
				Bill.Response beeCloudResponse = app.bill().ask(new Bill.Request().setBillNo(System.currentTimeMillis() + "").setTotalFee(Integer.parseInt(totalFee)), null);
				jObject.setRoot(beeCloudResponse);
			}else{
				throw new BusinessException("对不起，您的公众号还没开通微信支付");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(false, e.getMessage());
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
		
}
