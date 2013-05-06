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

import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

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
					}else{
						order.setServiceRate(order.getDestTbl().getServiceRate());
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
			if(order != null && order.hasOrderFood()){
				OrderFood item = null;
				int i = 0;
				for(com.wireless.protocol.OrderFood of : order.getOrderFoods()){
					idList += (i > 0 ? "," : "");
					idList += of.getFoodId();
					item = new OrderFood(of);
					item.setKitchenID(of.getKitchen().getId());
					root.add(item);
					item = null;
					i++;
				}
			}
			
			if(restaurantID != null && !restaurantID.trim().isEmpty()){
				List<Kitchen> kl = MenuDao.getKitchen(Integer.parseInt(restaurantID));
				for(OrderFood of : root){
					for(Kitchen temp : kl){
						if(of.getKitchenID() == temp.getId()){
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
				om.setOrderFoods(null);
				jobject.getOther().put("order", om);
				jobject.getOther().put("idList", idList);
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				jobject.initTip(false, e.getCode(), "操作失败, 没有获取到餐厅信息, 请重新确认!");
			} else if (e.getErrCode() == ProtocolError.TABLE_NOT_EXIST) {
				jobject.initTip(false, e.getCode(), "操作失败, 账单信息不正确, 请重新返回确认!");
			} else if (e.getErrCode() == ProtocolError.MENU_EXPIRED) {
				jobject.initTip(false, e.getCode(), "操作失败, 菜谱信息与服务器不匹配, 请与餐厅负责人确认或重新更新菜谱!");
			} else if (e.getErrCode() == ProtocolError.ORDER_NOT_EXIST) {
				jobject.initTip(false, e.getCode(), "操作失败, 账单信息不正确, 请重新返回确认!");
			} else {
				jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
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
