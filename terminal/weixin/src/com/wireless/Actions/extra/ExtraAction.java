package com.wireless.Actions.extra;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.PinyinUtil;

public class ExtraAction extends DispatchAction{
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("text/plain");
		response.setCharacterEncoding("ASCII");
		response.addHeader("Content-Disposition", "attachment;filename=" + new String(("RLGZL1102MZ" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt").getBytes("GBK"), "UTF-8"));
		final String restaurantId = "353";
		final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
		try {
			
			final List<Order> orders =  OrderDao.getByCond(staff, new OrderDao.ExtraCond(DateType.valueOf(0)).addStatus(Order.Status.PAID)
																									   .addStatus(Order.Status.REPAID)
																									   .setCalcByDuty(true), "");

			long dailyId = DateUtil.parseDate(DateUtil.format(new Date()));
			for(Order order : orders){
				dailyId ++;
				final HashMap<String, String> map = new HashMap<>();
				map.put("restaurant", "");
				map.put("posId", "");
				map.put("orderDate", "");
				map.put("orderTime", "");
				map.put("orderId", "");
				map.put("orderDetail", "");
				map.put("receiveCash", "0.00");
				map.put("payByCard", "0.00");
				map.put("payByforeignCard", "0.00");
				map.put("payByMember", "0.00");
				map.put("payByCoupon", "0.00");
				map.put("payByOther", "0.00");
				map.put("disCount", "0.00");
				map.put("actualMoney", "0.00");
				map.put("orderNum", "");
				map.put("comment", "");
				
				map.replace("restaurant", "RLGZL1102MZ");
				map.replace("posId", "01");
				map.replace("orderDate", new SimpleDateFormat("yyyy/MM/dd").format(order.getOrderDate()));
				map.replace("orderTime", new SimpleDateFormat("hhmm").format(order.getOrderDate()));
				map.replace("orderId", "" + order.getId());
				map.replace("orderDetail", "00101717");
				map.replace("disCount", "" + order.getDiscountPrice());
				map.replace("actualMoney", "" + order.getActualPrice());
				
				if(order.getCouponPrice() != 0){
					map.replace("payByCoupon", "" + order.getCouponPrice());
				}
				
				if(order.getPaymentType().equals(PayType.CASH)){
					map.replace("receiveCash", "" + order.getActualPrice());
				}else if(order.getPaymentType().equals(PayType.CREDIT_CARD)){
					map.replace("payByCard", "" + order.getActualPrice());
				}else if(order.getPaymentType().equals(PayType.MEMBER)){
					map.replace("payByMember", "" + order.getActualPrice());
				}else if(order.getPaymentType().equals(PayType.MIXED)){
					Order currentOrder = OrderDao.getById(staff, order.getId(), DateType.TODAY);
					Iterator<PayType> iter = currentOrder.getMixedPayment().getPayments().keySet().iterator();
					while(iter.hasNext()){
						PayType payType = iter.next();
						if(payType.equals(PayType.CASH)){
							map.replace("receiveCash", "" + currentOrder.getMixedPayment().getPayments().get(payType));
						}else if(payType.equals(PayType.CREDIT_CARD)){
							map.replace("payByCard", "" + currentOrder.getMixedPayment().getPayments().get(payType));
						}else if(payType.equals(PayType.MEMBER)){
							map.replace("payByMember", "" + currentOrder.getMixedPayment().getPayments().get(payType));
						}else{
							if(map.get("payByOther").equals("")){
								map.replace("payByOther", "" + currentOrder.getMixedPayment().getPayments().get(payType));
							}else{
								map.replace("payByOther", "" + (Float.parseFloat(map.get("payByOther")) + currentOrder.getMixedPayment().getPayments().get(payType)));
							}
							map.replace("comment", map.get("comment") + payType.getName());
						}
					}
				}else{
					map.replace("payByOther", "" + order.getActualPrice());
					map.replace("comment", map.get("comment") + PinyinUtil.cn2Spell(order.getPaymentType().getName()));
				}
				
				if(Float.parseFloat(map.get("actualMoney")) != 0){
					response.getWriter().println(map.get("restaurant") + "\t" + 
												 map.get("posId") + "\t" + 
												 map.get("orderDate") + "\t" + 
												 map.get("orderTime") + "\t" + 
												 (dailyId != 0 ? dailyId : map.get("orderId")) + "\t" + 
												 map.get("orderDetail") + "\t" + 
												 map.get("receiveCash") + "\t" + 
												 map.get("payByCard") + "\t" + 
												 map.get("payByforeignCard") + "\t" + 
												 map.get("payByMember") + "\t" + 
												 map.get("payByCoupon") + "\t" + 
												 map.get("payByOther") + "\t" + 
												 map.get("disCount") + "\t" + 
												 map.get("actualMoney") + "\t" + 
												 map.get("orderNum") + "\t" + 
												 map.get("comment"));
				}
				
				if(order.getCancelPrice() != 0){
					dailyId ++;
					map.replace("receiveCash", "-" + order.getCancelPrice());
					map.replace("actualMoney", "-" + order.getCancelPrice());
					response.getWriter().println(map.get("restaurant") + "\t" + 
												 map.get("posId") + "\t" + 
												 map.get("orderDate") + "\t" + 
												 map.get("orderTime") + "\t" + 
												 (dailyId != 0 ? dailyId : map.get("orderId")) + "\t" + 
												 map.get("orderDetail") + "\t" + 
												 map.get("receiveCash") + "\t" + 
												 "0.00" + "\t" + 
												 "0.00" + "\t" + 
												 "0.00" + "\t" + 
												 "0.00" + "\t" + 
												 "0.00" + "\t" + 
												 "0.00" + "\t" + 
												 map.get("actualMoney") + "\t" + 
												 map.get("orderId") + "\t" + 
												 map.get("comment"));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
