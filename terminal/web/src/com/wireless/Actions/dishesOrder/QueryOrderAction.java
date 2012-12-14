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
import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.TasteBasic;
import com.wireless.pojo.menuMgr.TasteGroup;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

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
					order = QueryOrder.execByID(orderID, QueryShiftDao.QUERY_HISTORY);
				} else {
					order = QueryOrder.execByID(orderID, QueryShiftDao.QUERY_TODAY);
				}
			}
			
			List<OrderFood> root = new ArrayList<OrderFood>();
			if(order.foods != null){
				OrderFood item = null;
				for(int i = 0; i < order.foods.length; i++){
					
					item = new OrderFood();
					item.setFoodName(order.foods[i].name);
					item.setFoodID(order.foods[i].foodID);
					item.setAliasID(order.foods[i].getAliasId());
					item.getKitchen().setKitchenID(Integer.parseInt(order.foods[i].kitchen.kitchenID+""));
					item.getKitchen().setDept(null);
					item.setCount(order.foods[i].getCount());
					item.setUnitPrice(order.foods[i].getPrice());
					item.setStatus(order.foods[i].getStatus());
					item.setDiscount(order.foods[i].getDiscount()); 
					item.setTemporary(order.foods[i].isTemporary);
					item.setSeqID(order.seqID);
					item.setOrderDate(order.foods[i].orderDate); 
					item.setWaiter(order.foods[i].waiter);
					item.setHangStatus(order.foods[i].hangStatus);
					
					if(order.foods[i].hasTaste()){
						// 
						TasteGroup tg = new TasteGroup();
						tg.getNormalTaste().setTasteName(order.foods[i].getTasteGroup().getTastePref());
						tg.getNormalTaste().setTastePrice(order.foods[i].getTasteGroup().getTastePrice());
						if(order.foods[i].getTasteGroup().getTmpTaste() != null){
							tg.getTempTaste().setTasteID(order.foods[i].getTasteGroup().getTmpTaste().tasteID);
							tg.getTempTaste().setTasteAliasID(order.foods[i].getTasteGroup().getTmpTaste().aliasID);
							tg.getTempTaste().setTasteName(order.foods[i].getTasteGroup().getTmpTaste().getPreference());
							tg.getTempTaste().setTastePrice(order.foods[i].getTasteGroup().getTmpTaste().getPrice());
						}
						// 
						for(Taste normalTaste : order.foods[i].getTasteGroup().getNormalTastes()){
							TasteBasic tb = new TasteBasic();
							tb.setTasteID(normalTaste.tasteID);
							tb.setTasteAliasID(normalTaste.aliasID);
							tb.setTasteCategory(normalTaste.category);
							tg.addTaste(tb);
						}
						item.setTasteGroup(tg);
					}else{
						item.getTasteGroup().getNormalTaste().setTasteName(com.wireless.protocol.TasteGroup.NO_TASTE_PREF);
						item.getTasteGroup().getNormalTaste().setTastePrice(0);
					}
					
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
							of.getKitchen().setDept(null);
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
			om.setErasePuotaPrice(order.getErasePrice());
			om.setMinCost(order.getMinimumCost());
			om.setRestaurantID(order.restaurantID);
			om.setDiscountID(order.getDiscount().discountID);
			om.setPayManner(Short.valueOf(order.payManner+""));
			om.setOrderFoods(null);
			
			jobject.getOther().put("order", om);

		} catch (BusinessException e) {
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				jobject.initTip(false, ErrorCode.TERMINAL_NOT_ATTACHED, "操作失败, 没有获取到餐厅信息,请重新确认!");
			} else if (e.errCode == ErrorCode.TABLE_NOT_EXIST) {
				jobject.initTip(false, ErrorCode.TABLE_NOT_EXIST, "操作失败, " + tableID + "号餐台信息不存在,请重新确认!");
			} else if (e.errCode == ErrorCode.MENU_EXPIRED) {
				jobject.initTip(false, ErrorCode.MENU_EXPIRED, "操作失败, 菜谱信息与服务器不匹配,请与餐厅负责人确认或重新更新菜谱!");
			} else if (e.errCode == ErrorCode.ORDER_NOT_EXIST) {
				jobject.initTip(false, ErrorCode.ORDER_NOT_EXIST, "操作失败, " + orderID + "号账单信息不存在,请重新确认!");
			} else {
				jobject.initTip(false, "没有获取到" + tableID + "号餐台的账单信息,请重新确认!");
			}
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			out.write(json.toString());
		}
		
		return null;
	}

}
