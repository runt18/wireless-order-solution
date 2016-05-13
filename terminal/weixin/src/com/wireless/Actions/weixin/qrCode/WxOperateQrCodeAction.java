package com.wireless.Actions.weixin.qrCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.QRCode;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxOperateQrCodeAction extends DispatchAction{
	
	/**
	 * 跨域生成qrCode
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward qrCode(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String limitStr = request.getParameter("limitStr");
		final String limit = request.getParameter("limit");
		final String temp = request.getParameter("temp");
		final String restaurantId = request.getParameter("restaurantId");
		final String fid = request.getParameter("fid");
		final String callbackFunName = request.getParameter("callback");
		final JObject jObject = new JObject();
		try {
			final Staff staff;
			if(restaurantId != null && !restaurantId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
			}else{
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(fid));
			}
			
			final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
			final AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
			final Token token = Token.newInstance(authorizerToken);
//			final Token token = Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET);
			final QRCode qrCode = new QRCode();
			
			if(limitStr != null && !limitStr.isEmpty()){
				qrCode.setLimitStr(limitStr);
			}else if(limit != null && !limit.isEmpty()){
				qrCode.setLimit(limit);
			}else if(temp != null && !temp.isEmpty()){
				qrCode.setTemp(temp);
			}
			
			final String qrCodeUrl = qrCode.createUrl(token);
			
			jObject.setRoot(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("qrCode", qrCodeUrl);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					// TODO Auto-generated method stub
				}
			});
			
			response.setContentType("text/plain");
			response.getWriter().print(callbackFunName + "(" + jObject.toString() + ")");
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		}

		return null;
	}
}
