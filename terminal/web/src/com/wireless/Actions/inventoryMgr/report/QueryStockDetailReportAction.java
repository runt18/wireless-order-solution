package com.wireless.Actions.inventoryMgr.report;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockDetailReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.util.WebParams;

public class QueryStockDetailReportAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		List<StockDetailReport> stockDetailReports = null;
		//String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int roots = 0;
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String materialId = request.getParameter("materialId");
			String deptId = request.getParameter("deptId");
			//String stockType = request.getParameter("stockType");
			String subType = request.getParameter("subType");
			String extra = " AND S.ori_stock_date >= '" + beginDate + "' AND S.ori_stock_date <= '" + endDate + " 23:59:59'";
			
			
			if(subType != null && !subType.isEmpty()){
				extra += " AND S.sub_type = " + subType;
			}
				
			if(deptId.equals("-1")){
				stockDetailReports = StockDetailReportDao.getStockDetailReport(staff, Integer.parseInt(materialId), extra, " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit));
			}else{
				extra += " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")";
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDept(staff, Integer.parseInt(materialId), extra, " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit), Integer.parseInt(deptId));
			}
			
			roots = StockDetailReportDao.getStockDetailReportCount(staff, Integer.parseInt(materialId), extra, null);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(stockDetailReports != null){
				jobject.setTotalProperty(roots);
				jobject.setRoot(stockDetailReports);
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}
