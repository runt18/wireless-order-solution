package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.pojo.stockMgr.StockInGeneral;

public class StockDetailReportDao {
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
	 * Get the the stock detail report.
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<StockDetailReport> getByCond(Staff staff, StockActionDetailDao.ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
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
		return getByCond(dbCon, staff, extraCond, null);
	}
	
	
	/**
	 * Get the the stock detail report.
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<StockDetailReport> getByCond(DBCon dbCon, Staff staff, StockActionDetailDao.ExtraCond extraCond,String orderClause) throws SQLException, BusinessException{
		final List<StockDetailReport> result = new ArrayList<>();
		List<StockActionDetail> details = StockActionDetailDao.getByCond(dbCon, staff, extraCond, (orderClause != null ? " ORDER BY D.id ASC " + orderClause : ""));
		for(StockActionDetail stockDetail : details){
			StockDetailReport detailReport = new StockDetailReport();
			detailReport.setStockActonDetail(stockDetail);
			result.add(detailReport);
		}
		
		if(!extraCond.isOnlyAmount()){
			for(StockDetailReport detailReport : result){
				detailReport.setStockAction(StockActionDao.getById(dbCon, staff, detailReport.getStockActionDetail().getStockActionId()));
			}		
		}
		
		return result;
	}
	
	public static StockDetailReport getSumByCond(Staff staff, StockActionDetailDao.ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getSumByCond(dbCon, staff, extraCond);
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 */
	public static StockDetailReport getSumByCond(DBCon dbCon,Staff staff, StockActionDetailDao.ExtraCond extraCond) throws SQLException{
		String sql = " SELECT " + 
					 " 0 " +
					 ", SUM(IF(S.type = " + StockAction.Type.STOCK_IN.getVal() + ", SD.amount, 0)) AS stock_in_amount " +
					 ", SUM(ROUND(IF(S.type = " + StockAction.Type.STOCK_IN.getVal() + ", SD.price * SD.amount, 0), 2)) AS stock_in_price " +
					 ", SUM(IF(S.type = " + StockAction.Type.STOCK_OUT.getVal() + ", SD.amount, 0)) AS stock_out_amount " +
					 ", SUM(ROUND(IF(S.type = " + StockAction.Type.STOCK_OUT.getVal() + ", SD.price * SD.amount, 0), 2)) AS stock_out_price " +
					 " FROM " + Params.dbName + ".stock_action_detail SD" + 
					 " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
					 " WHERE 1 = 1 " + 
					 " AND S.restaurant_id = " + staff.getRestaurantId() +
					 (extraCond != null ? extraCond.toString() : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		StockDetailReport stockDetailReport = new StockDetailReport();
		while(dbCon.rs.next()){
			stockDetailReport.setSummary(true);
			stockDetailReport.setTotalStockInAmount(dbCon.rs.getInt("stock_in_amount"));
			stockDetailReport.setTotalStockInMoney(dbCon.rs.getFloat("stock_in_price"));
			stockDetailReport.setTotalStockOutAmount(dbCon.rs.getInt("stock_out_amount"));
			stockDetailReport.setTotalStockOutMoney(dbCon.rs.getFloat("stock_out_price"));
		}
		return stockDetailReport;
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
