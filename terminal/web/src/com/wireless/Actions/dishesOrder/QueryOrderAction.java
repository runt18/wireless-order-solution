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
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;
import com.wireless.util.WebParams;

public class QueryOrderAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");

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
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(queryType != null && queryType.trim().equals("History")){
				if (oid != null && !oid.trim().isEmpty()){
					order = OrderDao.getById(staff, Integer.valueOf(oid), DateType.HISTORY);
				}else{
					order = null;
				}
			}else{
				if(tid != null && !tid.trim().isEmpty()){
					order = OrderDao.getByTableAlias(staff, Integer.parseInt(tid));
				} else if (oid != null && !oid.trim().isEmpty()){
					order = OrderDao.getById(staff, Integer.valueOf(oid), DateType.TODAY);
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
						order = PayOrder.calcByTable(staff, order);
					} else if (oid != null && !oid.trim().isEmpty()){
						order = PayOrder.calcById(staff, order);
					}
				}
			}
			
			List<OrderFood> root = new ArrayList<OrderFood>();
			if(order != null && order.hasOrderFood()){
				OrderFood item = null;
				int i = 0;
				for(com.wireless.pojo.dishesOrder.OrderFood of : order.getOrderFoods()){
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
				order.setChildOrder(null);
//				order.setDiscount(null);
				jobject.getOther().put("order", order);
				jobject.getOther().put("idList", idList);
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
