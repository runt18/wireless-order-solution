package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.StockError;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;

public class MonthlyBalanceDetailDao {

	/**
	 * Insert a new MonthlyBalanceDetail.
	 * @param dbCon
	 * @param detail
	 * @return
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, MonthlyBalanceDetail detail) throws SQLException{
		String sql = "INSERT INTO " + Params.dbName + ".monthly_balance_detail(" + 
					 "monthly_balance_id, restaurant_id, dept_id, dept_name, opening_balance, ending_balance) " +
					 "VALUES( " +
					 detail.getMonthlyBalanceId() + ", " +
					 detail.getRestaurantId() + ", " +
					 detail.getDeptId() + ", " +
					 "'" + detail.getDeptName() + "', " +
					 detail.getOpeningBalance() + ", " +
					 detail.getEndingBalance() + ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException(StockError.MONTHLY_BALANCE_ADD.toString());
		}
		return id;
		
	}
	
	/**
	 * Insert a new MonthlyBalanceDetail.
	 * @param builder
	 * 			the detail of MonthlyBalanceDetail
	 * @return
	 * 			the id of MonthlyBalanceDetail just create
	 * @throws SQLException
	 */
	public static int insert(MonthlyBalanceDetail.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return insert(dbCon, builder.build());
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of MonthlyBalanceDetail according to condition.
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * 			the list of MonthlyBalanceDetail
	 * @throws SQLException
	 */
	public static List<MonthlyBalanceDetail> getMonthlyBalanceDetail(String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getMonthlyBalanceDetail(dbCon, extraCond, otherClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of MonthlyBalanceDetail according to condition.
	 * @param dbCon
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	public static List<MonthlyBalanceDetail> getMonthlyBalanceDetail(DBCon dbCon, String extraCond, String otherClause) throws SQLException{
		String sql = "SELECT * FROM " + Params.dbName + ".monthly_balance_detail " +
						" WHERE 1=1 " +
						(extraCond == null ? "" : extraCond) +
						(otherClause == null ? "" : otherClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<MonthlyBalanceDetail> list = new ArrayList<MonthlyBalanceDetail>();
		while(dbCon.rs.next()){
			MonthlyBalanceDetail detail = new MonthlyBalanceDetail();
			detail.setId(dbCon.rs.getInt("id"));
			detail.setMonthlyBalanceId(dbCon.rs.getInt("monthly_balance_id"));
			detail.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			detail.setDeptId(dbCon.rs.getInt("dept_id"));
			detail.setDeptName(dbCon.rs.getString("dept_name"));
			detail.setOpeningBalance(dbCon.rs.getFloat("opening_balance"));
			detail.setEndingBalance(dbCon.rs.getFloat("ending_balance"));
			
			list.add(detail);
		}
		return list;
	}
	
	
	
}
