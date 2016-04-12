package com.wireless.Actions.inventoryMgr.report;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.db.stockMgr.StockDetailReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.util.DataPaging;

public class QueryStockDetailReportAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jObject = new JObject();
		//String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String beginDate = request.getParameter("beginDate");
			String materialId = request.getParameter("materialId");
			String materialCateId = request.getParameter("materialCateId");
			String cateType = request.getParameter("cateType");
			String deptId = request.getParameter("deptId");
			String supplier = request.getParameter("supplier");
			//String stockType = request.getParameter("stockType");
			String subType = request.getParameter("subType");
			
			final StockActionDetailDao.ExtraCond extraCond = new StockActionDetailDao.ExtraCond();
			
			extraCond.setOriDate(beginDate + "-01", beginDate + "-31");
			
			//String extra = " AND S.ori_stock_date BETWEEN '" + beginDate + "' AND '" + endDate + " 23:59:59'";
			
			if(materialId != null && !materialId.isEmpty()){
				//materialId = "-1";
				extraCond.setMaterial(Integer.parseInt(materialId));
			}
			
			if(materialCateId != null && !materialCateId.isEmpty()){
				//extra += " AND MC.cate_id = " + materialCateId;
				extraCond.setMaterialCate(Integer.parseInt(materialCateId));
			}
			
			if(cateType != null && !cateType.isEmpty() && !cateType.equals("-1")){
				//extra += " AND MC.type = " + cateType;
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(subType != null && !subType.isEmpty()){
				//extra += " AND S.sub_type = " + subType;
				extraCond.addSubType(StockAction.SubType.valueOf(Integer.parseInt(subType)));
			}
				
			if(supplier != null && !supplier.isEmpty() && !supplier.equals("-1")){
				//extra += " AND S.supplier_id = " + supplier;
				extraCond.setSupplier(Integer.parseInt(supplier));
			}
			
			//FIXME
//			if(deptId.equals("-1")){
//				stockDetailReports = StockDetailReportDao.getStockDetailReport(staff, Integer.parseInt(materialId), extra, " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit));
//			}else{
//				extra += " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")";
//				stockDetailReports = StockDetailReportDao.getStockDetailReportByDept(staff, Integer.parseInt(materialId), extra, " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit), Integer.parseInt(deptId));
//			}
			
			List<StockDetailReport> result = StockDetailReportDao.getByCond(staff, extraCond);
			jObject.setTotalProperty(result.size());

			StockDetailReport summary = new StockDetailReport();
			StockActionDetail summary4ActionDetail = new StockActionDetail();
			StockAction summary4StockAction = new StockAction(0);
			for (StockDetailReport s : result) {
				summary4ActionDetail.setRemaining(s.getStockActionDetail().getRemaining() + summary4ActionDetail.getRemaining());
				summary4ActionDetail.setAmount(s.getStockActionDetail().getAmount() + summary4ActionDetail.getAmount());
			}
			summary.setStockAction(summary4StockAction);
			summary.setStockActonDetail(summary4ActionDetail);
			//FIXME
			//summary.setTotalMoney(0);
				

			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, Integer.parseInt(start), Integer.parseInt(limit));
			}
			
			result.add(summary);
			
			jObject.setRoot(result);
			
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
