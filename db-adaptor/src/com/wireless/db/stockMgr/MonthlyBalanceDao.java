package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.pojo.util.DateUtil;

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
		String sql = "INSERT INTO " + Params.dbName + ".monthly_balance(restaurant_id, staff, month) VALUES (" +
					monthlyBalance.getRestaurantId() + ", " +
					"'" + monthlyBalance.getStaffName() + "', " + 
					"'" + DateUtil.format(monthlyBalance.getMonth()) + "')";
		
		int monthlyBalanceId;
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			monthlyBalanceId = dbCon.rs.getInt(1);
			for (MonthlyBalanceDetail detail : monthlyBalance.getDetails()) {
				detail.setMonthlyBalanceId(monthlyBalanceId);
				MonthlyBalanceDetailDao.insert(dbCon, detail);
			}
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
		int id;
		try{
			dbCon.conn.setAutoCommit(false);
			id = insert(dbCon, build.build());
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
		return id;
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
			String sql = "DELETE FROM " + Params.dbName + ".monthly_balance WHERE id = " + id;
			dbCon.stmt.executeUpdate(sql);
			
			sql = "DELETE FROM " + Params.dbName + ".monthly_balance_detail WHERE monthly_balance_id = " + id;
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
		String sql = "SELECT MB.id, MB.restaurant_id, MB.staff, MB.month, MBD.id as mbd_id, MBD.dept_id, MBD.dept_name, MBD.opening_balance, MBD.ending_balance FROM " + Params.dbName + ".monthly_balance MB " +
					" JOIN " + Params.dbName + ".monthly_balance_detail MBD ON MB.id = MBD.monthly_balance_id " +
					" WHERE 1=1 " +
					(extraCond == null ? "" : extraCond) +
					(otherClause == null ? "" : otherClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Map<MonthlyBalance, MonthlyBalance> result = new HashMap<MonthlyBalance, MonthlyBalance>();
		while(dbCon.rs.next()){
			MonthlyBalance mb = new MonthlyBalance();
			MonthlyBalanceDetail mbd = new MonthlyBalanceDetail();
			
			mbd.setId(dbCon.rs.getInt("mbd_id"));
			mbd.setDeptId(dbCon.rs.getInt("dept_id"));
			mbd.setDeptName(dbCon.rs.getString("dept_name"));
			mbd.setMonthlyBalanceId(dbCon.rs.getInt("id"));
			mbd.setOpeningBalance(dbCon.rs.getFloat("opening_balance"));
			mbd.setEndingBalance(dbCon.rs.getFloat("ending_balance"));
			
			mb.setId(dbCon.rs.getInt("id"));
			mb.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mb.setStaffName(dbCon.rs.getString("staff"));
			mb.setMonth(dbCon.rs.getDate("month").getTime());
			
			if(result.get(mb) == null){
				mb.addDetails(mbd);
				result.put(mb, mb);
			}else{
				result.get(mb).addDetails(mbd);
			}
		}
		
		return result.values().size() > 0 ? new ArrayList<MonthlyBalance>(result.values()) : new ArrayList<MonthlyBalance>();
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
			List<MonthlyBalance> list = getMonthlyBalance(" AND MB.id = " + id, null);
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
