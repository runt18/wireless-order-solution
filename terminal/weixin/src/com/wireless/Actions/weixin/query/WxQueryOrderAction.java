package com.wireless.Actions.weixin.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxQueryOrderAction extends DispatchAction {
	
	/**
	 * 获取相应微信会员的自助店内订单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String oid = request.getParameter("oid");
		final String fid = request.getParameter("fid");
		final String orderType = request.getParameter("type");
		
		final JObject jObject = new JObject();
		
		try {
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final WxOrder.Type ordersType;
			if(orderType != null && !orderType.isEmpty()){
				ordersType = WxOrder.Type.valueOf(Integer.parseInt(orderType));
			}else{
				ordersType = WxOrder.Type.INSIDE;
			}

			final List<WxOrder> result = new ArrayList<WxOrder>();
			//集团下需要显示所有门店的订单
			if(staff.isGroup()){
				for(Restaurant branches : RestaurantDao.getById(staff.getRestaurantId()).getBranches()){
					result.addAll(WxOrderDao.getByCond(StaffDao.getAdminByRestaurant(branches.getId()), new WxOrderDao.ExtraCond().setWeixin(oid).setType(ordersType), null));
				}
			}
			
			result.addAll(WxOrderDao.getByCond(staff, new WxOrderDao.ExtraCond().setWeixin(oid).setType(ordersType), null));
			
			//按下单日期降序显示
			Collections.sort(result, new Comparator<WxOrder>(){
				@Override
				public int compare(WxOrder o1, WxOrder o2) {
					if(o1.getBirthDate() > o2.getBirthDate()){
						return -1;
					}else if(o1.getBirthDate() < o2.getBirthDate()){
						return 1;
					}else{
						return 0;
					}
				}
			});
			
			for (WxOrder wxOrder : result) {
				if(wxOrder.getStatus() == WxOrder.Status.COMMITTED || wxOrder.getStatus() == WxOrder.Status.ORDER_ATTACHED){
					wxOrder.addFoods(WxOrderDao.getById(staff, wxOrder.getId()).getFoods());
				}
				if(ordersType == WxOrder.Type.TAKE_OUT){
					wxOrder.setTakoutAddress(TakeoutAddressDao.getById(staff, wxOrder.getTakeoutAddress().getId()));
				}
			}
			
			jObject.setRoot(result);
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

}
