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

public class MaterialStockStatisticsAction extends Action{
	
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
			
			StockReport sum = new StockReport();
			for(StockReport stockReport : stockReports){
				//入库采购
				sum.setStockIn(sum.getStockIn() + stockReport.getStockIn());
				//入库采购金额
				sum.setStockInMoney(sum.getStockInMoney() + stockReport.getStockInMoney());
				//入库调拨
				sum.setStockInTransfer(sum.getStockInTransfer() + stockReport.getStockInTransfer());
				//入库报溢
				sum.setStockSpill(sum.getStockSpill() + stockReport.getStockSpill());
				//入库盘盈
				sum.setStockTakeMore(sum.getStockTakeMore() + stockReport.getStockTakeMore());
				//出库退货
				sum.setStockOut(sum.getStockOut() + stockReport.getStockOut());
				//出库退货金额
				sum.setStockOutMoney(sum.getStockOutMoney() + stockReport.getStockOutMoney());
				//出库调拨
				sum.setStockOutTransfer(sum.getStockOutTransfer() + stockReport.getStockOutTransfer());
				//出库报损
				sum.setStockDamage(sum.getStockDamage() + stockReport.getStockDamage());
				//出库盘亏
				sum.setStockTakeLess(sum.getStockTakeLess() + stockReport.getStockTakeLess());
				//出库消耗
				sum.setConsumption(sum.getExpectConsumption() + stockReport.getExpectConsumption());
				//销售金额
				sum.setComsumeMoney(sum.getComsumeMoney() + stockReport.getComsumeMoney());
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, start, limit);
			}
			
			if(result.size() > 0){
				result.add(sum);
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
