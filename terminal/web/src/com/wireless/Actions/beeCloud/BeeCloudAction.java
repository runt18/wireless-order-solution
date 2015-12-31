package com.wireless.Actions.beeCloud;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.beeCloud.BeeCloud;
import com.wireless.beeCloud.Bill;
import com.wireless.beeCloud.Revert;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;

public class BeeCloudAction extends DispatchAction {
	
	/**
	 * 取消BeeCloud的支付
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String billNo = request.getParameter("billNo");
		final String channel = request.getParameter("channel");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Restaurant restaurant = RestaurantDao.getById(staff.getRestaurantId());
			if(restaurant.hasBeeCloud()){
				BeeCloud app = BeeCloud.registerApp(restaurant.getBeeCloudAppId(), restaurant.getBeeCloudAppSecret());
				final Revert.Response beeCloudResponse;
				if(channel.trim().equalsIgnoreCase("wx_scan")){
					beeCloudResponse = app.revert().ask(billNo, Bill.Channel.WX_SCAN);
				}else if(channel.trim().equalsIgnoreCase("wx_native")){
					beeCloudResponse = app.revert().ask(billNo, Bill.Channel.WX_NATIVE);
				}else{
					throw new BusinessException("支付方式必须输入");
				}
				
				if(beeCloudResponse.isOk()){
					jObject.initTip(true, "支付取消成功");
				}else{
					throw new BusinessException(beeCloudResponse.getResultMsg() + "," + beeCloudResponse.getErrDetail());
				}
			}else{
				throw new BusinessException("您还没开通在线支付功能");
			}
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
