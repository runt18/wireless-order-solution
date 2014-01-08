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
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.RepaidStatistics;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class QueryRepaidReportAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String staffId = request.getParameter("staffId");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			DutyRange range = new DutyRange(beginDate, endDate);
			List<RepaidStatistics> list;
			if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
				list = CalcBillStatisticsDao.getRepaidStatisticsByStaffId(staff, range, Integer.parseInt(staffId), DateType.HISTORY);
			}else{
				list = CalcBillStatisticsDao.getRepaidStatistics(staff, range, DateType.HISTORY);
			}
			if(!list.isEmpty()){
				jobject.setTotalProperty(list.size());
				RepaidStatistics total = new RepaidStatistics();
				for (RepaidStatistics item : list) {
					total.setmRepaidPrice(total.getmRepaidPrice() + item.getmRepaidPrice());
				}
				list = DataPaging.getPagingData(list, true, start, limit);
				list.add(total);
				jobject.setRoot(list);
			}
		}catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
