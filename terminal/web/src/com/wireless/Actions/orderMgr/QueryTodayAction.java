package com.wireless.Actions.orderMgr;

import java.util.Date;
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
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;
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
			String ope = request.getParameter("ope");
			String pin = (String) request.getSession().getAttribute("pin");
			String filterVal = request.getParameter("value");
			
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
					comboCond = " AND O.status = " + Order.Status.REPAID.getVal();
				}else if(comboVal == 2){
					//是否有折扣
					comboCond = " AND O.discount_price > 0 ";
				}else if(comboVal == 3){
					//是否有赠送
					comboCond = " AND O.gift_price > 0 ";
				}else if(comboVal == 4){
					//是否有退菜
					comboCond = " AND O.cancel_price > 0 ";
				}else if(comboVal == 5){
					//是否有抹数
					comboCond = " AND O.erase_price > 0 ";				
				}else{
					comboCond = "";
				}
			}else{
				comboCond = "";
			}
			
			String filterCond;
			int type = Integer.parseInt(request.getParameter("type"));
			if(type == 1){
				//按账单号
				filterCond = " AND O.id " + ope + filterVal;
			}else if(type == 2){
				//按流水号
				filterCond = " AND O.seq_id " + ope + filterVal; 
			}else if(type == 3){
				//按台号
				filterCond = " AND O.table_alias " + ope + filterVal;
			}else if(type == 4){
				//按时间
				filterCond = " AND O.order_date " + ope + "'" + DateUtil.formatToDate(new Date()) + " " + filterVal + "'";
			}else if(type == 5){
				//按金额
				filterCond = " AND O.total_price" + ope + filterVal;
			}else if(type == 6){
				//按实收
				filterCond = " AND O.actual_price" + ope + filterVal;
			}else if(type == 7){
				//按类型
				filterCond = " AND O.category " + ope + filterVal;
			}else if(type == 8){
				//按结帐方式
				filterCond = " AND O.pay_type " + ope + filterVal;
			}else{
				filterCond = "";
			}
			
			StringBuilder extraCond = new StringBuilder(); 
			extraCond.append(" AND O.seq_id IS NOT NULL ")
				 	 .append(comboCond)
				 	 .append(filterCond);
			
			String orderClause = " ORDER BY O.seq_id ASC ";
			
			list = OrderDao.getPureOrder(StaffDao.verify(Integer.parseInt(pin), Privilege.Code.FRONT_BUSINESS), extraCond.toString(), orderClause, DateType.TODAY);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				Order sum = new Order();
				sum.setDestTbl(new Table());
				for(int i = 0; i < list.size(); i++){
					sum.setTotalPrice(sum.getTotalPrice() + list.get(i).getTotalPrice());
					sum.setActualPrice(sum.getActualPrice() + list.get(i).getActualPrice());
				}
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				list.add(sum);
				jobject.setRoot(list);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
