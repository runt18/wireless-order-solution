package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.combo.ComboIncome;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class CalcComboStatisticsDao {
	
	public static class ExtraCond {

		private final CalcBillStatisticsDao.ExtraCond extraCond4BillStatistics;
		private Department.DeptId deptId;
		
		public ExtraCond(DateType dateType) {
			extraCond4BillStatistics = new CalcBillStatisticsDao.ExtraCond(dateType);
		}

		public ExtraCond setSubDept(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setCalcByDuty(boolean onOff){
			this.extraCond4BillStatistics.setCalcByDuty(onOff);
			return this;
		}
		
		public ExtraCond setComboDept(Department.DeptId deptId){
			this.extraCond4BillStatistics.setDept(deptId);
			return this;
		}
		
		public ExtraCond setDutyRange(DutyRange range){
			this.extraCond4BillStatistics.setDutyRange(range);
			return this;
		}
		
		public ExtraCond setHourRange(HourRange range){
			this.extraCond4BillStatistics.setHourRange(range);
			return this;
		}	
		
		public ExtraCond setFoodName(String name){
			this.extraCond4BillStatistics.setFoodName(name);
			return this;
		}
		
		public CalcBillStatisticsDao.ExtraCond getE(){
			return this.extraCond4BillStatistics;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			
			if(deptId != null){
				extraCond.append(" AND K.dept_id = " + deptId.getVal());
			}
			
			return extraCond.toString();
		}
	}
	
	public static List<ComboIncome> calcCombo(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCombo(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<ComboIncome> calcCombo(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		
		sql = " SELECT " + 
			  " K.dept_id AS dept_id, TMP.food_name AS combo_name, IFNULL(F.name, '已删除子菜') AS sub_food_name, (TMP.food_amount * C.amount) AS amount, (TMP.food_amount * C.amount * F.price) AS total_price "	+
			  " FROM (" +	
			  	CalcBillStatisticsDao.makeSql4CalcFood(staff, extraCond.extraCond4BillStatistics) +
			  ") AS TMP " +
			  " LEFT JOIN " + Params.dbName + ".combo C" + " ON C.food_id = TMP.food_id " +
			  " LEFT JOIN " + Params.dbName + ".food F" + " ON C.sub_food_id = F.food_id " + 
			  " LEFT JOIN " + Params.dbName + ".kitchen K" + " ON F.kitchen_id = K.kitchen_id " +
			  " WHERE (TMP.food_status & " + Food.COMBO + ") <> 0 " +
			  extraCond.toString();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);		
		
		List<ComboIncome> comboIncomes = new ArrayList<ComboIncome>();
		
		while(dbCon.rs.next()){
			ComboIncome comboIncome = new ComboIncome();
			comboIncome.setAmount(dbCon.rs.getFloat("amount"));
			comboIncome.setTotalPrice(dbCon.rs.getFloat("total_price"));
			comboIncome.setComboFoodName(dbCon.rs.getString("combo_name"));
			comboIncome.setSubFoodName(dbCon.rs.getString("sub_food_name"));
			comboIncomes.add(comboIncome);	
		}
		dbCon.rs.close();
		
		return comboIncomes;
		
	}
	
}
