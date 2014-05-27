package com.wireless.Actions.orderMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.PayType;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class QueryTodayAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		List<Order> list = null;
		
		String dateType = request.getParameter("dataType");
		DateType dateTypeEnmu = DateType.valueOf(Integer.parseInt(dateType));
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		OrderDao.ExtraCond extraCond = new ExtraCond(DateType.valueOf(Integer.parseInt(dateType)));
		try{
			String pin = (String)request.getAttribute("pin");

			String comboType = request.getParameter("havingCond");
			String orderId = request.getParameter("orderId");
			String seqId = request.getParameter("seqId");
			String tableAlias = request.getParameter("tableAlias");
			String tableName = request.getParameter("tableName");
			String region = request.getParameter("region");
			String common = request.getParameter("common");
			String comboPayType = request.getParameter("comboPayType");
			
			if(comboType != null && !comboType.trim().isEmpty()){
				int comboVal = Integer.valueOf(comboType);
				if(comboVal == 1){
					//是否有反结帐
					extraCond.isRepaid(true);
				}else if(comboVal == 2){
					//是否有折扣
					extraCond.isDiscount(true);
				}else if(comboVal == 3){
					//是否有赠送
					extraCond.isGift(true);
				}else if(comboVal == 4){
					//是否有退菜
					extraCond.isCancelled(true);
				}else if(comboVal == 5){
					//是否有抹数
					extraCond.isErased(true);
				}else if(comboVal == 6){
					//是否有优惠劵
					extraCond.isCoupon(true);
				}
			}
			
			if(orderId != null && !orderId.isEmpty()){
				extraCond.setOrderId(Integer.parseInt(orderId));
			}
			if(seqId != null && !seqId.isEmpty()){
				extraCond.setSeqId(Integer.parseInt(seqId));
			}
			if(comboPayType != null && !comboPayType.equals("-1")){
				//按结帐方式
				extraCond.setPayType(PayType.valueOf(Integer.parseInt(comboPayType)));
			}
			if(common != null && !common.isEmpty()){
				extraCond.setComment(common);
			}
			if(tableAlias != null && !tableAlias.isEmpty()){
				extraCond.setTableAlias(Integer.parseInt(tableAlias));
			}
			if(tableName != null && !tableName.isEmpty()){
				extraCond.setTableName(tableName);
			}
			if(region != null && !region.equals("-1")){
				extraCond.setRegionId(Short.parseShort(region));
			}
			if(dateBeg != null && !dateBeg.isEmpty()){
				DutyRange orderRange = new DutyRange(dateBeg, dateEnd);
				extraCond.setOrderRange(orderRange);
			}
			String orderClause = " ORDER BY "+ extraCond.orderTbl +".order_date ASC " + " LIMIT " + start + "," + limit;
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			list = OrderDao.getPureOrder(staff, extraCond, orderClause);
			
			OrderSummary summary = OrderDao.getOrderSummary(staff, extraCond.toString(), dateTypeEnmu);
			
			jobject.setTotalProperty(summary.getTotalAmount());
			jobject.setRoot(list);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(!list.isEmpty() && dateTypeEnmu == DateType.TODAY){
				Order sum = new Order();
				sum.setDestTbl(new Table());
				for(int i = 0; i < list.size(); i++){
					sum.setTotalPrice(sum.getTotalPrice() + list.get(i).getTotalPrice());
					sum.setActualPrice(sum.getActualPrice() + list.get(i).getActualPrice());
				}
				sum.setDestTbl(list.get(0).getDestTbl());
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(sum);
				jobject.setRoot(list);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
