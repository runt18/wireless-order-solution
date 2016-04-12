package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.pojo.stockMgr.StockAction;
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
	 * @throws BusinessException 
	 * @throws ParseException 
	 */
	public static int insert(DBCon dbCon, MonthlyBalance monthlyBalance, Staff staff) throws SQLException, BusinessException, ParseException{
		
		Calendar c = Calendar.getInstance();
		String beginDate, endDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat firstDay = new SimpleDateFormat("yyyy-MM-01");
		//获取当前月
		long monthly = MonthlyBalanceDao.getCurrentMonthTimeByRestaurant(staff.getRestaurantId());
		c.setTime(firstDay.parse(firstDay.format(new Date(monthly))));
		
		beginDate = sdf.format(c.getTime());
		
		monthlyBalance.setMonth(c.getTime().getTime());
		//获取这个月中最后一天
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		//格式化期末时间
		endDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day + " 23:59:59";
		
		List<Department> depts = DepartmentDao.getByType(staff, Department.Type.NORMAL);
		
		float openingBalance, endingBalance;
		//获取每个部门的期初和期末余额
		for (Department dept : depts) {
			String endAmount = "SELECT MBD.ending_balance FROM " + Params.dbName + ".monthly_balance MB" +
					" JOIN " + Params.dbName + ".monthly_balance_detail MBD ON MB.id = MBD.monthly_balance_id " +
					" WHERE MB.restaurant_id = " + staff.getRestaurantId() + 
					" AND MB.month <= '" + beginDate + "'" +
					" AND MBD.dept_id = " + dept.getId() + 
					" ORDER BY MB.id DESC LIMIT 0,1";
			dbCon.rs = dbCon.stmt.executeQuery(endAmount);
			if(dbCon.rs.next()){
				openingBalance = dbCon.rs.getFloat("ending_balance");
			}else{
				openingBalance = 0;
			}
		
			dbCon.rs.close();
			endingBalance = CostAnalyzeReportDao.getBalance(endDate, dept.getId(), staff.getRestaurantId());
			monthlyBalance.addDetails(new MonthlyBalanceDetail.InsertBuilder(dept.getId(), openingBalance, endingBalance).setDeptName(dept.getName()).setRestaurantId(staff.getRestaurantId()).build());
		}
		
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
	 * @throws BusinessException 
	 * @throws ParseException 
	 */
	public static int insert(MonthlyBalance.InsertBuilder build, Staff staff) throws SQLException, BusinessException, ParseException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		int id;
		try{
			dbCon.conn.setAutoCommit(false);
			id = insert(dbCon, build.build(), staff);
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
	private static List<MonthlyBalance> getMonthlyBalance(DBCon dbCon, String extraCond, String otherClause) throws SQLException{
		String sql = "SELECT MB.id, MB.restaurant_id, MB.staff, MB.month, MBD.id as mbd_id, MBD.dept_id, MBD.dept_name, MBD.opening_balance, MBD.ending_balance FROM " + Params.dbName + ".monthly_balance MB " +
					" JOIN " + Params.dbName + ".monthly_balance_detail MBD ON MB.id = MBD.monthly_balance_id " +
					" WHERE 1=1 " +
					(extraCond == null ? "" : extraCond) +
					(otherClause == null ? "" : otherClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Map<MonthlyBalance, MonthlyBalance> result = new LinkedHashMap<MonthlyBalance, MonthlyBalance>();
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
	
	/**
	 * Get current by restaurant
	 * @param restaurant
	 * 			the restaurant id
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static MonthlyBalance getCurrentMonthByRestaurant(int restaurant) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getCurrentMonthByRestaurant(dbCon, restaurant);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static long getCurrentMonthTimeByRestaurant(int restaurant) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			MonthlyBalance m = getCurrentMonthByRestaurant(dbCon, restaurant);
			if(m.getId() > 0){
				return m.getMonth();
			}else{
				//Restaurant r = RestaurantDao.getById(dbCon, restaurant);
				Staff staff = new Staff();
				staff.setRestaurantId(restaurant);
				List<StockAction> list = StockActionDao.getByCond(dbCon, staff, new StockActionDao.ExtraCond().addExceptSubType(StockAction.SubType.CONSUMPTION), " ORDER BY S.birth_date LIMIT 0,1");
				if(list.isEmpty()){
					return new Date().getTime();
				}else{
					return list.get(0).getOriStockDate();
				}
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static MonthlyBalance getCurrentMonthByRestaurant(DBCon dbCon, int restaurant) throws SQLException{
		String sql = "SELECT id, MAX(date_add(month, interval 1 MONTH)) month FROM " + Params.dbName + ".monthly_balance " + 
						" WHERE restaurant_id = " + restaurant ;
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		MonthlyBalance m = new MonthlyBalance();
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("month") != null){
				m.setId(dbCon.rs.getInt("id"));
				m.setMonth(dbCon.rs.getTimestamp("month").getTime()) ;
			}else{
				m.setId(-1);
			};
		}
		return m;
	}
}
