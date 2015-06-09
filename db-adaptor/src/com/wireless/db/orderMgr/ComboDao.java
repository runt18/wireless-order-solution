package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class ComboDao {

	/**
	 * Insert the combo order food.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the combo order food builder
	 * @return the combo id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	static int insert(DBCon dbCon, Staff staff, List<ComboOrderFood> combo) throws SQLException{
		
		if(combo == null || combo.isEmpty()){
			return 0;
		}
		
		String sql;
		
		//Get the max combo id;
		sql = " SELECT IFNULL(MAX(combo_id), 0) + 1 FROM " + Params.dbName + ".combo_order_food";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int comboId = 0;
		if(dbCon.rs.next()){
			comboId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		for(ComboOrderFood cof : combo){
			int tgId = TasteGroup.EMPTY_TASTE_GROUP_ID;
			if(cof.hasTasteGroup()){
				//Insert the taste group to this combo order food
				tgId = TasteGroupDao.insert(dbCon, staff, new TasteGroup.InsertBuilder(new OrderFood(cof.asComboFood().asFood()))
																	    .addTastes(cof.getTasteGroup().getNormalTastes())
																	    .setTmpTaste(cof.getTasteGroup().getTmpTaste()));
			}
			//Insert the combo order food
			sql = " INSERT INTO " + Params.dbName + ".combo_order_food" + 
				  " ( combo_id, food_id, food_name, food_amount, food_unit_id, food_unit, taste_group_id ) VALUES ( " +
				  comboId + "," +
				  cof.asComboFood().getFoodId() + "," +
				  "'" + cof.asComboFood().getName() + "'," +
				  cof.asComboFood().getAmount() + "," +
				  (cof.hasFoodUnit() ? cof.getFoodUnit().getId() : "NULL") + "," +
				  (cof.hasFoodUnit() ? "'" + cof.getFoodUnit().getUnit() + "'" : "NULL") + "," +
				  tgId +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return comboId;
	}
	
	/**
	 * Get the combo 
	 * @param dbCon
	 * @param staff
	 * @param comboId
	 * @param dateType
	 * @return
	 * @throws SQLException
	 */
	static List<ComboOrderFood> getByComboId(Staff staff, int comboId, DateType dateType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByComboId(dbCon, staff, comboId, dateType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the combo 
	 * @param dbCon
	 * @param staff
	 * @param comboId
	 * @param dateType
	 * @return
	 * @throws SQLException
	 */
	static List<ComboOrderFood> getByComboId(DBCon dbCon, Staff staff, int comboId, DateType dateType) throws SQLException{
		List<ComboOrderFood> result = new ArrayList<ComboOrderFood>();
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".combo_order_food WHERE combo_id = " + comboId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Food f = new Food(dbCon.rs.getInt("food_id"));
			f.setName(dbCon.rs.getString("food_name"));
			ComboOrderFood cof = new ComboOrderFood(new ComboFood(f, dbCon.rs.getInt("food_amount")));
			//Get the food unit.
			if(dbCon.rs.getInt("food_unit_id") != 0){
				FoodUnit unit = new FoodUnit(dbCon.rs.getInt("food_unit_id"));
				unit.setUnit(dbCon.rs.getString("food_unit"));
				cof.setFoodUnit(unit);
			}			
			//Get the taste group.
			int tgId = dbCon.rs.getInt("taste_group_id");
			if(tgId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				cof.setTasteGroup(new TasteGroup(tgId, null, null, null));
			}
			result.add(cof);
		}
		dbCon.rs.close();
		
		for(ComboOrderFood cof : result){
			if(cof.getTasteGroup() != null){
				try{
					cof.setTasteGroup(TasteGroupDao.getById(dbCon, staff, cof.getTasteGroup().getGroupId(), dateType));
				}catch(BusinessException e){
					cof.clearTasteGroup();
				}
			}
		}
		
		return result;
	}
	
}
