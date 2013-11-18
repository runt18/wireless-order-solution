package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.stockMgr.MonthlyBalance;

public class MonthlyBalanceDao {

	/**
	 * Insert a new monthlyBalance.
	 * @param dbCon
	 * @param monthlyBalance
	 * 			the monthlyBalance's detail
	 * @return	
	 * 			the id of monthlyBalance
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, MonthlyBalance monthlyBalance) throws SQLException{
		String sql = "INSERT INTO" + Params.dbName + ".monthly_balance(restaurant_id, staff, month) VALUES (" +
					monthlyBalance.getRestaurantId() + 
					monthlyBalance.getStaffName() +
					monthlyBalance.getMonth();
		
		int monthlyBalanceId;
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			monthlyBalanceId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id is not generated successfully.");
		}
		
		return monthlyBalanceId;
	}
	
	/**
	 * Insert a new monthlyBalance.
	 * @param build
	 * @return
	 * 			the id of monthlyBalance
	 * @throws SQLException
	 */
	public static int insert(MonthlyBalance.InsertBuilder build) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return insert(dbCon, build.build());
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete monthlyBalance by Id.
	 * @param id
	 * 			the id of monthlyBalance
	 * @throws SQLException
	 */
	public static void delete(int id) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			String sql = "DELETE " + Params.dbName + ".monthly_balance WHERE id = " + id;
			dbCon.stmt.executeUpdate(sql);
		}catch(SQLException e){
			throw new SQLException("failed to delete");
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of MonthlyBalance according to extra condition.
	 * @param extraCond
	 * 			the extra condition
	 * @param otherClause
	 * @return	list of MonthlyBalance
	 * @throws SQLException
	 */
	public static List<MonthlyBalance> getMonthlyBalance(String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getMonthlyBalance(dbCon, extraCond, otherClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of MonthlyBalance according to extra condition.
	 * @param dbCon
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	public static List<MonthlyBalance> getMonthlyBalance(DBCon dbCon, String extraCond, String otherClause) throws SQLException{
		String sql = "SELECT * FROM " + Params.dbName + 
					" WHERE 1=1 " +
					(extraCond == null ? "" : extraCond) +
					(otherClause == null ? "" : otherClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<MonthlyBalance> list = new ArrayList<MonthlyBalance>(); 
		
		while(dbCon.rs.next()){
			MonthlyBalance mb = new MonthlyBalance();
			mb.setId(dbCon.rs.getInt("id"));
			mb.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mb.setStaffName(dbCon.rs.getString("staff"));
			mb.setMonth(dbCon.rs.getDate("month").getTime());
			
			list.add(mb);
		}
		
		return list;
	}
	
	/**
	 * Get MonthlyBalance according to id.
	 * @param id
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static MonthlyBalance getMonthlyBalanceById(int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			List<MonthlyBalance> list = getMonthlyBalance(" AND id = " + id, null);
			if(!list.isEmpty()){
				return list.get(0);
			}else{
				throw new BusinessException(StockError.MONTHLY_BALANCE_NOT_EXIST);
			}
		}finally{
			dbCon.disconnect();
		}
	}
}
