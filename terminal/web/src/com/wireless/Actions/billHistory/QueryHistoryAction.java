package com.wireless.Actions.billHistory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class QueryHistoryAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");

			String comboCond;
			String comboType = request.getParameter("havingCond");
			if(comboType != null && !comboType.trim().isEmpty()){
				int comboVal = Integer.valueOf(comboType);
				if(comboVal == 1){
					//是否有反结帐
					comboCond = " AND OH.status = " + Order.Status.REPAID.getVal();
				}else if(comboVal == 2){
					//是否有折扣
					comboCond = " AND OH.discount_price > 0 ";
				}else if(comboVal == 3){
					//是否有赠送
					comboCond = " AND OH.gift_price > 0 ";
				}else if(comboVal == 4){
					//是否有退菜
					comboCond = " AND OH.cancel_price > 0 ";
				}else if(comboVal == 5){
					//是否有抹数
					comboCond = " AND OH.erase_price > 0 ";				
				}else if(comboVal == 6){
					//是否有抹数
					comboCond = " AND OH.coupon_price > 0 ";				
				}else{
					comboCond = "";
				}
			}else{
				comboCond = "";
			}
			
			String filterCond = "";
			
			String value = request.getParameter("value");
			if(value != null && !value.isEmpty()){
				filterCond += " AND OH.id = " + value;
			}
			String type = request.getParameter("type");
			if(Boolean.parseBoolean(type)){
				String beginDate = request.getParameter("beginDate");
				String endDate = request.getParameter("endDate");
				String comboPayType = request.getParameter("comboPayType");
				String common = request.getParameter("common");
				
				filterCond += " AND OH.order_date BETWEEN '" + beginDate + "' AND '" + endDate + "'";
				
				if(comboPayType != null && !comboPayType.equals("-1")){
					//按结帐方式
					filterCond += " AND OH.pay_type = " + comboPayType;
				}
				if(common != null && !common.isEmpty()){
					filterCond += " AND OH.comment LIKE '%" + common + "%' ";
				}
				
			}
			String orderClause = " ORDER BY OH.order_date ASC " + " LIMIT " + start + "," + limit;
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			List<Order> list = OrderDao.getPureOrder(staff, comboCond + filterCond, orderClause, DateType.HISTORY);
			
			OrderSummary summary = OrderDao.getOrderSummary(staff, comboCond + filterCond, DateType.HISTORY);
			
			jobject.setTotalProperty(summary.getTotalAmount());
			jobject.setRoot(list);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}