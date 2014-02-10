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

import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryOrderGroupAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<Order> orderGroup = new ArrayList<Order>();
		Order item = null;
		try{
			String pin = (String)request.getAttribute("pin");
			String restaurantID = (String)request.getAttribute("restaurantID");
			String queryType = request.getParameter("queryType");
			String orderID = request.getParameter("orderID");
			String childTableAliasID = request.getParameter("childTableAliasID");
			String calc = request.getParameter("calc");
			String discountID = request.getParameter("discountID");
			String status = request.getParameter("status");
			String category = request.getParameter("category");
			String eraseQuota = request.getParameter("eraseQuota");
			String serviceRate = request.getParameter("serviceRate");
			String customNum = request.getParameter("customNum");
			boolean hasFood = request.getParameter("hasFood") != null ? Boolean.valueOf(request.getParameter("hasFood")) : false;
			
			List<com.wireless.pojo.dishesOrder.Order> ol = null;
			StringBuffer orderClause = new StringBuffer();
			
			if(queryType == null){
				jobject.initTip(false, "操作失败, 获取数据类型错误.");
				return null;
			}
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(calc != null && Boolean.valueOf(calc) && orderID != null){
				// 读取计算数据
				com.wireless.pojo.dishesOrder.Order calcOrder = new com.wireless.pojo.dishesOrder.Order();
				calcOrder.setId(Integer.valueOf(orderID));
				if(status != null && !status.trim().isEmpty()){
					calcOrder.setStatus(Integer.valueOf(status));
				}
				if(discountID != null && !discountID.trim().isEmpty()){
					calcOrder.setDiscount(new Discount(Integer.valueOf(discountID)));
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
					calcOrder = PayOrder.calc(staff, calcOrder);
				}else{
					calcOrder = OrderDao.getById(staff, Integer.valueOf(orderID), DateType.valueOf(queryType));
				}
				if(calcOrder != null){
					ol = calcOrder.getChildOrder();		
					jobject.getOther().put("order", ol);
				}
			}else{
				StringBuilder extraCond = new StringBuilder();
				//extraCond.append(" AND O.restaurant_id = " + restaurantID);
				// 读取原始数据
				if(Integer.valueOf(queryType) == DateType.TODAY.getValue()){
					if(status != null && !status.trim().isEmpty()){
						extraCond.append(" AND O.status = " + status.trim());
					}
					if(category != null){
						extraCond.append(" AND O.category = " + category);
					}else{
						extraCond.append(" AND O.category <> " + Order.Category.MERGER_CHILD.getVal());
					}
					
					if(childTableAliasID != null && !childTableAliasID.trim().isEmpty()){
						Table childTable = new Table();
						childTable.setRestaurantId(Integer.valueOf(restaurantID));
						childTable.setTableAlias(Integer.valueOf(childTableAliasID));
						ol = OrderDao.getOrderByChild(staff, extraCond.toString(), orderClause.toString(), DateType.TODAY, childTable);
						if(hasFood){
							for(int i = 0; i < ol.size(); i++){
								for(int j = 0; j < ol.get(i).getChildOrder().size(); j++){
									ol.get(i).getChildOrder().get(j).setOrderFoods(OrderDao.getById(staff, Integer.valueOf(ol.get(i).getChildOrder().get(j).getId()), DateType.TODAY).getOrderFoods());
								}
							}
						}
					}else{
						if(orderID != null && !orderID.trim().isEmpty()){
							extraCond.append(" AND O.id = " + orderID.trim());							
						}
						ol = OrderDao.getByCond(staff, extraCond.toString(), " ORDER BY O.table_alias ",	DateType.TODAY);
					}
				}else if(Integer.valueOf(queryType) == DateType.HISTORY.getValue()){
					if(status != null && !status.trim().isEmpty()){
						extraCond.append(" AND OH.status = " + status.trim());
					}
					if(category != null){
						extraCond.append(" AND OH.category = " + category);
					}else{
						extraCond.append(" AND OH.category <> " + Order.Category.MERGER_CHILD.getVal());
					}
					
					if(childTableAliasID != null && !childTableAliasID.trim().isEmpty()){
						Table childTable = new Table();
						childTable.setRestaurantId(Integer.valueOf(restaurantID));
						childTable.setTableAlias(Integer.valueOf(childTableAliasID));
						ol = OrderDao.getOrderByChild(staff, extraCond.toString(), orderClause.toString(), DateType.HISTORY, childTable);
						if(hasFood){
							for(int i = 0; i < ol.size(); i++){
								for(int j = 0; j < ol.get(i).getChildOrder().size(); j++){
									ol.get(i).getChildOrder().get(j).setOrderFoods(OrderDao.getById(staff, Integer.valueOf(ol.get(i).getChildOrder().get(j).getId()), DateType.HISTORY).getOrderFoods());
								}
							}
						}
					}else{
						if(orderID != null && !orderID.trim().isEmpty()){
							extraCond.append(" AND OH.id = " + orderID.trim());							 
						}
						ol = OrderDao.getByCond(staff, extraCond.toString(), " ORDER BY OH.table_alias ", DateType.HISTORY);
					}
				}
			}
			ol = ol == null ? new ArrayList<com.wireless.pojo.dishesOrder.Order>() : ol;
			for(int i = 0; i < ol.size(); i++){
				com.wireless.pojo.dishesOrder.Order temp = ol.get(i);
//				item = new Order(temp);
				item = temp;
				if(calc != null && Boolean.valueOf(calc) && orderID != null){
					List<OrderFood> orderFood = null;
					if(temp.hasOrderFood()){
						orderFood = new ArrayList<OrderFood>();
						OrderFood ofItem = null;
						for(com.wireless.pojo.dishesOrder.OrderFood of : temp.getOrderFoods()){
							// 
							ofItem = new OrderFood(of);
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
					for(int k = 0; k < temp.getChildOrder().size(); k++){
						com.wireless.pojo.dishesOrder.Order kt = temp.getChildOrder().get(k);
						child = kt;
//						child.setOrderFoods(null);
						child.setOrderFoods(kt.getOrderFoods());
//						child.setChildOrder(null);
//						childList.add(child);
						child = null;
					}
					item.setChildOrder(childList);
				}
				orderGroup.add(item);
				item = null;
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
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
