package com.wireless.Actions.inventoryMgr.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.CostAnalyzeReportDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.CostAnalyze;
import com.wireless.util.WebParams;

public class QueryCostAnalyzeReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String beginDate = request.getParameter("beginDate");
			String endDate = "";
			Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.INVENTORY);
			List<CostAnalyze> list = new ArrayList<CostAnalyze>();
			Calendar c = Calendar.getInstance();
			if(beginDate == null){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				long current = SystemDao.getCurrentMonth(staff);
				
				c.setTime(new Date(current));
				c.add(Calendar.MONTH, -1);
				beginDate = sdf.format(c.getTime());
				int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
				endDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day;
				list = CostAnalyzeReportDao.getCostAnalyzes(staff, beginDate, endDate, null);
			}else{
				endDate = beginDate + "-31 23:59:59";
				list = CostAnalyzeReportDao.getCostAnalyzes(staff, beginDate + "-01", endDate, null);
			}
			jobject.setTotalProperty(list.size());
			jobject.setRoot(list);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}	
		return null;
	}
}
