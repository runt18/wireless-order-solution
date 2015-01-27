package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.staffMgr.Staff;

public class FoodUnitDao {

	public static class ExtraCond {
		private int id;
		private final List<Integer> foods = new ArrayList<Integer>();

		public ExtraCond setId(int id) {
			this.id = id;
			return this;
		}

		public ExtraCond addFood(int foodId) {
			foods.add(foodId);
			return this;
		}

		public ExtraCond addFood(Food food) {
			foods.add(food.getFoodId());
			return this;
		}

		@Override
		public String toString() {
			StringBuilder extraCond = new StringBuilder();
			if (id != 0) {
				extraCond.append(" AND id = " + id);
			}
			StringBuilder foodCond = new StringBuilder();
			for (Integer foodId : foods) {
				if (foodCond.length() == 0) {
					foodCond.append(foodId);
				} else {
					foodCond.append(",").append(foodId);
				}
			}
			if (foodCond.length() != 0) {
				extraCond.append(" AND food_id IN ( " + foodCond.toString()	+ ")");
			}

			return extraCond.toString();
		}
	}

	public static int insert(DBCon dbCon, Staff staff, FoodUnit.InsertBuilder builder) throws SQLException {
		FoodUnit foodUnit = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".food_unit " +
			  "(food_id, price, unit) VALUES( " + 
			  foodUnit.getFoodId() + "," + 
			  foodUnit.getPrice() + "," + 
			  "'" + foodUnit.getUnit() + "'" + 
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int unitId = 0;
		if (dbCon.rs.next()) {
			unitId = dbCon.rs.getInt(1);
		} else {
			throw new SQLException("The food unit id is NOT generated successfully.");
		}
		dbCon.rs.close();
		return unitId;
	}

	
	public static List<FoodUnit> getByCond(Staff staff, ExtraCond extraCond) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<FoodUnit> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException {
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".food_unit WHERE 1 = 1 "	+
			   (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<FoodUnit> result = new ArrayList<FoodUnit>();
		while (dbCon.rs.next()) {
			FoodUnit foodUnit = new FoodUnit(dbCon.rs.getInt("id"));
			foodUnit.setFoodId(dbCon.rs.getInt("food_id"));
			foodUnit.setPrice(dbCon.rs.getFloat("price"));
			foodUnit.setUnit(dbCon.rs.getString("unit"));
			result.add(foodUnit);
		}
		dbCon.rs.close();

		return result;
	}

	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException {
		int amount = 0;
		for (FoodUnit foodUnit : getByCond(dbCon, staff, extraCond)) {
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".food_unit WHERE id = " + foodUnit.getId();
			if (dbCon.stmt.executeUpdate(sql) != 0) {
				amount++;
			}
		}
		return amount;
	}
}
