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
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.DailySettle;
import com.wireless.util.DateType;
import com.wireless.util.WebParams;

public class QueryHistoryAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String restaurantId = request.getParameter("restaurantID");
			String pin = (String)request.getAttribute("pin");
			String value = request.getParameter("value");
			
			String ope = request.getParameter("ope");
			if(ope != null && !ope.trim().isEmpty()){
				int opeType = Integer.parseInt(ope);
				
				if(opeType == 1){
					ope = "=";
				}else if(opeType == 2){
					ope = ">=";
				}else if(opeType == 3){
					ope = "<=";
				}else{
					ope = "=";
				}
			}else{
				ope = "=";
			}
			
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
				}else{
					comboCond = "";
				}
			}else{
				comboCond = "";
			}
			
			String filterCond;
			String type = request.getParameter("type");
			if(type.equals("1")){
				//按账单号
				filterCond = " AND OH.id" + ope + value;
			}else if(type.equals("2")){
				//按流水号
				filterCond = " AND OH.seq_id " + ope + value;
			}else if(type.equals("3")){
				//按台号
				filterCond = " AND OH.table_alias != '' " + " AND OH.table_alias" + ope + value;
			}else if(type.equals("4")){
				//按日期
				String[] dutyParams = request.getParameter("value").split("<split>");
				filterCond = " AND OH.order_date BETWEEN '" + dutyParams[0] + "' AND '" + dutyParams[1] + "'";
			}else if(type.equals("5")){
				//按类型
				filterCond = " AND OH.category " + ope + value;
			}else if(type.equals("6")){
				//按结帐方式
				filterCond = " AND OH.pay_type " + ope + value;
			}else if(type.equals("7")){
				//按金额
				filterCond = " AND OH.total_price " + ope + value;
			}else if(type.equals("8")){
				//按实收
				filterCond = " AND OH.actual_price " + ope + value;
			}else if(type.equals("9")){
				DailySettle ds = SystemDao.getDailySettle(Integer.valueOf(restaurantId), SystemDao.MAX_DAILY_SETTLE);
//				System.out.println("ds: "+ds.getOnDutyFormat()+"  -  "+ds.getOffDutyFormat());
				filterCond = " AND OH.order_date BETWEEN '" + ds.getOnDutyFormat() + "' AND '" + ds.getOffDutyFormat() + "'";
			}else{
				filterCond = "";
			}
			
			String orderClause = " ORDER BY OH.order_date ASC " + " LIMIT " + start + "," + limit;
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			List<Order> list = OrderDao.getPureOrder(staff, comboCond + filterCond, orderClause, DateType.HISTORY);
			
			OrderSummary summary = OrderDao.getOrderSummary(staff, comboCond + filterCond, DateType.HISTORY);
			
			jobject.setTotalProperty(summary.getTotalAmount());
			jobject.setRoot(list);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}