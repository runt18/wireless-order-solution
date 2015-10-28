package com.wireless.Actions.orderMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class QueryOrderStatisticsAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		List<Order> list = null;
		List<Order> totalList = null;
		
		String dateType = request.getParameter("dataType");
		DateType dateTypeEnmu = DateType.valueOf(Integer.parseInt(dateType));
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String isRange = request.getParameter("isRange");
		
		String businessHourBeg = request.getParameter("opening");
		String businessHourEnd = request.getParameter("ending");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		DBCon dbCon = new DBCon();
		
		
		OrderDao.ExtraCond extraCond = new ExtraCond(DateType.valueOf(Integer.parseInt(dateType)));
		try{
			String pin = (String)request.getAttribute("pin");

			String comboType = request.getParameter("havingCond");
			String orderId = request.getParameter("orderId");
			String seqId = request.getParameter("seqId");
			String tableAlias = request.getParameter("tableAlias");
			String tableName = request.getParameter("tableName");
			String region = request.getParameter("region");
			String staffId = request.getParameter("staffId");
			String common = request.getParameter("common");
			String comboPayType = request.getParameter("comboPayType");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
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
				}else if(comboVal == 7){
					//是否有转菜
					extraCond.isTransfer(true);
				}else if(comboVal == 8){
					//是否有会员价
					extraCond.isMemberPrice(true);
				}
			}
			
			if(orderId != null && !orderId.isEmpty()){
				extraCond.setOrderId(Integer.parseInt(orderId));
			}
			if(seqId != null && !seqId.isEmpty()){
				extraCond.setSeqId(Integer.parseInt(seqId));
			}
			if(comboPayType != null && !comboPayType.isEmpty() && !comboPayType.equals("-1")){
				//按结帐方式
				extraCond.setPayType(new PayType(Integer.parseInt(comboPayType)));
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
			if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
				extraCond.setStaff(Integer.parseInt(staffId));
			}
			if(region != null && !region.equals("-1")){
				extraCond.setRegionId(Region.RegionId.valueOf(Short.parseShort(region)));
			}
			if(dateBeg != null && !dateBeg.isEmpty()){
				dbCon.connect();
				
				if(isRange != null && !isRange.isEmpty()){
					
					DutyRange range = DutyRangeDao.exec(dbCon, staff, 
							DateUtil.format(DateUtil.parseDate(dateBeg), DateUtil.Pattern.DATE_TIME), 
							DateUtil.format(DateUtil.parseDate(dateEnd), DateUtil.Pattern.DATE_TIME));				
					
					extraCond.setOrderRange(new DutyRange(range.getOnDutyFormat(), range.getOffDutyFormat()));
				}else{
					extraCond.setOrderRange(new DutyRange(dateBeg, dateEnd));
				}
			}
			if(businessHourBeg != null && !businessHourBeg.isEmpty()){
				extraCond.setHourRange(new HourRange(businessHourBeg, businessHourEnd, DateUtil.Pattern.HOUR));
			}
			extraCond.addStatus(Order.Status.PAID);
			extraCond.addStatus(Order.Status.REPAID);
			
			String orderClause = " ORDER BY "+ extraCond.orderTblAlias +".order_date ASC " + " LIMIT " + start + "," + limit;
			
			
			
			list = OrderDao.getByCond(staff, extraCond, orderClause);
			totalList = OrderDao.getByCond(staff, extraCond, null);
			
			OrderSummary summary = OrderDao.getOrderSummary(staff, extraCond, dateTypeEnmu);
			
			jobject.setTotalProperty(summary.getTotalAmount());
			jobject.setRoot(list);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			dbCon.disconnect();
			if(!list.isEmpty() && dateTypeEnmu == DateType.TODAY){
				Order sum = new Order();
				sum.setDestTbl(new Table());
				for(int i = 0; i < totalList.size(); i++){
					sum.setTotalPrice(sum.getTotalPrice() + totalList.get(i).getTotalPrice());
					sum.setActualPrice(sum.getActualPrice() + totalList.get(i).getActualPrice());
				}
				sum.setDestTbl(totalList.get(0).getDestTbl());
				list.add(sum);
				jobject.setRoot(list);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
