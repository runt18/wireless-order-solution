package com.wireless.Actions.system;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.CostAnalyzeReportDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalance.InsertBuilder;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.pojo.util.DateUtil;

public class UpdateCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		Calendar c = Calendar.getInstance();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String beginDate, endDate;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			//获取当前月
			long current = SystemDao.getCurrentMonth(staff);
			
			c.setTime(new Date(current));
			beginDate = sdf.format(c.getTime());
			//获取这个月中最后一天
			int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
			//格式化期末时间
			endDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day + " 23:59:59";
			
			List<Department> depts = DepartmentDao.getDepartments(staff, null, null);
			
			MonthlyBalance.InsertBuilder build = new InsertBuilder(staff.getRestaurantId(), staff.getName(), DateUtil.parseDate(beginDate));
			float openingBalance, endingBalance;
			//获取每个部门的期初和期末余额
			for (Department dept : depts) {
				openingBalance = CostAnalyzeReportDao.getBalance(beginDate, dept.getId());
				endingBalance = CostAnalyzeReportDao.getBalance(endDate, dept.getId());
				build.addMonthlyBalanceDetail(new MonthlyBalanceDetail.InsertBuilder(dept.getId(), openingBalance, endingBalance).setDeptName(dept.getName()).setRestaurantId(staff.getRestaurantId()).build());
			}
			
			MonthlyBalanceDao.insert(build);
			
			SystemDao.updateCurrentMonth(staff);
			jobject.initTip(true, "操作成功, 已经月结.");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
