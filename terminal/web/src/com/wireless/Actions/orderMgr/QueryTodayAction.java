package com.wireless.Actions.orderMgr;

import java.util.Date;
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
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryTodayAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Order> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String restaurantID = request.getParameter("restaurantID");
			String ope = request.getParameter("ope");
			String cond = request.getParameter("havingCond");
			int type = Integer.parseInt(request.getParameter("type"));
			String filterVal = request.getParameter("value");
			
			StringBuffer extra = new StringBuffer(), orderBy = new StringBuffer();
			extra.append(" AND A.restaurant_id = " + restaurantID);
			String havingCond = null, filterCondition = null;
			
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
					havingCond = " HAVING A.status = 2 ";
				}else if(condType == 2){
					//是否有折扣
					havingCond = " HAVING A.discount_price > 0 ";
				}else if(condType == 3){
					//是否有赠送
					havingCond = " HAVING A.gift_price > 0 ";
				}else if(condType == 4){
					//是否有退菜
					havingCond = " HAVING A.cancel_price > 0 ";
				}else if(condType == 5){
					//是否有抹数
					havingCond = " HAVING A.erase_price > 0 ";				
				}else{
					havingCond = "";
				}
			}
			
			if(type == 1){
				//按账单号
				filterCondition = " AND A.id" + ope + filterVal;
			}else if(type == 2){
				//按流水号
				if(havingCond.equals("")){
					havingCond = " HAVING A.seq_id " + ope + filterVal;
				} else {
					havingCond = havingCond + " AND A.seq_id " + ope + filterVal;
				}
				filterCondition = "";
			}else if(type == 3){
				//按台号
				filterCondition = " AND A.table_alias" + ope + filterVal;
			}else if(type == 4){
				//按时间
				filterCondition = " AND A.order_date" + ope + "'" + DateUtil.formatToDate(new Date()) + " " + filterVal + "'";
			}else if(type == 5){
				//按类型
				filterCondition = " AND A.category" + ope + filterVal;
			}else if(type == 6){
				//按结帐方式
				filterCondition = " AND A.type" + ope + filterVal;
			}else if(type == 7){
				//按金额
				filterCondition = " AND A.total_price" + ope + filterVal;
			}else if(type == 8){
				//按实收
				filterCondition = " AND A.actual_price" + ope + filterVal;
			}else{
				filterCondition = "";
			}
			
			extra.append(filterCondition);
			extra.append(" GROUP BY A.id ");
			extra.append(havingCond);
			orderBy.append(" ORDER BY A.seq_id ASC ");		
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			list = OrderDao.getOrderByToday(paramsSet);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				Order sum = new Order();
				for(int i = 0; i < list.size(); i++){
					sum.setTotalPrice(sum.getTotalPrice() + list.get(i).getTotalPrice());
					sum.setActuralPrice(sum.getActuralPrice() + list.get(i).getActuralPrice());
				}
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				list.add(sum);
				jobject.setRoot(list);
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}
