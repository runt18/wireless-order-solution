package com.wireless.Actions.inventoryMgr.report;

import java.sql.SQLException;
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

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.util.DataPaging;

public class QueryReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("beginDate");
		final String materialId = request.getParameter("materialId");
		final String deptId = request.getParameter("deptId");
		final String cateId = request.getParameter("cateId");
		final String cateType = request.getParameter("cateType");
		final JObject jObject = new JObject();
		try{

			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final StockReportDao.ExtraCond extraCond = new StockReportDao.ExtraCond();
			
			if(beginDate != null){
				extraCond.setRange(beginDate);
			}else{
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.MONTH, -1);
				extraCond.setRange(new SimpleDateFormat("yyyy-MM").format(c.getTime()));
			}
			
			if(materialId != null && !materialId.equals("-1") && !materialId.trim().isEmpty()){
				extraCond.setMaterial(Integer.parseInt(materialId));
			}
			
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDept(Integer.parseInt(deptId));
			}
			
			if(cateId != null && !cateId.isEmpty() && Integer.parseInt(cateId) >= 0){
				extraCond.setMaterialCate(Integer.parseInt(cateId));
			}
			
			if(cateType != null && !cateType.isEmpty() && Integer.parseInt(cateType) >= 0){
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			List<StockReport> result = StockReportDao.getByCond(staff, extraCond);

			StockReport summary = new StockReport();
			for(StockReport report : result){
				summary.setFinalAmount(summary.getFinalAmount() + report.getFinalAmount());
				summary.setFinalMoney(summary.getFinalMoney() + report.getFinalMoney());
				summary.setPrimeAmount(summary.getPrimeAmount() + report.getPrimeAmount());
				summary.setPrimeMoney(summary.getPrimeMoney() + report.getPrimeMoney());
			}
			
			jObject.setTotalProperty(result.size());
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, Integer.parseInt(start), Integer.parseInt(limit));
			}
			
			result.add(summary);
			
			jObject.setRoot(result);
			
//			List<StockReport> stockReports = null ;
//			List<StockReport> stockReportPage = new ArrayList<StockReport>() ;
//			int roots = 0;
//			String extra = "";
//			extra += " AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") ";
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			if(beginDate == null || cateType == null){
//					
//				Calendar c = Calendar.getInstance();
//				c.setTime(new Date());
//				c.add(Calendar.MONTH, -1);
//				stockReports = StockReportDao.getStockCollectByTime(staff, sdf.format(c.getTime()), sdf.format(new Date()), extra, null);
//				
//			}else{
//				endDate = beginDate + "-31";
//				beginDate += "-01";
//				
//				if(!materialId.equals("-1") && !materialId.trim().isEmpty()){
//					extra += " AND M.material_id = " + materialId;
//				}
//				
//				if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
//					extra += " AND (S.dept_in = " + deptId +" OR S.dept_out = " + deptId + ")";
//					stockReports = StockReportDao.getStockCollectByDept(staff, beginDate, endDate, extra, null, Integer.parseInt(deptId));
//				}else{
//					stockReports = StockReportDao.getStockCollectByTypes(staff, beginDate, endDate, extra, null);
//				}
//			}
//
//			if(stockReports == null || stockReports.isEmpty()){
//				roots = 0;
//			}else{
//				roots = stockReports.size();
//				int plus = Integer.parseInt(start)+Integer.parseInt(limit);
//				if(plus > roots){
//					plus = roots;
//				}
//				stockReportPage = stockReports.subList(Integer.parseInt(start), plus);
//				float tatalMoney = 0;
//				for (StockReport stockReport : stockReports) {
//					tatalMoney += stockReport.getFinalMoney();
//				}
//				StockReport totalStockReport = new StockReport();
//				totalStockReport.setFinalMoney(tatalMoney);
//				stockReportPage.add(totalStockReport);
//			}
//			jObject.setTotalProperty(roots);
//			jObject.setRoot(stockReportPage);
			
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
