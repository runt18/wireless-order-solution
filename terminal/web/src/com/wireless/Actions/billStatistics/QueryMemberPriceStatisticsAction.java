package com.wireless.Actions.billStatistics;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcMemberPriceDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class QueryMemberPriceStatisticsAction extends DispatchAction{
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<Order> list;
			
			CalcMemberPriceDao.ExtraCond extraCond = new CalcMemberPriceDao.ExtraCond(DateType.HISTORY);
			
			if(opening != null && !opening.isEmpty()){
				extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
			}
			
			list = CalcMemberPriceDao.getMemberPriceDetail(staff, new DutyRange(beginDate, endDate), extraCond);
			
			if(!list.isEmpty()){
				jobject.setTotalProperty(list.size());
				Order total = new Order();
				for (Order item : list) {
					total.setPurePrice(item.getPurePrice() + total.getPurePrice());
					total.setActualPrice(item.getActualPrice() + total.getActualPrice());
				}
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(total);
				
			}
			jobject.setRoot(list);
		}catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
