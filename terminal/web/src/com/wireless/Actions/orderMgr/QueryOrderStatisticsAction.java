package com.wireless.Actions.orderMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class QueryOrderStatisticsAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String comboType = request.getParameter("havingCond");
		final String orderId = request.getParameter("orderId");
		final String seqId = request.getParameter("seqId");
		final String tableAlias = request.getParameter("tableAlias");
		final String tableName = request.getParameter("tableName");
		final String region = request.getParameter("region");
		final String staffId = request.getParameter("staffId");
		final String comment = request.getParameter("common");
		final String comboPayType = request.getParameter("comboPayType");

		final String dateType = request.getParameter("dataType");
		final DateType dateTypeEnmu = DateType.valueOf(Integer.parseInt(dateType));
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		
		final String businessHourBeg = request.getParameter("opening");
		final String businessHourEnd = request.getParameter("ending");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		
		final JObject jObject = new JObject();
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final OrderDao.ExtraCond extraCond = new ExtraCond(DateType.valueOf(Integer.parseInt(dateType)))
													.addStatus(Order.Status.PAID)
													.addStatus(Order.Status.REPAID);
			
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
			
			if(comment != null && !comment.isEmpty()){
				extraCond.setComment(comment);
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
			
			if(dateBeg != null && !dateBeg.isEmpty() && dateEnd != null && !dateEnd.isEmpty()){
				DutyRange range = DutyRangeDao.exec(staff, 
						DateUtil.format(DateUtil.parseDate(dateBeg), DateUtil.Pattern.DATE_TIME), 
						DateUtil.format(DateUtil.parseDate(dateEnd), DateUtil.Pattern.DATE_TIME));	
				if(range == null){
					range = new DutyRange(dateBeg, dateEnd);
				}
				extraCond.setOrderRange(range);
			}
			
			if(businessHourBeg != null && !businessHourBeg.isEmpty()){
				extraCond.setHourRange(new HourRange(businessHourBeg, businessHourEnd, DateUtil.Pattern.HOUR));
			}
			
			String orderClause = " ORDER BY " + extraCond.orderTblAlias + ".order_date ASC ";
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				orderClause += " LIMIT " + start + "," + limit;
			}
			
			final List<Order> result = OrderDao.getByCond(staff, extraCond, orderClause);
			jObject.setTotalProperty(OrderDao.getByCond(staff, extraCond.setOnlyAmount(true), null).size());
			
			if(!result.isEmpty() && dateTypeEnmu == DateType.TODAY){
				Order sum = new Order();
				for(int i = 0; i < result.size(); i++){
					sum.setTotalPrice(sum.getTotalPrice() + result.get(i).getTotalPrice());
					sum.setActualPrice(sum.getActualPrice() + result.get(i).getActualPrice());
				}
				sum.setDestTbl(result.get(0).getDestTbl());
				result.add(sum);
			}
			
			jObject.setRoot(result);
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}
