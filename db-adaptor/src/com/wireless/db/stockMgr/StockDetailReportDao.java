package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.pojo.stockMgr.StockAction.SubType;

public class StockDetailReportDao {

	public static int getStockDetailReportCount(DBCon dbCon, Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
		String sql = "SELECT count(*)" + 
						" FROM " + Params.dbName + ".stock_action as S  INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +  
						" WHERE S.restaurant_id = " + term.getRestaurantId() +
						" AND D.material_id = " + materialId + " AND S.status = " + StockAction.Status.AUDIT.getVal() +
						(extraCond == null ? "" : extraCond) +
						(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
	
		int count = 0 ;
		if(dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		
		return count;
		
	}
	public static List<StockDetailReport> getStockDetailReport(DBCon dbCon, Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
		String sql = "SELECT S.id, S.ori_stock_date, S.ori_stock_id, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.sub_type, D.amount, D.price, D.remaining" + 
						" FROM " + Params.dbName + ".stock_action as S  INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +  
						" WHERE S.restaurant_id = " + term.getRestaurantId() +
						" AND D.material_id = " + materialId + " AND S.status = " + StockAction.Status.AUDIT.getVal() +
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
			stockDetailReport.setStockDetailPrice(dbCon.rs.getFloat("price"));
			stockDetailReport.setRemaining(dbCon.rs.getFloat("remaining"));
			stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
			
			stockDetailReports.add(stockDetailReport);
		}
		
		return stockDetailReports;
		
	}
	
	public static List<StockDetailReport> getStockDetailReportByDept(DBCon dbCon, Staff term, int materialId, String extraCond, String orderClause, int deptId) throws SQLException{
		String sql = "SELECT S.id, S.ori_stock_date, S.ori_stock_id, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.sub_type, D.amount, D.price, D.remaining, D.dept_in_remaining, D.dept_out_remaining" + 
						" FROM " + Params.dbName + ".stock_action as S  INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +  
						" WHERE S.restaurant_id = " + term.getRestaurantId() +
						" AND D.material_id = " + materialId + " AND S.status = " + StockAction.Status.AUDIT.getVal() +
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
			stockDetailReport.setStockDetailPrice(dbCon.rs.getFloat("price"));
			//stockDetailReport.setRemaining(dbCon.rs.getFloat("remaining"));
			
			if(dbCon.rs.getInt("dept_in") == deptId){
				if(dbCon.rs.getInt("sub_type") == SubType.STOCK_IN_TRANSFER.getVal() || dbCon.rs.getInt("sub_type") == SubType.STOCK_OUT_TRANSFER.getVal()){
					stockDetailReport.setStockActionSubType(SubType.STOCK_IN_TRANSFER);
				}else{
					stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
				}
				stockDetailReport.setRemaining(dbCon.rs.getFloat("dept_in_remaining"));
			}else if(dbCon.rs.getInt("dept_out") == deptId){
				if(dbCon.rs.getInt("sub_type") == SubType.STOCK_IN_TRANSFER.getVal() || dbCon.rs.getInt("sub_type") == SubType.STOCK_OUT_TRANSFER.getVal()){
					stockDetailReport.setStockActionSubType(SubType.STOCK_OUT_TRANSFER);
				}else{
					stockDetailReport.setStockActionSubType(dbCon.rs.getInt("sub_type"));
				}
				stockDetailReport.setRemaining(dbCon.rs.getFloat("dept_out_remaining"));
			}
			
			stockDetailReports.add(stockDetailReport);
		}
		
		return stockDetailReports;
		
	}
	
	public static List<StockDetailReport> getStockDetailReport(Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, term, materialId, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
		
	}
	
	public static List<StockDetailReport> getStockDetailReportByDept(Staff term, int materialId, String extraCond, String orderClause, int deptId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReportByDept(dbCon, term, materialId, extraCond, orderClause, deptId);
		}finally{
			dbCon.disconnect();
		}
		
		
	}
	
	public static int getStockDetailReportCount(Staff term, int materialId, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReportCount(dbCon, term, materialId, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static int getStockDetailReportByDateAndDeptCount(Staff term, String begin, String end, int materialId, int deptId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReportCount(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'" + " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	public static List<StockDetailReport> getStockDetailReportByDateAndDept(Staff term, String begin, String end, int materialId, int deptId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'" + " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	public static int getStockDetailReportByDateCount(Staff term, String begin, String end, int materialId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReportCount(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "' ", orderClause);
		}finally{
			dbCon.disconnect();
			
		}
	}
	
	public static List<StockDetailReport> getStockDetailReportByDate(Staff term, String begin, String end, int materialId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, term, materialId, " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "' ", orderClause);
		}finally{
			dbCon.disconnect();
			
		}
	}
	public static int getStockDetailReportByDeptCount(Staff term, int materialId, int deptId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReportCount(dbCon, term, materialId, " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);

		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<StockDetailReport> getStockDetailReportByDept(Staff term, int materialId, int deptId, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockDetailReport(dbCon, term, materialId, " AND (S.dept_in =" + deptId + " OR S.dept_out =" + deptId + ")", orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
}
