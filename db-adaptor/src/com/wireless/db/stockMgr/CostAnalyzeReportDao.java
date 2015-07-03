package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.CostAnalyze;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.util.DateUtil;

public class CostAnalyzeReportDao {

	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param begin
	 * @param end 
	 * 				already format 23:59:59
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<CostAnalyze> getCostAnalyzes(DBCon dbCon, Staff staff, String begin, String end, String orderClause) throws SQLException, BusinessException{
		List<CostAnalyze> costAnalyzes = new ArrayList<CostAnalyze>();
		List<MaterialDept> materialDepts;
		List<Department> departments = DepartmentDao.getByType(dbCon, staff, Department.Type.NORMAL);
		String extra;
		String extraCond = " AND S.ori_stock_date BETWEEN '" + begin + "' AND '" + end + "'";
		
		//判断这个月是否有月结
		List<MonthlyBalance> thisMb = MonthlyBalanceDao.getMonthlyBalance(" AND MB.restaurant_id = " + staff.getRestaurantId() + " AND MB.month = '" + begin + "' ", null);
		
		long beginL = DateUtil.parseDate(begin);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(beginL);
		c.add(Calendar.MONTH, -1);
		//判断上个月是否有月结
		List<MonthlyBalance> lastMb = MonthlyBalanceDao.getMonthlyBalance(" AND MB.restaurant_id = " + staff.getRestaurantId() + " AND MB.month = '" + DateUtil.format(c.getTimeInMillis(), DateUtil.Pattern.DATE) + "'", null);
		for (Department dept : departments) {
			CostAnalyze costAnalyze = new CostAnalyze();
			costAnalyze.setDeptId(dept.getId());
			costAnalyze.setDeptName(dept.getName());
			
			materialDepts = MaterialDeptDao.getMaterialDepts(dbCon, staff, " AND MD.dept_id = " + dept.getId(), null);
			if(thisMb.isEmpty()){
				float endMoney = 0;
				//期末金额为materialDept剩余物料总和 * 参考成本
				for (MaterialDept md : materialDepts) {
					Material m = MaterialDao.getById(md.getMaterialId());
					endMoney += md.getStock() * m.getPrice();
				}
				costAnalyze.setEndMoney(endMoney);
				
				costAnalyze.setPrimeMoney(0);
				if(!lastMb.isEmpty()){
					for (MonthlyBalanceDetail mbd : lastMb.get(0).getDetails()) {
						if(mbd.getDeptId() == dept.getId()){
							//上个月的月末金额作为这个月的月初金额
							costAnalyze.setPrimeMoney(mbd.getEndingBalance());
							break;
						}
					}
				}
			}else{
				float primeMoney = 0, endMoney = 0;
				String primeAmount = "SELECT MBD.opening_balance FROM " + Params.dbName + ".monthly_balance MB" +
										" JOIN " + Params.dbName + ".monthly_balance_detail MBD ON MB.id = MBD.monthly_balance_id " +
										" WHERE MB.restaurant_id = " + staff.getRestaurantId() + 
										" AND MB.month <= '" + begin + "'" +
										" AND MBD.dept_id = " + dept.getId() + 
										" ORDER BY MB.id DESC LIMIT 0,1";
				dbCon.rs = dbCon.stmt.executeQuery(primeAmount);
				if(dbCon.rs.next()){
					primeMoney = dbCon.rs.getFloat("opening_balance");
				}
				
				dbCon.rs.close();
				
				String endAmount = "SELECT MBD.ending_balance FROM " + Params.dbName + ".monthly_balance MB" +
						" JOIN " + Params.dbName + ".monthly_balance_detail MBD ON MB.id = MBD.monthly_balance_id " +
						" WHERE MB.restaurant_id = " + staff.getRestaurantId() + 
						" AND MB.month <= '" + end + "'" +
						" AND MBD.dept_id = " + dept.getId() + 
						" ORDER BY MB.id DESC LIMIT 0,1";
				dbCon.rs = dbCon.stmt.executeQuery(endAmount);
				if(dbCon.rs.next()){
					endMoney = dbCon.rs.getFloat("ending_balance");
				}
			
				dbCon.rs.close();
				costAnalyze.setPrimeMoney(primeMoney);
				costAnalyze.setEndMoney(endMoney);
			}
			
			//获取领料金额
			extra = " AND S.dept_in = " + dept.getId() + " AND S.sub_type = " + SubType.STOCK_IN.getVal();
			costAnalyze.setPickMaterialMoney(getMoney(dbCon, staff, extraCond + extra, orderClause));
			//拨入金额
			extra = " AND S.dept_in = " + dept.getId() + " AND (S.sub_type = " + SubType.STOCK_OUT_TRANSFER.getVal() + " OR S.sub_type = " + SubType.STOCK_IN_TRANSFER.getVal() + ") ";
			costAnalyze.setStockInTransferMoney(getMoney(dbCon, staff, extraCond + extra, orderClause));
			//退料金额
			extra = " AND S.dept_out = " + dept.getId() + " AND S.sub_type = " + SubType.STOCK_OUT.getVal();
			costAnalyze.setStockOutMoney(getMoney(dbCon, staff, extraCond + extra, orderClause));
			//拨出金额
			extra = " AND S.dept_out = " + dept.getId() + " AND (S.sub_type = " + SubType.STOCK_OUT_TRANSFER.getVal() + " OR S.sub_type = " + SubType.STOCK_IN_TRANSFER.getVal() + ") ";
			costAnalyze.setStockOutTransferMoney(getMoney(dbCon, staff, extraCond + extra, orderClause));
			//成本金额
			costAnalyze.setCostMoney(costAnalyze.getPrimeMoney() + costAnalyze.getPickMaterialMoney() + costAnalyze.getStockInTransferMoney() - costAnalyze.getStockOutMoney() - costAnalyze.getStockOutTransferMoney() - costAnalyze.getEndMoney());
			
			costAnalyzes.add(costAnalyze);
		}
		//获取销售额
		String sql = "SELECT dept_id, SUM(unit_price * order_count) as money FROM " + Params.dbName + ".order_food_history " +
				 	" WHERE restaurant_id = " + staff.getRestaurantId() + 
				 	" AND order_date >= '" + begin + "' AND  order_date <= '" + end + "'" +
				 	" GROUP BY dept_id";
		DBCon cost = new DBCon();
		cost.connect();
		cost.rs = cost.stmt.executeQuery(sql);
		while(cost.rs.next()){
			int index = costAnalyzes.indexOf(new CostAnalyze(cost.rs.getInt("dept_id")));
			if(index >= 0){
				costAnalyzes.get(index).setSalesMoney(cost.rs.getInt("money"));
			}
		}
		cost.disconnect();
		
		return costAnalyzes;
	}
	/**
	 * 
	 * @param term
	 * @param begin
	 * @param end
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<CostAnalyze> getCostAnalyzes(Staff term, String begin, String end, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCostAnalyzes(dbCon, term, begin, end, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param term
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static float getMoney(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMoney(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static float getMoney(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql = "SELECT SUM(D.price * D.amount) AS money FROM " + Params.dbName + ".stock_action_detail as D " +
		" JOIN " + Params.dbName + ".stock_action as S ON S.id = D.stock_action_id "+
		" WHERE S.restaurant_id = " + term.getRestaurantId() +
		" AND S.status IN ("+ StockAction.Status.AUDIT.getVal() +", "+ StockAction.Status.DELETE.getVal() +") " +
		(extraCond == null ? "" : extraCond) +
		(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		float money = 0;
		while(dbCon.rs.next()){
			money += dbCon.rs.getFloat("money");
		}
		return money;
		
	}
	
	/**
	 * Get the balance according to format data.
	 * @param data
	 * 				yyyy-mm-dd hh:mm:ss
	 * @param deptId
	 * 				the balance of which department
	 * @return
	 * @throws SQLException
	 */
	public static float getBalance(String data, int deptId, int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getBalance(dbCon, data, deptId, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static float getBalance(DBCon dbCon, String data, int deptId, int restaurantId) throws SQLException{
		float endMoney = 0;
		//直接用stock * material的参考单价
		String endAmount = "SELECT SUM(ROUND(MD.stock * M.price, 2)) AS endMoney FROM " + Params.dbName + ".material_dept MD "
				+ " JOIN " + Params.dbName + ".material M ON M.material_id = MD.material_id "
				+ " WHERE MD.restaurant_id = " + restaurantId 
				+ " AND MD.dept_id = " + deptId;
		
		dbCon.rs = dbCon.stmt.executeQuery(endAmount);
		
		if(dbCon.rs.next()){
			endMoney = dbCon.rs.getFloat("endMoney");
		}
		return endMoney;
	}
}
