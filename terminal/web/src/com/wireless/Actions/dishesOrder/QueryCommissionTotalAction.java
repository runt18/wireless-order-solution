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
import com.wireless.pojo.dishesOrder.CommissionStatistics;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

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
				list = CalcBillStatisticsDao.getCommissionTotalByDept(staff, range, Integer.parseInt(deptId), DateType.HISTORY);
			}else{
				list = CalcBillStatisticsDao.getCommissionTotal(staff, range, DateType.HISTORY);
			}
			if(!list.isEmpty()){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, true, start, limit));
			}
		}catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
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
