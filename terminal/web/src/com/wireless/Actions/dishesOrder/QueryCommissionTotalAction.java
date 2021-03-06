package com.wireless.Actions.dishesOrder;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.commission.CommissionStatistics;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class QueryCommissionTotalAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String deptId = request.getParameter("deptId");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			DutyRange range = new DutyRange(beginDate, endDate);
			List<CommissionStatistics> list;
			if(deptId != null && !deptId.equals("-1")){
				list = CalcBillStatisticsDao.calcCommissionTotalByDept(staff, range, Integer.parseInt(deptId), DateType.HISTORY);
			}else{
				list = CalcBillStatisticsDao.calcCommissionTotal(staff, range, DateType.HISTORY);
			}
			if(!list.isEmpty()){
				jobject.setTotalProperty(list.size());
				CommissionStatistics total = new CommissionStatistics();
				for (CommissionStatistics item : list) {
					total.setTotalPrice(item.getTotalPrice() + total.getTotalPrice());
					total.setCommission(item.getCommission() + total.getCommission());
				}
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(total);
				jobject.setRoot(list);
			}
		}catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
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
