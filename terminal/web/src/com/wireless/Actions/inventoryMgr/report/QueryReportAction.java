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
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockReport;

public class QueryReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			String beginDate = request.getParameter("beginDate");
			String endDate = "";
			String cateType = request.getParameter("cateType");
			String cateId = request.getParameter("cateId");
			String materialId = request.getParameter("materialId");
			String deptId = request.getParameter("deptId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<StockReport> stockReports = null ;
			List<StockReport> stockReportPage = new ArrayList<StockReport>() ;
			int roots = 0;
			String extra = "";
			extra += " AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.DELETE.getVal() + ") ";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if(beginDate == null || cateType == null){
					
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.MONTH, -1);
				stockReports = StockReportDao.getStockCollectByTime(staff, sdf.format(c.getTime()), sdf.format(new Date()), extra, null);
				
			}else{
				endDate = beginDate + "-31";
				beginDate += "-01";
				
				if(!materialId.equals("-1") && !materialId.trim().isEmpty()){
					extra += " AND M.material_id = " + materialId;
				}
				
				if(cateType != null && !cateType.isEmpty()){
					extra += " AND S.cate_type = " + cateType;
				}
				
				if(cateId != null && !cateId.isEmpty()){
					extra += " AND M.cate_id = " + cateId;
				}
				
				if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
					extra += " AND (S.dept_in = " + deptId +" OR S.dept_out = " + deptId + ")";
					stockReports = StockReportDao.getStockCollectByDept(staff, beginDate, endDate, extra, null, Integer.parseInt(deptId));
				}else{
					stockReports = StockReportDao.getStockCollectByTypes(staff, beginDate, endDate, extra, null);
				}
			}

			if(stockReports == null || stockReports.isEmpty()){
				roots = 0;
			}else{
				roots = stockReports.size();
				int plus = Integer.parseInt(start)+Integer.parseInt(limit);
				if(plus > roots){
					plus = roots;
				}
				stockReportPage = stockReports.subList(Integer.parseInt(start), plus);
				float tatalMoney = 0;
				for (StockReport stockReport : stockReports) {
					tatalMoney += stockReport.getFinalMoney();
				}
				StockReport totalStockReport = new StockReport();
				totalStockReport.setFinalMoney(tatalMoney);
				stockReportPage.add(totalStockReport);
			}
			jobject.setTotalProperty(roots);
			jobject.setRoot(stockReportPage);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), e.getCode(), e.getDesc());

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;

	}
	
}
