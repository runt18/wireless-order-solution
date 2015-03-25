package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxQueryOrderAction extends DispatchAction {
	
	public ActionForward getByMember(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String oid = request.getParameter("oid");
		String fid = request.getParameter("fid");
		String orderType = request.getParameter("type");
		DBCon dbCon = null;
		JObject jobject = new JObject();
		
		try {
			dbCon = new DBCon();
			dbCon.connect();
			
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			List<WxOrder> orders = WxOrderDao.getByCond(dbCon, staff, new WxOrderDao.ExtraCond().setWeixin(oid).setType(WxOrder.Type.valueOf(Integer.parseInt(orderType))), " ORDER BY birth_date DESC");
			
			for (WxOrder wxOrder : orders) {
				if(wxOrder.getStatus() == WxOrder.Status.COMMITTED || wxOrder.getStatus() == WxOrder.Status.ORDER_ATTACHED){
					wxOrder.addFoods(WxOrderDao.getById(dbCon, staff, wxOrder.getId()).getFoods());
				}
				wxOrder.setTakoutAddress(TakeoutAddressDao.getById(staff, wxOrder.getTakeoutAddress().getId()));
			}
			
			jobject.setRoot(orders);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
