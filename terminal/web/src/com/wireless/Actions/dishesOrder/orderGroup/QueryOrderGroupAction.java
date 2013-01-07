package com.wireless.Actions.dishesOrder.orderGroup;

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
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.payment.PayOrder;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.protocol.Discount;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings("unchecked")
public class QueryOrderGroupAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Order> orderGroup = new ArrayList<Order>();
		Order item = null;
		try{
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String queryType = request.getParameter("queryType");
			String orderID = request.getParameter("orderID");
			String calc = request.getParameter("calc");
			String discountID = request.getParameter("discountID");
			String pricePlanID = request.getParameter("pricePlanID");
			String status = request.getParameter("status");
			String category = request.getParameter("category");
			String eraseQuota = request.getParameter("eraseQuota");
			String serviceRate = request.getParameter("serviceRate");
			String customNum = request.getParameter("customNum");
			
			com.wireless.protocol.Order[] ol = null;
			StringBuffer extra = new StringBuffer();
			
			if(queryType == null){
				jobject.initTip(false, "操作失败, 获取数据类型错误.");
				return null;
			}
			
			if(calc != null && Boolean.valueOf(calc) && orderID != null){
				// 读取计算数据
				com.wireless.protocol.Order calcOrder = new com.wireless.protocol.Order();
				calcOrder.setId(Integer.valueOf(orderID));
				if(status != null && !status.trim().isEmpty()){
					calcOrder.setStatus(Integer.valueOf(status));
				}
				if(discountID != null && !discountID.trim().isEmpty()){
					calcOrder.setDiscount(new Discount(Integer.valueOf(discountID)));
				}
				if(pricePlanID != null && !pricePlanID.trim().isEmpty()){
					calcOrder.setPricePlan(new PricePlan(Integer.valueOf(pricePlanID)));
				}
				if(eraseQuota != null && !eraseQuota.trim().isEmpty()){
					calcOrder.setErasePrice(Integer.valueOf(eraseQuota));
				}
				if(serviceRate != null && !serviceRate.trim().isEmpty()){
					calcOrder.setServiceRate(Float.valueOf(serviceRate));
				}
				if(customNum != null && !customNum.trim().isEmpty()){
					calcOrder.setCustomNum(Short.valueOf(customNum));
				}
				if(calcOrder.isUnpaid()){
					calcOrder = PayOrder.calcByID(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), calcOrder);
				}else{
					calcOrder = QueryOrderDao.execByID(Integer.valueOf(orderID), Integer.valueOf(queryType));
				}
				if(calcOrder != null){
					ol = calcOrder.getChildOrder();		
					Order om = new Order(calcOrder);
					jobject.getOther().put("order", om);
				}
			}else{
				// 读取原始数据
				if(Integer.valueOf(queryType) == QueryShiftDao.QUERY_TODAY){
					if(orderID != null && !orderID.trim().isEmpty()){
						extra.append(" AND O.id = " + orderID.trim());
					}
					if(status != null && !status.trim().isEmpty()){
						extra.append(" AND O.status = " + status.trim());
					}
					if(category != null){
						extra.append(" AND O.category = " + category);
					}else{
						extra.append(" AND O.category <> " + Order.CATE_MERGER_CHILD);
					}
					extra.append(" AND O.restaurant_id = " + restaurantID);
					ol = QueryOrderDao.exec(extra.toString(), 
							" ORDER BY O.table_alias ", 
							QueryShiftDao.QUERY_TODAY);
				}else if(Integer.valueOf(queryType) == QueryShiftDao.QUERY_HISTORY){
					if(orderID != null && !orderID.trim().isEmpty()){
						extra.append(" AND OH.id = " + orderID.trim());
					}
					if(status != null && !status.trim().isEmpty()){
						extra.append(" AND OH.status = " + status.trim());
					}
					if(category != null){
						extra.append(" AND OH.category = " + category);
					}else{
						extra.append(" AND OH.category <> " + Order.CATE_MERGER_CHILD);
					}
					extra.append(" AND OH.restaurant_id = " + restaurantID);
					ol = QueryOrderDao.exec(extra.toString(),  
							" ORDER BY OH.table_alias ", 
							QueryShiftDao.QUERY_HISTORY);
				}
			}
			ol = ol == null ? new com.wireless.protocol.Order[0] : ol;
			for(int i = 0; i < ol.length; i++){
				com.wireless.protocol.Order temp = ol[i];
				item = new Order(temp);
				if(calc != null && Boolean.valueOf(calc) && orderID != null){
					List<OrderFood> orderFood = null;
					if(temp.foods != null){
						orderFood = new ArrayList<OrderFood>();
						OrderFood ofItem = null;
						for(int j = 0; j < temp.foods.length; j++){
							// 
							ofItem = new OrderFood(temp.foods[j]);
							orderFood.add(ofItem);
							ofItem = null;
						}
					}
					item.setOrderFoods(orderFood);
				}else{
					item.setOrderFoods(null);					
				}
				if(temp.isMerged()){
					List<Order> childList = item.getChildOrder();
					Order child = null;
					for(int k = 0; k < temp.getChildOrder().length; k++){
						com.wireless.protocol.Order kt = temp.getChildOrder()[k];
						child = new Order(kt);
						child.setOrderFoods(null);
						child.setChildOrder(null);
						childList.add(child);
						child = null;
					}
					item.setChildOrder(childList);
				}
				orderGroup.add(item);
				item = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			jobject.setRoot(orderGroup);
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

}
