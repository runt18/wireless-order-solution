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

import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

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
			String restaurantID = request.getParameter("restaurantID");
			String queryType = request.getParameter("queryType");
			com.wireless.protocol.Order[] ol = null;
			
			if(queryType == null){
				return null;
			}else if(Integer.valueOf(queryType) == QueryShiftDao.QUERY_TODAY){
				ol = QueryOrderDao.exec(" AND O.restaurant_id = " + restaurantID + " AND O.category <> " + Order.CATE_MERGER_CHILD, 
						" ORDER BY O.table_alias ", 
						QueryShiftDao.QUERY_TODAY);
			}else if(Integer.valueOf(queryType) == QueryShiftDao.QUERY_HISTORY){
				ol = QueryOrderDao.exec(" AND OH.restaurant_id = " + restaurantID + " AND OH.category <> " + Order.CATE_MERGER_CHILD,  
						" ORDER BY OH.table_alias ", 
						QueryShiftDao.QUERY_HISTORY);
			}
			
			for(int i = 0; i < ol.length; i++){
				com.wireless.protocol.Order temp = ol[i];
				item = new Order();
				item.setId(temp.getId());
				item.setCustomNum(temp.getCustomNum());
				item.setOrderDate(temp.orderDate);
				item.setServiceRate(temp.getServiceRate());
				item.setCategory(temp.getCategory());
				item.setStatus(Short.valueOf(temp.getStatus()+""));
				item.setErasePuotaPrice(temp.getErasePrice());
				item.setMinCost(temp.getMinimumCost());
				item.setRestaurantID(temp.restaurantID);
				item.setDiscountID(temp.getDiscount().discountID);
				item.setPayManner(Short.valueOf(temp.payManner+""));
				item.setTableID(temp.destTbl.tableID);
				item.setTableAlias(temp.destTbl.aliasID);
				item.setTableName(temp.destTbl.name);
				item.setTableID2(temp.srcTbl.tableID);
				item.setTableAlias2(temp.srcTbl.aliasID);
				item.setTableName2(temp.srcTbl.name);
				item.setOrderFoods(null);
				if(temp.isMerged()){
					List<Order> childList = item.getChildOrder();
					Order child = null;
					for(int k = 0; k < temp.getChildOrder().length; k++){
						com.wireless.protocol.Order kt = temp.getChildOrder()[k];
						child = new Order();
						child.setId(kt.getId());
						child.setCustomNum(kt.getCustomNum());
						child.setOrderDate(kt.orderDate);
						child.setServiceRate(kt.getServiceRate());
						child.setCategory(kt.getCategory());
						child.setStatus(Short.valueOf(kt.getStatus()+""));
						child.setErasePuotaPrice(kt.getErasePrice());
						child.setMinCost(kt.getMinimumCost());
						child.setRestaurantID(kt.restaurantID);
						child.setDiscountID(kt.getDiscount().discountID);
						child.setPayManner(Short.valueOf(kt.payManner+""));
						child.setTableID(kt.destTbl.tableID);
						child.setTableAlias(kt.destTbl.aliasID);
						child.setTableName(kt.destTbl.name);
						child.setTableID2(kt.srcTbl.tableID);
						child.setTableAlias2(kt.srcTbl.aliasID);
						child.setTableName2(kt.srcTbl.name);
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
