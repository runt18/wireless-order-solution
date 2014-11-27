package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxQueryOrderAction extends DispatchAction {
	
	public ActionForward getByMember(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");		
		
		String oid = request.getParameter("oid");
		String fid = request.getParameter("fid");
		DBCon dbCon = null;
		JObject jobject = new JObject();
		
		try {
			dbCon = new DBCon();
			dbCon.connect();
			
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			List<WxOrder> orders = WxOrderDao.getByCond(dbCon, staff, new WxOrderDao.ExtraCond().setWeixin(oid), " ORDER BY birth_date DESC");
			
			for (WxOrder wxOrder : orders) {
				if(wxOrder.getStatus() == WxOrder.Status.COMMITTED || wxOrder.getStatus() == WxOrder.Status.ORDER_ATTACHED){
					wxOrder.addFoods(WxOrderDao.getById(dbCon, staff, wxOrder.getId()).getFoods());
				}
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
	
	public ActionForward getByOrder(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");		
		
		String tid = request.getParameter("orderId");
		String pin = (String) request.getAttribute("pin");
		DBCon dbCon = null;
		JObject jobject = new JObject();
		
		try {
			dbCon = new DBCon();
			dbCon.connect();
			int orderId = 0;
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(tid != null && !tid.trim().isEmpty()){
				Table table = TableDao.getByAlias(staff, Integer.parseInt(tid));
				if(table.isBusy()){
					orderId = table.getOrderId();
				}else{
					throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
				}
				
			}			
			List<WxOrder> orders = WxOrderDao.getByCond(dbCon, staff, new WxOrderDao.ExtraCond().setOrder(orderId), null);
			
			for (int i = 0; i < orders.size(); i++) {
				orders.set(i, WxOrderDao.getById(dbCon, staff, orders.get(i).getId()));
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
