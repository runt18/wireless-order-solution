package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond;
import com.wireless.pojo.billStatistics.combo.ComboIncome;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;

public class CalcComboStatisticsDao {
	
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
			  " TMP.food_name AS combo_name, IFNULL(F.name, '已删除子菜') AS sub_food_name, (TMP.food_amount * C.amount) AS amount, (TMP.food_amount * C.amount * F.price) AS total_price "	+
			  " FROM (" +	
			  	CalcBillStatisticsDao.makeSql4CalcFood(staff, extraCond) +
			  ") AS TMP " +
			  " LEFT JOIN " + Params.dbName + ".combo C" + " ON C.food_id = TMP.food_id " +
			  " LEFT JOIN " + Params.dbName + ".food F" + " ON C.sub_food_id = F.food_id " + 
			  " WHERE (TMP.food_status & " + Food.COMBO + ") <> 0 ";
		
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
