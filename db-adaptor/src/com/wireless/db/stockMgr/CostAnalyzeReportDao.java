package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.CostAnalyze;
import com.wireless.pojo.stockMgr.StockReport;

public class CostAnalyzeReportDao {
	public static class ExtraCond{
		private String beginDate;
		private String endDate;
		private int deptId;
		
		public ExtraCond setDateRange(String begin, String end){
			if(begin != null && !begin.isEmpty()){
				this.beginDate = begin;
			}
			if(end != null && !end.isEmpty()){
				this.endDate = end;
			}
			return this;
		}
		
		public ExtraCond setDeptId(int deptId){
			this.deptId = deptId;
			return this;
		}
		
		
		public String toString() {
			final StringBuilder extraCond = new StringBuilder();
			if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				extraCond.append(" AND ori_stock_date BETWEEN '" + beginDate + "' AND " + endDate);
			}else if(beginDate != null && !beginDate.isEmpty() && endDate == null && endDate.isEmpty()){
				extraCond.append(" AND ori_stock_date > " + beginDate);
			}else if(beginDate == null && beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				extraCond.append(" AND ori_stock_date < " + beginDate);
			}
			return extraCond.toString();
		}
	}
	
	
	/**
	 * get the costAnalyze by extraCond
	 * @param dbCon
	 * @param staff
	 * @param begin
	 * @param end
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static List<CostAnalyze> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException, Exception{
		List<CostAnalyze> result = new ArrayList<>();
		DepartmentDao.ExtraCond departExtraCond = new DepartmentDao.ExtraCond();
		List<Department> departments;
		if(extraCond.deptId != 0){
			departExtraCond.setId(extraCond.deptId);
			departments = DepartmentDao.getByCond(staff, departExtraCond, orderClause);
		}else{
			departments = DepartmentDao.getDepartments4Inventory(staff);
		}

		for(Department department : departments){
			CostAnalyze costAnalyze = new CostAnalyze();
			List<StockReport> stockReports = StockReportDao.getByCond(staff, new StockReportDao.ExtraCond().setDept(department.getId())
																										  .setRange(extraCond.beginDate, extraCond.endDate));
				float primeMoney = 0;				//期初金额
				float pickMaterialMoney = 0;		//采购金额
				float stockOutMoney = 0;			//退货金额
				float stockInTransferMoney = 0;		//领料金额
				float stockOutTransferMoney = 0;	//退料金额
				float endMoney = 0;					//期末金额
				float stockSpillMoney = 0;			//其他入库额
				float stockDamageMoney = 0;			//其他出库额
				float stockTakeMoreMoney = 0;		//盘盈金额
				float stockTakeLessMoney = 0;		//盘亏金额	
				float costMoney = 0;				//成本金额
				float salesMoney = 0;				//销售金额
				float profit = 0;					//毛利额
				float profitRate;					//毛利率
				
				for(StockReport stockReport : stockReports){
					
					primeMoney += stockReport.getPrimeMoney();													//期初金额
					pickMaterialMoney += stockReport.getStockIn() * stockReport.getFinalPrice();				//采购金额
					stockOutMoney += stockReport.getStockOut() * stockReport.getFinalPrice();					//退货金额
					stockInTransferMoney += stockReport.getStockInTransfer() * stockReport.getFinalPrice();		//领料金额
					stockOutTransferMoney += stockReport.getStockOutTransfer() * stockReport.getFinalPrice();	//退料金额
					endMoney += stockReport.getFinalMoney();													//期末金额
					stockSpillMoney += stockReport.getStockSpill() * stockReport.getFinalPrice();				//其他入库额
					stockDamageMoney += stockReport.getStockDamage() * stockReport.getFinalPrice();				//其他出库额
					stockTakeMoreMoney += stockReport.getStockTakeMore() * stockReport.getFinalPrice();			//盘盈金额
					stockTakeLessMoney += stockReport.getStockTakeLess() * stockReport.getFinalPrice();			//盘亏金额
					salesMoney += stockReport.getComsumeMoney();	//销售金额
				}
				
				//成本金额 = 期初金额 + 采购金额 + 领料金额  - 退货金额 - 退料金额 - 期末金额
				/**
				 * 报损盘亏报溢盘盈 算为   有成本  0收入类型
				 */
				costMoney += primeMoney + pickMaterialMoney + stockInTransferMoney - stockOutMoney - stockOutTransferMoney - endMoney;//成本金额
				
				profit += salesMoney - costMoney;															//毛利金额
				profitRate = profit / salesMoney;															//毛利率
				
				//部门id
				costAnalyze.setDeptId(department.getId());
				//部门名字
				costAnalyze.setDeptName(department.getName());
				//期初金额
				costAnalyze.setPrimeMoney(primeMoney);
				//期末金额
				costAnalyze.setEndMoney(endMoney);
				//采购金额
				costAnalyze.setPickMaterialMoney(pickMaterialMoney);
				//退货金额
				costAnalyze.setStockOutMoney(stockOutMoney);
				//领料金额
				costAnalyze.setStockInTransferMoney(stockInTransferMoney);
				//退料金额
				costAnalyze.setStockOutTransferMoney(stockOutTransferMoney);
				//其他入库额
				costAnalyze.setStockSpillMoney(stockSpillMoney);
				//其他出库额
				costAnalyze.setStockDamageMoney(stockDamageMoney);
				//盘盈金额
				costAnalyze.setStockTakeMoreMoney(stockTakeMoreMoney);
				//盘亏金额
				costAnalyze.setStockTakeLessMoney(stockTakeLessMoney);
				//成本金额
				costAnalyze.setCostMoney(costMoney);
				//销售金额
				costAnalyze.setSalesMoney(salesMoney);
				//毛利额
				costAnalyze.setProfit(profit);
				//毛利金额
				costAnalyze.setProfitRate(profitRate);
			
			result.add(costAnalyze);
		}
		return result;
	}
	
	/**
	 * get the costAnalyze by extraCond
	 * @param term
	 * @param begin
	 * @param end
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<CostAnalyze> getByCond(Staff term, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException, Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	
}
