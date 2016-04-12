package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.pojo.stockMgr.StockInGeneral;

public class StockDetailReportDao {

	
	/**
	 * Get the the stock detail report.
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<StockDetailReport> getByCond(Staff staff, StockActionDetailDao.ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the the stock detail report.
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<StockDetailReport> getByCond(DBCon dbCon, Staff staff, StockActionDetailDao.ExtraCond extraCond) throws SQLException, BusinessException{
		final List<StockDetailReport> result = new ArrayList<>();
		List<StockActionDetail> details = StockActionDetailDao.getByCond(dbCon, staff, extraCond, " ORDER BY S.ori_stock_date ");
		for(StockActionDetail stockDetail : details){
			StockDetailReport detailReport = new StockDetailReport();
			detailReport.setStockActonDetail(stockDetail);
			result.add(detailReport);
		}
		
		for(StockDetailReport detailReport : result){
			detailReport.setStockAction(StockActionDao.getById(dbCon, staff, detailReport.getStockActionDetail().getStockActionId()));
		}		
		return result;
	}
//	
//	public static int getStockDetailReportCount(DBCon dbCon, Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
//		String sql = "SELECT count(*)" + 
//						" FROM " + Params.dbName + ".stock_action as S  " +
//						"INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +
//						"INNER JOIN " + Params.dbName + ".material as M ON M.material_id = D.material_id " +
//						"INNER JOIN " + Params.dbName + ".material_cate as MC ON MC.cate_id = M.cate_id " +  
//						" WHERE S.restaurant_id = " + term.getRestaurantId() +
//						(materialId != -1 ? " AND D.material_id = " + materialId : "")+ 
//						" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
//						
//						(extraCond == null ? "" : extraCond) +
//						(orderClause == null ? "" : orderClause);
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//	
//		int count = 0 ;
//		if(dbCon.rs.next()){
//			count = dbCon.rs.getInt(1);
//		}
//		
//		return count;
//		
//	}
//	public static List<StockDetailReport> getStockDetailReport(DBCon dbCon, Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
//		String sql = "SELECT S.id, S.ori_stock_date, S.ori_stock_id, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.sub_type, S.approver,S.supplier_name, D.name, D.amount, D.price, D.remaining" + 
//						" FROM " + Params.dbName + ".stock_action as S  " +
//						"INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +  
//						"INNER JOIN " + Params.dbName + ".material as M ON M.material_id = D.material_id " +
//						"INNER JOIN " + Params.dbName + ".material_cate as MC ON MC.cate_id = M.cate_id " +  
//						" WHERE S.restaurant_id = " + term.getRestaurantId() +
//						(materialId != -1 ? " AND D.material_id = " + materialId : "")+  
//						" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
//						(extraCond == null ? "" : extraCond) +
//						(orderClause == null ? "" : orderClause);
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		
//		List<StockDetailReport> stockDetailReports = new ArrayList<StockDetailReport>();
//		while(dbCon.rs.next()){
//			StockDetailReport stockDetailReport = new StockDetailReport();
//			stockDetailReport.setId(dbCon.rs.getInt("id"));
//			stockDetailReport.setDate(dbCon.rs.getTimestamp("ori_stock_date").getTime());
//			stockDetailReport.setOriStockId(dbCon.rs.getString("ori_stock_id"));
//			stockDetailReport.setMaterialName(dbCon.rs.getString("name"));
//			stockDetailReport.setSupplier(dbCon.rs.getString("supplier_name"));
//			stockDetailReport.setDeptIn(dbCon.rs.getString("dept_in_name"));
//			stockDetailReport.setDeptOut(dbCon.rs.getString("dept_out_name"));
//			stockDetailReport.setStockActionAmount(dbCon.rs.getFloat("amount"));
//			stockDetailReport.setStockDetailPrice(dbCon.rs.getFloat("price"));
//			stockDetailReport.setRemaining(dbCon.rs.getFloat("remaining"));
//			stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
//			stockDetailReport.setOperater(dbCon.rs.getString("approver"));
//			stockDetailReport.setTotalMoney(dbCon.rs.getFloat("amount") * dbCon.rs.getFloat("price"));
//			
//			stockDetailReports.add(stockDetailReport);
//		}
//		
//		return stockDetailReports;
//		
//	}
//	
//	public static List<StockDetailReport> getStockDetailReportByDept(DBCon dbCon, Staff term, int materialId, String extraCond, String orderClause, int deptId) throws SQLException{
//		String sql = "SELECT S.id, S.ori_stock_date, S.ori_stock_id, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.sub_type, S.approver,S.supplier_name, D.name, D.amount, D.price, D.remaining, D.dept_in_remaining, D.dept_out_remaining" + 
//						" FROM " + Params.dbName + ".stock_action as S  " +
//						"INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +
//						"INNER JOIN " + Params.dbName + ".material as M ON M.material_id = D.material_id " +
//						"INNER JOIN " + Params.dbName + ".material_cate as MC ON MC.cate_id = M.cate_id " +  
//						" WHERE S.restaurant_id = " + term.getRestaurantId() +
//						(materialId != -1 ? " AND D.material_id = " + materialId : "")+  
//						" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
//						(extraCond == null ? "" : extraCond) +
//						(orderClause == null ? "" : orderClause);
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		
//		List<StockDetailReport> stockDetailReports = new ArrayList<StockDetailReport>();
//		while(dbCon.rs.next()){
//			StockDetailReport stockDetailReport = new StockDetailReport();
//			stockDetailReport.setId(dbCon.rs.getInt("id"));
//			stockDetailReport.setDate(dbCon.rs.getTimestamp("ori_stock_date").getTime());
//			stockDetailReport.setOriStockId(dbCon.rs.getString("ori_stock_id"));
//			stockDetailReport.setMaterialName(dbCon.rs.getString("name"));
//			stockDetailReport.setSupplier(dbCon.rs.getString("supplier_name"));
//			stockDetailReport.setDeptIn(dbCon.rs.getString("dept_in_name"));
//			stockDetailReport.setDeptOut(dbCon.rs.getString("dept_out_name"));
//			stockDetailReport.setStockActionAmount(dbCon.rs.getFloat("amount"));
//			stockDetailReport.setStockDetailPrice(dbCon.rs.getFloat("price"));
//			stockDetailReport.setOperater(dbCon.rs.getString("approver"));
//			stockDetailReport.setTotalMoney(dbCon.rs.getFloat("amount") * dbCon.rs.getFloat("price"));
//			
//			if(dbCon.rs.getInt("dept_in") == deptId){
//				if(dbCon.rs.getInt("sub_type") == SubType.STOCK_IN_TRANSFER.getVal() || dbCon.rs.getInt("sub_type") == SubType.STOCK_OUT_TRANSFER.getVal()){
//					stockDetailReport.setStockActionSubType(SubType.STOCK_IN_TRANSFER);
//				}else{
//					stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
//				}
//				stockDetailReport.setRemaining(dbCon.rs.getFloat("dept_in_remaining"));
//			}else if(dbCon.rs.getInt("dept_out") == deptId){
//				if(dbCon.rs.getInt("sub_type") == SubType.STOCK_IN_TRANSFER.getVal() || dbCon.rs.getInt("sub_type") == SubType.STOCK_OUT_TRANSFER.getVal()){
//					stockDetailReport.setStockActionSubType(SubType.STOCK_OUT_TRANSFER);
//				}else{
//					stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
//				}
//				stockDetailReport.setRemaining(dbCon.rs.getFloat("dept_out_remaining"));
//			}
//			
//			stockDetailReports.add(stockDetailReport);
//		}
//		
//		return stockDetailReports;
//		
//	}
	
//	public static List<StockDetailReport> getStockDetailReport(Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReport(dbCon, term, materialId, extraCond, orderClause);
//		}finally{
//			dbCon.disconnect();
//		}
//		
//		
//	}
//	
//	public static List<StockDetailReport> getStockDetailReportByDept(Staff term, int materialId, String extraCond, String orderClause, int deptId) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReportByDept(dbCon, term, materialId, extraCond, orderClause, deptId);
//		}finally{
//			dbCon.disconnect();
//		}
//		
//		
//	}
//	
//	public static int getStockDetailReportCount(Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReportCount(dbCon, term, materialId, extraCond, orderClause);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	public static int getStockDetailReportByDateAndDeptCount(Staff term, String begin, String end, int materialId, int deptId, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReportCount(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'" + " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	public static List<StockDetailReport> getStockDetailReportByDateAndDept(Staff term, String begin, String end, int materialId, int deptId, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReport(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'" + " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	public static int getStockDetailReportByDateCount(Staff term, String begin, String end, int materialId, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReportCount(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "' ", orderClause);
//		}finally{
//			dbCon.disconnect();
//			
//		}
//	}
//	
//	public static List<StockDetailReport> getStockDetailReportByDate(Staff term, String begin, String end, int materialId, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReport(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "' ", orderClause);
//		}finally{
//			dbCon.disconnect();
//			
//		}
//	}
//	public static int getStockDetailReportByDeptCount(Staff term, int materialId, int deptId, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReportCount(dbCon, term, materialId, " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
//
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	public static List<StockDetailReport> getStockDetailReportByDept(Staff term, int materialId, int deptId, String orderClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getStockDetailReport(dbCon, term, materialId, " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	
	public static class ExtraCond{
		
		private DutyRange range;
		private int operateStaff ;
		private String name;
		private int deptId;
		private int cateType;
		private int cateId;
		private int suppler;
		
		
		public DutyRange getRange() {
			return range;
		}


		public void setRange(DutyRange range) {
			this.range = range;
		}


		public int getOperateStaff() {
			return operateStaff;
		}


		public void setOperateStaff(int operateStaff) {
			this.operateStaff = operateStaff;
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}


		public int getDeptId() {
			return deptId;
		}


		public void setDeptId(int deptId) {
			this.deptId = deptId;
		}


		public int getCateType() {
			return cateType;
		}


		public void setCateType(int cateType) {
			this.cateType = cateType;
		}


		public int getCateId() {
			return cateId;
		}


		public void setCateId(int cateId) {
			this.cateId = cateId;
		}


		public int getSuppler() {
			return suppler;
		}


		public void setSuppler(int suppler) {
			this.suppler = suppler;
		}


		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(range != null){
				extraCond.append(" AND S.ori_stock_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
			}
			if(operateStaff > 0){
				extraCond.append(" AND S.operator_id = " + operateStaff);
			}
			if(name != null){
				extraCond.append(" AND M.name LIKE '%" + name + "%'");
			}
			if(deptId > 0){
				extraCond.append(" AND S.dept_in = " + deptId);
			}
			if(cateType > 0){
				extraCond.append(" AND MC.type = " + cateType);
			}
			if(cateId > 0){
				extraCond.append(" AND M.cate_id = " + cateId);
			}
			if(suppler > 0){
				extraCond.append(" AND S.supplier_id = " + suppler);
			}
			
			return extraCond.toString();
		}
	}	
	
	/**
	 * Get the stock_in general information by extra.
	 * @param staff
	 * @param extra
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<StockInGeneral> getStockInGeneral(Staff staff, ExtraCond extra) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockInGeneral(dbCon, staff, extra);
		}finally{
			dbCon.disconnect();
		}
	}	

	public static List<StockInGeneral> getStockInGeneral(DBCon dbCon, Staff staff, ExtraCond extra) throws SQLException, BusinessException{
		List<StockInGeneral> list = new ArrayList<>();
		String sql = "SELECT ROUND(SUM(S.actual_price), 2) AS totalMoney, M.price AS referencePrice, ROUND(AVG(SD.price), 2) AS avgPrice,SD.material_id, SD.name, SUM(SD.amount) AS totalAmount, MAX(SD.price) AS maxPrice, min(SD.price) AS minPrice "
					+ " FROM stock_action_detail SD "
					+ " JOIN stock_action  S ON SD.stock_action_id = S.id "
					+ " JOIN material M ON M.material_id = SD.material_id "
					+ " JOIN material_cate MC ON M.cate_id = MC.cate_id "
					+ " WHERE S.restaurant_id = " + staff.getRestaurantId() + " AND S.sub_type = " + StockAction.SubType.STOCK_IN.getVal()
					+ (extra != null ? extra : " ") 
					+ " GROUP BY SD.material_id";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockInGeneral sg = new StockInGeneral();
			sg.setAvgPrice(dbCon.rs.getFloat("avgPrice"));
			sg.setCount(dbCon.rs.getFloat("totalAmount"));
			sg.setMaxPrice(dbCon.rs.getFloat("maxPrice"));
			sg.setMinPrice(dbCon.rs.getFloat("minPrice"));
			sg.setName(dbCon.rs.getString("name"));
			sg.setReferencePrice(dbCon.rs.getFloat("referencePrice"));
			sg.setTotalMoney(dbCon.rs.getFloat("totalMoney"));
			
			list.add(sg);
		}
		
		dbCon.rs.close();
		
		return list;
	}	
	
	
}
