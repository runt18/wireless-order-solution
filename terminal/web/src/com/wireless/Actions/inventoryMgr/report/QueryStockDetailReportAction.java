package com.wireless.Actions.inventoryMgr.report;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.stockMgr.StockDetailReportDao;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.util.WebParams;

public class QueryStockDetailReportAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<StockDetailReport> stockDetailReports = null;
		//String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int roots = 0;
		try{
			
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String materialId = request.getParameter("materialId");
			String deptId = request.getParameter("deptId");
			
			if(materialId == null){
				stockDetailReports = new ArrayList<StockDetailReport>();
			}else if(deptId == null){
				roots = StockDetailReportDao.getStockDetailReportByDateCount(beginDate, endDate, Integer.parseInt(materialId), null);
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(beginDate, endDate, Integer.parseInt(materialId), " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit));
			}else if(deptId.trim().isEmpty() || deptId.equals("-1")){
				roots = StockDetailReportDao.getStockDetailReportByDateCount(beginDate, endDate, Integer.parseInt(materialId), null);
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(beginDate, endDate, Integer.parseInt(materialId), " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit));
			}else if(!deptId.trim().isEmpty()){
				roots = StockDetailReportDao.getStockDetailReportByDateAndDeptCount(beginDate, endDate, Integer.parseInt(materialId), Integer.parseInt(deptId), null);
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDateAndDept (beginDate, endDate, Integer.parseInt(materialId), Integer.parseInt(deptId), " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit));
			}
			//stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(beginDate, endDate, Integer.parseInt(materialId), null);
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
