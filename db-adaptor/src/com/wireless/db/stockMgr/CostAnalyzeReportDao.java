package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.CostAnalyze;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;

public class CostAnalyzeReportDao {

	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param begin
	 * @param end 
	 * 				already format 23:59:59
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<CostAnalyze> getCostAnalyzes(DBCon dbCon, Staff term, String begin, String end, String orderClause) throws SQLException, BusinessException{
		List<CostAnalyze> costAnalyzes = new ArrayList<CostAnalyze>();
		List<MaterialDept> materialDepts;
		List<Department> departments = DepartmentDao.getNormalDepartments(dbCon, term);
		String extra;
		String extraCond = " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'";
		
		for (Department dept : departments) {
			CostAnalyze costAnalyze = new CostAnalyze();
			costAnalyze.setDeptId(dept.getId());
			costAnalyze.setDeptName(dept.getName());
			
			materialDepts = MaterialDeptDao.getMaterialDepts(dbCon, term, " AND MD.dept_id = " + dept.getId(), null);
			if(materialDepts.isEmpty()){
				costAnalyze.setPrimeMoney(0);
				costAnalyze.setEndMoney(0);
			}else{
				float primeMoney = 0, endMoney = 0;
				String primeAmount = "SELECT MBD.opening_balance FROM " + Params.dbName + ".monthly_balance MB" +
										" JOIN " + Params.dbName + ".monthly_balance_detail MBD ON MB.id = MBD.monthly_balance_id " +
										" WHERE MB.restaurant_id = " + term.getRestaurantId() + 
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
						" WHERE MB.restaurant_id = " + term.getRestaurantId() + 
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
			extra = " AND S.dept_in = " + dept.getId() + " AND (S.sub_type = " + SubType.STOCK_IN.getVal() + " OR S.sub_type = " + SubType.STOCK_OUT_TRANSFER.getVal() + " OR S.sub_type = " + SubType.STOCK_IN_TRANSFER.getVal() + ")";
			costAnalyze.setPickMaterialMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			//退料金额
			extra = " AND S.dept_out = " + dept.getId() + " AND S.sub_type = " + SubType.STOCK_OUT.getVal();
			costAnalyze.setStockOutMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			//拨出金额
			extra = " AND S.dept_out = " + dept.getId() + " AND (S.sub_type = " + SubType.STOCK_OUT_TRANSFER.getVal() + " OR S.sub_type = " + SubType.STOCK_IN_TRANSFER.getVal() + ") ";
			costAnalyze.setStockOutTransferMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			//成本金额
			costAnalyze.setCostMoney(costAnalyze.getPrimeMoney() + costAnalyze.getPickMaterialMoney() - costAnalyze.getStockOutMoney() - costAnalyze.getStockOutTransferMoney() - costAnalyze.getEndMoney());
			
			costAnalyzes.add(costAnalyze);
		}
		//获取销售额
		String sql = "SELECT dept_id, SUM(unit_price * order_count) as money FROM " + Params.dbName + ".order_food_history " +
				 	" WHERE restaurant_id = " + term.getRestaurantId() + 
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
		String sql = "SELECT S.price as money" + 
				" FROM " + Params.dbName + ".stock_action as S  INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +  
				" WHERE S.restaurant_id = " + term.getRestaurantId() +
				" AND S.status = " + StockAction.Status.AUDIT.getVal() +
				(extraCond == null ? "" : extraCond) +
				" GROUP BY S.id " +
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
		String dept_material = "SELECT material_id, dept_id FROM " + Params.dbName + ".material_dept " +
								" WHERE restaurant_id = " + restaurantId +
								" AND dept_id = " + deptId;
		dbCon.rs = dbCon.stmt.executeQuery(dept_material);
		while(dbCon.rs.next()){
			String endAmount = "SELECT S.sub_type, S.dept_in, S.dept_out, D.remaining, ROUND(D.dept_in_remaining * M.price, 2) as dept_in_money, ROUND(D.dept_out_remaining * M.price, 2) as dept_out_money, D.price FROM " + Params.dbName + ".stock_action as S " + 
					" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +
					" JOIN " + Params.dbName + ".material M ON M.material_id = D.material_id " +
					" WHERE 1 = 1 " +
					" AND (S.dept_in = " + deptId + " OR S.dept_out = " + deptId + ") " +
					" AND S.ori_stock_date <= '" + data + "'" + 
					" AND S.status = " + Status.AUDIT.getVal() +
					" AND D.material_id = " + dbCon.rs.getInt("material_id") +
					" ORDER BY D.id DESC LIMIT 0,1";
			DBCon balance = new DBCon();
			balance.connect();
			balance.rs = balance.stmt.executeQuery(endAmount);
			
			if(balance.rs.next()){
				SubType actionSubType = SubType.valueOf(balance.rs.getInt("sub_type"));
				if(actionSubType == SubType.STOCK_IN || actionSubType == SubType.MORE || actionSubType == SubType.SPILL){
					endMoney += balance.rs.getFloat("dept_in_money");
				}else if(actionSubType == SubType.STOCK_IN_TRANSFER || actionSubType == SubType.STOCK_OUT_TRANSFER){
					if(deptId == balance.rs.getInt("dept_in")){
						endMoney += balance.rs.getFloat("dept_in_money");
					}else{
						endMoney += balance.rs.getFloat("dept_out_money");
					}
					
				}else{
					endMoney += balance.rs.getFloat("dept_out_money");
				}
			}
			balance.disconnect();
		}
		return endMoney;
	}
}
