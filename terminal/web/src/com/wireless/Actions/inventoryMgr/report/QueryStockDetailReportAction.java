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
import com.wireless.util.DataPaging;
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
		try{
			
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String materialId = request.getParameter("materialId");
			String deptId = request.getParameter("deptId");
			
/*			String deptId = "";
			String materialId = "3";*/
/*			String endDate = "2013-09-09";
			String beginDate = "2012-01-09";*/
			//String materialId = "3";
			if(materialId == null){
				stockDetailReports = new ArrayList<StockDetailReport>();
			}else if(deptId == null){
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(beginDate, endDate, Integer.parseInt(materialId), null);
			}else if(deptId.trim().isEmpty() || deptId.equals("-1")){
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(beginDate, endDate, Integer.parseInt(materialId), null);
			}else if(!deptId.trim().isEmpty()){
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDateAndDept (beginDate, endDate, Integer.parseInt(materialId), Integer.parseInt(deptId), null);
			}
			//stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(beginDate, endDate, Integer.parseInt(materialId), null);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(stockDetailReports != null){
				jobject.setTotalProperty(stockDetailReports.size());
				jobject.setRoot(DataPaging.getPagingData(stockDetailReports, "true", start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}
