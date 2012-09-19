package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.QueryOrder;
import com.wireless.db.QueryShift;
import com.wireless.db.menuMgr.MenuDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;

@SuppressWarnings("unchecked")
public class QueryOrderAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		int tableID = 0;
		int orderID = 0;
		PrintWriter out = null;
		JObject jobject = new JObject();
		try {
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, query order by
			 * table id pin=0x1 & tableID=201 2nd example, query order by order
			 * id pin=0x01 & orderID=40 pin : the pin the this terminal tableID
			 * : the order with this table ID to query
			 */
			String pin = request.getParameter("pin");
			String queryType = request.getParameter("queryType");
			String restaurantID = request.getParameter("restaurantID");
			
			
			Order order = null;
			if (request.getParameter("tableID") != null) {
				tableID = Integer.parseInt(request.getParameter("tableID"));
				order = QueryOrder.exec(Long.parseLong(pin), Terminal.MODEL_STAFF, tableID);

			} else if (request.getParameter("orderID") != null) {
				orderID = Integer.parseInt(request.getParameter("orderID"));
				if (queryType.equals("History")) {
					order = QueryOrder.execByID(orderID, QueryShift.QUERY_HISTORY);
				} else {
					order = QueryOrder.execByID(orderID, QueryShift.QUERY_TODAY);
				}
			}
			
			List<OrderFood> root = new ArrayList<OrderFood>();
			if(order.foods != null){
				OrderFood item = null;
				for(int i = 0; i < order.foods.length; i++){
					
					item = new OrderFood();
					item.setFoodName(order.foods[i].name);
					item.setFoodID(order.foods[i].foodID);
					item.setAliasID(order.foods[i].aliasID);
//					item.setKitchen(order.foods[i].kitchen);
					item.getKitchen().setKitchenID(Integer.parseInt(order.foods[i].kitchen.kitchenID+""));
					item.setTaste(order.foods[i].tastes);
					item.setTastePref(order.foods[i].getTastePref());
					item.setCount(order.foods[i].getCount());
					item.setUnitPrice(order.foods[i].getPrice());
					item.setTastePrice(order.foods[i].getTastePrice());
					item.setStatus(order.foods[i].status);
					item.setDiscount(order.foods[i].getDiscount()); 
					item.setTemporary(order.foods[i].isTemporary);
					item.setSeqID(order.seqID);
					item.setOrderDate(order.foods[i].orderDate); 
					item.setWaiter(order.foods[i].waiter);
					item.setTmpTaste(order.foods[i].tmpTaste);
					item.setHangStatus(order.foods[i].hangStatus);
					root.add(item);
					item = null;
				}
			}
			
			if(restaurantID != null && restaurantID.trim().length() > 0){
				List<Kitchen> kl = MenuDao.getKitchen(Integer.parseInt(restaurantID));
				for(OrderFood of : root){
					for(Kitchen temp : kl){
						if(of.getKitchenId() == temp.getKitchenID()){
							of.setKitchen(temp);
							break;
						}
					}
				}
			}
			
			jobject.setSuccess(true);
			jobject.setTotalProperty(root.size());
			jobject.setRoot(root);
			
			com.wireless.pojo.dishesOrder.Order om = new com.wireless.pojo.dishesOrder.Order();
			om.setId(order.id);
			om.setCustomNum(order.customNum);
			om.setOrderDate(order.orderDate);
			om.setServiceRate(order.getServiceRate());
			om.setCategory(order.category);
			om.setPaid(order.isPaid);
			om.getDestTbl().setMimnmuCost(order.getMinimumCost());
			om.setRestaurantID(order.restaurantID);
			
			jobject.getOther().put("order", om);

		} catch (BusinessException e) {
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				jobject.setSuccess(false);
				jobject.setCode(ErrorCode.TERMINAL_NOT_ATTACHED);
				jobject.setMsg("没有获取到餐厅信息,请重新确认!");
			} else if (e.errCode == ErrorCode.TABLE_NOT_EXIST) {
				jobject.setSuccess(false);
				jobject.setCode(ErrorCode.TABLE_NOT_EXIST);
				jobject.setMsg(tableID + "号餐台信息不存在,请重新确认!");
			} else if (e.errCode == ErrorCode.TABLE_IDLE) {
				jobject.setSuccess(true);
				jobject.setCode(ErrorCode.TABLE_IDLE);
				jobject.setMsg(null);
			} else if (e.errCode == ErrorCode.MENU_EXPIRED) {
				jobject.setSuccess(false);
				jobject.setCode(ErrorCode.MENU_EXPIRED);
				jobject.setMsg("菜谱信息与服务器不匹配,请与餐厅负责人确认或重新更新菜谱!");
			} else if (e.errCode == ErrorCode.ORDER_NOT_EXIST) {
				jobject.setSuccess(false);
				jobject.setCode(ErrorCode.ORDER_NOT_EXIST);
				jobject.setMsg(orderID + "号账单信息不存在,请重新确认!");
			} else {
				jobject.setSuccess(false);
				jobject.setMsg("没有获取到" + tableID + "号餐台的账单信息,请重新确认!");
			}
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			jobject.setSuccess(false);
			jobject.setMsg("数据库请求发生错误,请确认网络是否连接正常!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			out.write(json.toString());
		}
		
		return null;
	}

}
