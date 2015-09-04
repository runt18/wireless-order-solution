package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxQueryOrderAction extends DispatchAction {
	
	public ActionForward getByMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String oid = request.getParameter("oid");
		String fid = request.getParameter("fid");
		String orderType = request.getParameter("type");
		
		JObject jobject = new JObject();
		
		try {
			
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			WxOrder.Type ordersType = WxOrder.Type.INSIDE;
			if(orderType != null && !orderType.isEmpty()){
				ordersType = WxOrder.Type.valueOf(Integer.parseInt(orderType));
			}
			
			List<WxOrder> orders = WxOrderDao.getByCond(staff, new WxOrderDao.ExtraCond().setWeixin(oid).setType(ordersType), " ORDER BY birth_date DESC");
			
			for (WxOrder wxOrder : orders) {
				if(wxOrder.getStatus() == WxOrder.Status.COMMITTED || wxOrder.getStatus() == WxOrder.Status.ORDER_ATTACHED){
					wxOrder.addFoods(WxOrderDao.getById(staff, wxOrder.getId()).getFoods());
				}
				if(ordersType == WxOrder.Type.TAKE_OUT){
					wxOrder.setTakoutAddress(TakeoutAddressDao.getById(staff, wxOrder.getTakeoutAddress().getId()));
				}
			}
			
			jobject.setRoot(orders);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
