package com.wireless.Actions.dishesOrder;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class QueryOrderByCalcAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		String idList = "";
		try {
			
			/**
			 * The parameters looks like below. 1st example, query order by
			 * table id pin=0x1 & tableID=201 2nd example, query order by order
			 * id pin=0x01 & orderID=40 pin : the pin the this terminal tableID
			 * : the order with this table ID to query
			 */
			String pin = (String)request.getAttribute("pin");
//			String queryType = request.getParameter("queryType");
			String restaurantID = (String)request.getAttribute("restaurantID");
			String tid = request.getParameter("tableID");
			String oid = request.getParameter("orderID");
			String calc = request.getParameter("calc");
			String discountId = request.getParameter("discountID");
			String eraseQuota = request.getParameter("eraseQuota");
			String serviceRate = request.getParameter("serviceRate");
			String customNum = request.getParameter("customNum");
			
			Order order = new Order();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(tid != null && !tid.trim().isEmpty()){
				order = OrderDao.getByTableAlias(staff, Integer.parseInt(tid));
			} else if (oid != null && !oid.trim().isEmpty()){
				order = OrderDao.getById(staff, Integer.valueOf(oid), DateType.TODAY);
			}
			if(calc != null && Boolean.valueOf(calc)){
				Order.PayBuilder payParam = Order.PayBuilder.build(order.getId());
				if(discountId != null && !discountId.trim().isEmpty()){
					payParam.setDiscountId(Integer.valueOf(discountId));
				}
				if(eraseQuota != null && !eraseQuota.trim().isEmpty()){
					payParam.setErasePrice(Integer.valueOf(eraseQuota));
				}
				if(customNum != null && !customNum.trim().isEmpty() && Integer.valueOf(customNum.trim()) > 0){
					payParam.setCustomNum(Short.valueOf(customNum));
				}
				if(serviceRate != null && !serviceRate.trim().isEmpty()){
					if(Float.valueOf(serviceRate.trim()) > 0){
						payParam.setServiceRate(Float.valueOf(serviceRate) / 100);						
					}
				}
				
				order = PayOrder.calc(staff, payParam);
				
			}else{
				order = PayOrder.calc(staff, Order.PayBuilder.build(order.getId()));
			}
			
			List<OrderFood> root = new ArrayList<OrderFood>();
			if(order != null && order.hasOrderFood()){
				OrderFood item = null;
				int i = 0;
				for(OrderFood of : order.getOrderFoods()){
					idList += (i > 0 ? "," : "");
					idList += of.getFoodId();
					item = new OrderFood(of);
					item.getKitchen().setId(of.getKitchen().getId());
					root.add(item);
					item = null;
					i++;
				}
			}
			
			if(restaurantID != null && !restaurantID.trim().isEmpty()){
				List<Kitchen> kl = MenuDao.getKitchen(Integer.parseInt(restaurantID));
				for(OrderFood of : root){
					for(Kitchen temp : kl){
						if(of.getKitchen().getId() == temp.getId()){
							of.asFood().setKitchen(temp);
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
				order.setOrderFoods(null);
//				order.setDiscount(null);
				jobject.getOther().put("order", order);
				jobject.getOther().put("idList", idList);
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
			
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
