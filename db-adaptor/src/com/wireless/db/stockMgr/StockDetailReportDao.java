package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.stockMgr.StockDetailReport;

public class StockDetailReportDao {

	public static List<StockDetailReport> getStockDetailReport(DBCon dbCon, int materialId, String extraCond, String orderClause) throws SQLException{
		String sql = "SELECT S.id, S.ori_stock_date, S.ori_stock_id, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.sub_type, D.amount, D.remaining" + 
						" FROM " + Params.dbName + ".stock_action as S  INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +  
						" WHERE D.material_id = " + materialId +
						(extraCond == null ? "" : extraCond) +
						(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<StockDetailReport> stockDetailReports = new ArrayList<StockDetailReport>();
		while(dbCon.rs.next()){
			StockDetailReport stockDetailReport = new StockDetailReport();
			stockDetailReport.setId(dbCon.rs.getInt("id"));
			stockDetailReport.setDate(dbCon.rs.getTimestamp("ori_stock_date").getTime());
			stockDetailReport.setOriStockId(dbCon.rs.getString("ori_stock_id"));
			stockDetailReport.setDeptIn(dbCon.rs.getString("dept_in_name"));
			stockDetailReport.setDeptOut(dbCon.rs.getString("dept_out_name"));
			stockDetailReport.setStockActionAmount(dbCon.rs.getFloat("amount"));
			stockDetailReport.setRemaining(dbCon.rs.getFloat("remaining"));
			stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
			
			stockDetailReports.add(stockDetailReport);
		}
		
		return stockDetailReports;
		
	}
	public static List<StockDetailReport> getStockDetailReportByDateAndDept(String begin, String end, int materialId, int deptId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'" + " AND S.dept_in =" + deptId + " OR S.dept_out =" + deptId + " AND S.status = 2", orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	public static List<StockDetailReport> getStockDetailReportByDate(String begin, String end, int materialId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'" + " AND S.status = 2", orderClause);
		}finally{
			dbCon.disconnect();
			
		}
	}
	public static List<StockDetailReport> getStockDetailReportByDept(int materialId, int deptId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, materialId, " AND S.dept_in =" + deptId + " OR S.dept_out =" + deptId + " AND S.status = 2", orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
}
