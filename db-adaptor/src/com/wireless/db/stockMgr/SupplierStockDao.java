package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.inventoryMgr.MaterialCate.Type;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.stockMgr.SupplierStock;
import com.wireless.pojo.supplierMgr.Supplier;

public class SupplierStockDao {
	public static class ExtraCond{
		private String beginDate;
		private String endDate;
		private Integer supplierId;
		private MaterialCate.Type cateType;
		private int cateId;
		
		public ExtraCond setRange(String begin, String end) throws ParseException{
			this.beginDate = begin;
			this.endDate = end;
			return this;
		}
		
		public ExtraCond setSupplierId(int id){
			this.supplierId = id;
			return this;
		}
		
		public ExtraCond setCateType(Type type){
			this.cateType = type;
			return this;
		}
		
		public ExtraCond setCate(int cateId){
			this.cateId = cateId;
			return this;
		}

	}
	
	
	
	public static List<SupplierStock> getByCond(Staff staff, ExtraCond extraCond)throws SQLException, BusinessException, Exception{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		} finally {
			dbCon.disconnect();
		}
	}
	
	
	
	/**
	 * get suppliserStock by extraCond
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static List<SupplierStock> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond)throws SQLException, BusinessException, Exception{
		
		final StockReportDao.ExtraCond extraCond4StockReport = new StockReportDao.ExtraCond(); 
		final StockActionDao.ExtraCond extraCond4StockAction = new StockActionDao.ExtraCond();
		
		if(extraCond.beginDate != null && !extraCond.beginDate.isEmpty() && extraCond.endDate != null && !extraCond.endDate.isEmpty()){
			extraCond4StockReport.setRange(extraCond.beginDate, extraCond.endDate);
			extraCond4StockAction.setOriDate(extraCond.beginDate, extraCond.endDate);
		}
		
		if(extraCond.cateType != null){
			extraCond4StockReport.setMaterialCateType(extraCond.cateType);
			extraCond4StockAction.setMaterialCateType(extraCond.cateType);
		}
		
		if(extraCond.cateId != 0){
			extraCond4StockReport.setMaterialCate(extraCond.cateId);
			extraCond4StockAction.setCateId(extraCond.cateId);
		}
		
		final List<Supplier> suppliers = new ArrayList<>();
		if(extraCond.supplierId == null){
			suppliers.addAll(SupplierDao.getByCond(dbCon, staff, null, null));
		
		}else{
			suppliers.add(SupplierDao.getById(staff, extraCond.supplierId));
		}
		
		final List<SupplierStock> supplierStocks = new ArrayList<>();
		for(Supplier supplier : suppliers){
			//按供应商寻找汇总表
			final List<StockReport> stockReports = StockReportDao.getRangeStockByCond(dbCon, staff, extraCond4StockReport.setSupplier(supplier));
			final SupplierStock supplierStock = new SupplierStock();
			supplierStock.setSupplier(supplier);
			//累加获取供应商的采购金额同退货金额
			for(StockReport stockReport : stockReports){
				supplierStock.setStockInActualMoney(supplierStock.getStockInActualMoney() + stockReport.getStockInMoney());
				supplierStock.setStockOutActualMoney(supplierStock.getStockOutActualMoney() + stockReport.getStockOutMoney());
				final Material material = MaterialDao.getById(dbCon, staff, stockReport.getMaterial().getId()); 
				supplierStock.setStockInMoney(supplierStock.getStockInMoney() + (stockReport.getStockInAmount() * material.getPrice()));
				supplierStock.setStockOutMoney(supplierStock.getStockOutMoney() + (stockReport.getStockOutAmount() * material.getPrice()));
			}
			
			final List<StockAction> stockActionStockIns = StockActionDao.getByCond(dbCon, staff, extraCond4StockAction.setSupplier(supplier).setType(StockAction.Type.STOCK_IN), null);
			supplierStock.setStockInAmount(stockActionStockIns.size());
			final List<StockAction> stockActionStockOuts = StockActionDao.getByCond(dbCon, staff, extraCond4StockAction.setSupplier(supplier).setType(StockAction.Type.STOCK_OUT), null);
			supplierStock.setStockOutAmount(stockActionStockOuts.size());
			
			supplierStock.setTotalMoney(supplierStock.getStockInActualMoney() - supplierStock.getStockOutActualMoney());
			supplierStocks.add(supplierStock);
		}
		
		
		return supplierStocks;
	}

}
