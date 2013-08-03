package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.orderMgr.OrderFoodDao;
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
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<CostAnalyze> getCostAnalyzes(DBCon dbCon, Staff term, String begin, String end, String orderClause) throws SQLException, BusinessException{
		List<CostAnalyze> costAnalyzes = new ArrayList<CostAnalyze>();
		List<MaterialDept> materialDepts;
		List<Department> departments = DepartmentDao.getDepartments(dbCon, term, null, null);
		String extra;
		String extraCond = " AND S.ori_stock_date >= '" + begin + "' AND S.ori_stock_date <= '" + end + "'";
		String orderFoodHistory = " AND order_date >= '" + begin + "' AND  order_date <= '" + end + "'"; 
		for (Department dept : departments) {
			CostAnalyze costAnalyze = new CostAnalyze();
			costAnalyze.setDeptId(dept.getId());
			costAnalyze.setDeptName(dept.getName());
			extra = " AND S.dept_out = " + dept.getId() + " AND S.sub_type <> " + SubType.LESS.getVal();
			costAnalyze.setUseMaterialMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			extra = " AND S.dept_out = " + dept.getId() + " AND S.sub_type = " + SubType.STOCK_OUT.getVal();
			costAnalyze.setStockOutMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			extra = " AND S.dept_out = " + dept.getId() + " AND S.sub_type = " + SubType.STOCK_OUT_TRANSFER.getVal();
			costAnalyze.setStockOutTransferMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			extra = " AND S.dept_in = " + dept.getId() + " AND S.sub_type = " + SubType.STOCK_IN.getVal();
			costAnalyze.setCostMoney(getMoney(dbCon, term, extraCond + extra, orderClause));
			String orderFoodHistoryExtra = orderFoodHistory + " AND dept_id = " + dept.getId() ;
			costAnalyze.setSalesMoney(OrderFoodDao.getSalesMoney(dbCon, term, orderFoodHistoryExtra));
			
			materialDepts = MaterialDeptDao.getMaterialDepts(dbCon, term, " AND MD.dept_id = " + dept.getId(), null);
			if(materialDepts.isEmpty()){
				costAnalyze.setPrimeMoney(0);
				costAnalyze.setEndMoney(0);
			}else{
				float primeMoney = 0, endMoney = 0;
				for (MaterialDept materialDept : materialDepts) {
					String primeAmount = "SELECT S.sub_type, D.remaining, (D.dept_in_remaining * D.price) as dept_in_money, (D.dept_out_remaining * D.price) as dept_out_money, D.price FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND (S.dept_in = " + dept.getId() + " OR S.dept_out = " + dept.getId() + ") " +
							" AND S.ori_stock_date < '" + begin + "' AND D.material_id = " + materialDept.getMaterialId() + 
							" AND S.status = " + Status.AUDIT.getVal() +
							" ORDER BY D.id DESC LIMIT 0,1";
					dbCon.rs = dbCon.stmt.executeQuery(primeAmount);
					if(dbCon.rs.next()){
						SubType actionSubType = SubType.valueOf(dbCon.rs.getInt("sub_type"));
						if(actionSubType == SubType.STOCK_IN || actionSubType == SubType.STOCK_IN_TRANSFER || actionSubType == SubType.MORE || actionSubType == SubType.SPILL){
							primeMoney += dbCon.rs.getFloat("dept_in_money");
						}else{
							primeMoney += dbCon.rs.getFloat("dept_out_money");
						}
					}
					dbCon.rs.close();
					
					String endAmount = "SELECT S.sub_type, D.remaining, (D.dept_in_remaining * D.price) as dept_in_money, (D.dept_out_remaining * D.price) as dept_out_money, D.price FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND (S.dept_in = " + dept.getId() + " OR S.dept_out = " + dept.getId() + ") " +
							" AND S.ori_stock_date <= '" + end + " 23:59:59' AND D.material_id = " + materialDept.getMaterialId() + 
							" AND S.status = " + Status.AUDIT.getVal() +
							" ORDER BY D.id DESC LIMIT 0,1";
					dbCon.rs = dbCon.stmt.executeQuery(endAmount);
					if(dbCon.rs.next()){
						SubType actionSubType = SubType.valueOf(dbCon.rs.getInt("sub_type"));
						if(actionSubType == SubType.STOCK_IN || actionSubType == SubType.STOCK_IN_TRANSFER || actionSubType == SubType.MORE || actionSubType == SubType.SPILL){
							endMoney += dbCon.rs.getFloat("dept_in_money");
						}else{
							endMoney += dbCon.rs.getFloat("dept_out_money");
						}
					}
					dbCon.rs.close();
					
					
				}
				costAnalyze.setPrimeMoney(primeMoney);
				costAnalyze.setEndMoney(endMoney);
	
				
			}
			
			
			
			
			costAnalyzes.add(costAnalyze);
		}
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
}
