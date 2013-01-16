package com.wireless.Actions.dishesOrder;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.VerifyPin;
import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.payment.PayOrder;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.Discount;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings("unchecked")
public class QueryOrderAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		JObject jobject = new JObject();
		String idList = "";
		try {
			response.setContentType("text/json; charset=utf-8");
			
			/**
			 * The parameters looks like below. 1st example, query order by
			 * table id pin=0x1 & tableID=201 2nd example, query order by order
			 * id pin=0x01 & orderID=40 pin : the pin the this terminal tableID
			 * : the order with this table ID to query
			 */
			String pin = request.getParameter("pin");
			String queryType = request.getParameter("queryType");
			String restaurantID = request.getParameter("restaurantID");
			String tid = request.getParameter("tableID");
			String oid = request.getParameter("orderID");
			String calc = request.getParameter("calc");
			String discountID = request.getParameter("discountID");
			String pricePlanID = request.getParameter("pricePlanID");
			String eraseQuota = request.getParameter("eraseQuota");
			String serviceRate = request.getParameter("serviceRate");
			String customNum = request.getParameter("customNum");
			
			Order order = new Order();
//			Table table = new Table();
//			order.setId(Integer.valueOf(orderID));
//			if(discountID != null && !discountID.trim().isEmpty()){
//				order.setDiscount(new Discount(Integer.valueOf(discountID)));
//			}
//			if(pricePlanID != null && !pricePlanID.trim().isEmpty()){
//				order.setPricePlan(new PricePlan(Integer.valueOf(pricePlanID)));
//			}
//			if(eraseQuota != null && !eraseQuota.trim().isEmpty()){
//				order.setErasePrice(Integer.valueOf(eraseQuota));
//			}
//			if(serviceRate != null && !serviceRate.trim().isEmpty()){
//				order.setServiceRate(Float.valueOf(serviceRate) / 100);
//			}
//			if(customNum != null && !customNum.trim().isEmpty()){
//				order.setCustomNum(Short.valueOf(customNum));
//			}
//			order.setDestTbl(table);
//			if (tid != null && !tid.trim().isEmpty()) {
//				tableID = Integer.parseInt(tid);
//				if(calc != null && Boolean.valueOf(calc)){
//					order.getDestTbl().setAliasId(tableID);
//					order = PayOrder.calcByTable(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), order);
//				}else{
//					order = QueryOrderDao.execByTable(Long.parseLong(pin), Terminal.MODEL_STAFF, tableID);
//				}
//			} else if (oid != null && !oid.trim().isEmpty()) {
//				orderID = Integer.parseInt(oid);
//				if (queryType.equals("History")) {
//					order = QueryOrderDao.execByID(orderID, QueryShiftDao.QUERY_HISTORY);
//				} else {
//					if(calc != null && Boolean.valueOf(calc)){
//						order = PayOrder.calcByID(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), order);
//					}else{
//						order = QueryOrderDao.execByID(orderID, QueryShiftDao.QUERY_TODAY);
//					}
//				}
//			}
			
			if(queryType != null && queryType.trim().equals("History")){
				if (oid != null && !oid.trim().isEmpty()){
					order = QueryOrderDao.execByID(Integer.valueOf(oid), QueryShiftDao.QUERY_HISTORY);
				}else{
					order = null;
				}
			}else{
				if(tid != null && !tid.trim().isEmpty()){
					order = QueryOrderDao.execByTable(Long.parseLong(pin), Terminal.MODEL_STAFF, Integer.parseInt(tid));
				} else if (oid != null && !oid.trim().isEmpty()){
					order = QueryOrderDao.execByID(Integer.valueOf(oid), QueryShiftDao.QUERY_TODAY);
				}
				if(calc != null && Boolean.valueOf(calc)){
					if(discountID != null && !discountID.trim().isEmpty()){
						order.setDiscount(new Discount(Integer.valueOf(discountID)));
					}
					if(pricePlanID != null && !pricePlanID.trim().isEmpty()){
						order.setPricePlan(new PricePlan(Integer.valueOf(pricePlanID)));
					}
					if(eraseQuota != null && !eraseQuota.trim().isEmpty()){
						order.setErasePrice(Integer.valueOf(eraseQuota));
					}
					if(serviceRate != null && !serviceRate.trim().isEmpty() && Float.valueOf(serviceRate.trim()) > 0){
						order.setServiceRate(Float.valueOf(serviceRate) / 100);
					}
					if(customNum != null && !customNum.trim().isEmpty() && Integer.valueOf(customNum.trim()) > 0){
						order.setCustomNum(Short.valueOf(customNum));
					}
					if(tid != null && !tid.trim().isEmpty()){
						order = PayOrder.calcByTable(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), order);
					} else if (oid != null && !oid.trim().isEmpty()){
						order = PayOrder.calcByID(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), order);
					}
				}
			}
			
			
			List<OrderFood> root = new ArrayList<OrderFood>();
			if(order != null && order.foods != null){
				OrderFood item = null;
				for(int i = 0; i < order.foods.length; i++){
					idList += (i > 0 ? "," : "");
					idList += (order.foods[i].getFoodId());
					item = new OrderFood(order.foods[i]);
					root.add(item);
					item = null;
				}
			}
			
			if(restaurantID != null && !restaurantID.trim().isEmpty()){
				List<Kitchen> kl = MenuDao.getKitchen(Integer.parseInt(restaurantID));
				for(OrderFood of : root){
					for(Kitchen temp : kl){
						if(of.getKitchenID() == temp.getKitchenID()){
							of.setKitchen(temp);
							of.getKitchen().setDept(null);
							break;
						}
					}
				}
			}
			
			jobject.setSuccess(true);
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
			
			if(order != null){
				com.wireless.pojo.dishesOrder.Order om = new com.wireless.pojo.dishesOrder.Order(order);
//				om.setServiceRate(order.getServiceRate() > 0 ? om.getServiceRate() : order.destTbl.getServiceRate());
				jobject.getOther().put("order", om);
				jobject.getOther().put("idList", idList);
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				jobject.initTip(false, ErrorCode.TERMINAL_NOT_ATTACHED, "操作失败, 没有获取到餐厅信息, 请重新确认!");
			} else if (e.errCode == ErrorCode.TABLE_NOT_EXIST) {
				jobject.initTip(false, ErrorCode.TABLE_NOT_EXIST, "操作失败, 账单信息不正确, 请重新返回确认!");
			} else if (e.errCode == ErrorCode.MENU_EXPIRED) {
				jobject.initTip(false, ErrorCode.MENU_EXPIRED, "操作失败, 菜谱信息与服务器不匹配, 请与餐厅负责人确认或重新更新菜谱!");
			} else if (e.errCode == ErrorCode.ORDER_NOT_EXIST) {
				jobject.initTip(false, ErrorCode.ORDER_NOT_EXIST, "操作失败, 账单信息不正确, 请重新返回确认!");
			} else {
				jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.errCode, e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

}
