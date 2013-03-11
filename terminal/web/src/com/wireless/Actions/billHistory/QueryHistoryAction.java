package com.wireless.Actions.billHistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.system.SystemDao;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.system.DailySettle;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryHistoryAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		List<Order> list = null;
		Order sum = null;
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String restaurantID = request.getParameter("restaurantID");
//			String pin = request.getParameter("pin");
			String ope = request.getParameter("ope");
			String type = request.getParameter("type");
			String value = request.getParameter("value");
			String cond = request.getParameter("havingCond");
			
			String extra = "", groupBy = "", having = "", orderBy = "";
			
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
			
			if(cond != null && !cond.trim().isEmpty()){
				int condType = Integer.valueOf(cond);
				if(condType == 1){
					//是否有反结帐
					extra += " AND OH.status = 2 ";
				}else if(condType == 2){
					//是否有折扣
					extra += " AND OH.discount_price > 0 ";
				}else if(condType == 3){
					//是否有赠送
					extra += " AND OH.gift_price > 0 ";
				}else if(condType == 4){
					//是否有退菜
					extra += " AND OH.cancel_price > 0 ";
				}else if(condType == 5){
					//是否有抹数
					extra += " AND OH.erase_price > 0 ";				
				}else{
					extra += "";
				}
			}
			
			if(type.equals("1")){
				//按账单号
				extra += " AND OH.id" + ope + value;
			}else if(type.equals("2")){
				//按流水号
				extra += " AND OH.seq_id " + ope + value;
			}else if(type.equals("3")){
				//按台号
				extra += " AND OH.table_alias != '' ";
				extra += " AND OH.table_alias" + ope + value;
			}else if(type.equals("4")){
				//按日期
				String[] dutyParams = request.getParameter("value").split("<split>");
				extra += " AND OH.order_date BETWEEN '" + dutyParams[0] + "' AND '" + dutyParams[1] + "'";
			}else if(type.equals("5")){
				//按类型
				extra += " AND OH.category" + ope + value;
			}else if(type.equals("6")){
				//按结帐方式
				extra += " AND OH.type" + ope + value;
			}else if(type.equals("7")){
				//按金额
				extra += " AND OH.total_price" + ope + value;
			}else if(type.equals("8")){
				//按实收
				extra += " AND OH.actual_price" + ope + value;
			}else if(type.equals("9")){
				DailySettle ds = SystemDao.getDailySettle(Integer.valueOf(restaurantID), SystemDao.MAX_DAILY_SETTLE);
//				System.out.println("ds: "+ds.getOnDutyFormat()+"  -  "+ds.getOffDutyFormat());
				extra += " AND OH.order_date BETWEEN '" + ds.getOnDutyFormat() + "' AND '" + ds.getOffDutyFormat() + "'";
			}else{
				extra += "";
			}
			
			extra += " AND OH.restaurant_id = " + restaurantID;
			orderBy = " ORDER BY OH.order_date ASC ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			paramsSet.put(SQLUtil.SQL_PARAMS_GROUPBY, groupBy);
			paramsSet.put(SQLUtil.SQL_PARAMS_HAVING, having);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, start);
			paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, limit);
			
//			System.out.println(DateUtil.format(new Date()));
			list = OrderDao.getOrderByHistory(paramsSet);
//			System.out.println(DateUtil.format(new Date()));
			
			paramsSet.remove(SQLUtil.SQL_PARAMS_LIMIT_OFFSET);
			paramsSet.remove(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT);
			sum = OrderDao.getOrderByHistorySummary(paramsSet);
//			System.out.println(DateUtil.format(new Date()));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				jobject.setTotalProperty((int)sum.getId());
				jobject.setRoot(list);
				jobject.getOther().put("sum", sum);
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
}