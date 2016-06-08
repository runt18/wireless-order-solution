package com.wireless.Actions.inventoryMgr.materialStock;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.util.DataPaging;

public class MaterialStockStatistics extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("dateBegin");
		final String endDate = request.getParameter("dateEnd");
		final String materialId = request.getParameter("materialId");
		final String cateId = request.getParameter("cateId");
		final String cateType = request.getParameter("cateType");
		final String supplier = request.getParameter("supplierId");
		final JObject jObject = new JObject();
		final StockReportDao.ExtraCond extraCond = new StockReportDao.ExtraCond();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				extraCond.setRange(beginDate, endDate);
			}
			
			if(cateType != null && !cateType.isEmpty() && Integer.parseInt(cateType) > 0){
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(cateId != null && !cateId.isEmpty() && Integer.parseInt(cateId) > 0){
				extraCond.setMaterialCate(Integer.parseInt(cateId));
			}
			
			if(materialId != null && !materialId.isEmpty()){
				extraCond.setMaterial(Integer.parseInt(materialId));
			}
			
			if(supplier != null && !supplier.isEmpty() && Integer.parseInt(supplier) > 0){
				extraCond.setSupplier(Integer.parseInt(supplier));
			}
			
			List<StockReport> stockReports = StockReportDao.getRangeStockByCond(staff, extraCond);
			
			List<StockReport> result = new ArrayList<>();
			for(StockReport stockReport : stockReports){
				if(stockReport.getStockIn() > 0 || stockReport.getStockOut() > 0){
					result.add(stockReport);
				}
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, start, limit);
			}
			
			jObject.setRoot(result);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject);
		}
		return null;
	}
}
