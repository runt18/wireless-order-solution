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
			String endDate = "";
			String materialId = request.getParameter("materialId");
			String materialCateId = request.getParameter("materialCateId");
			String cateType = request.getParameter("cateType");
			String deptId = request.getParameter("deptId");
			String supplier = request.getParameter("supplier");
			//String stockType = request.getParameter("stockType");
			String subType = request.getParameter("subType");
			
			endDate = beginDate + "-31";
			beginDate += "-01";
			
			String extra = " AND S.ori_stock_date BETWEEN '" + beginDate + "' AND '" + endDate + " 23:59:59'";
			
			if(materialId == null || materialId.isEmpty()){
				materialId = "-1";
			}
			
			if(materialCateId != null && !materialCateId.isEmpty()){
				extra += " AND MC.cate_id = " + materialCateId;
			}
			
			if(cateType != null && !cateType.isEmpty() && !cateType.equals("-1")){
				extra += " AND MC.type = " + cateType;
			}
			
			if(subType != null && !subType.isEmpty()){
				extra += " AND S.sub_type = " + subType;
			}
				
			if(supplier != null && !supplier.isEmpty() && !supplier.equals("-1")){
				extra += " AND S.supplier_id = " + supplier;
			}
			
			if(deptId.equals("-1")){
				stockDetailReports = StockDetailReportDao.getStockDetailReport(staff, Integer.parseInt(materialId), extra, " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit));
			}else{
				extra += " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")";
				stockDetailReports = StockDetailReportDao.getStockDetailReportByDept(staff, Integer.parseInt(materialId), extra, " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit), Integer.parseInt(deptId));
			}
			
			if(!stockDetailReports.isEmpty()){
				StockDetailReport sum = new StockDetailReport();
				float totalMoney = 0, stockActionCount = 0, remaining = 0;
				for (StockDetailReport s : stockDetailReports) {
					totalMoney += s.totalMoney();
					stockActionCount += s.getStockActionAmount();
					remaining += s.getRemaining();
				}
				sum.setStockActionAmount(stockActionCount);
				sum.setTotalMoney(totalMoney);
				sum.setRemaining(remaining);
				
				sum.setStockActionSubType(stockDetailReports.get(0).getStockActionSubType());
				
				stockDetailReports.add(sum);				
			}

			
			roots = StockDetailReportDao.getStockDetailReportCount(staff, Integer.parseInt(materialId), extra, null);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
