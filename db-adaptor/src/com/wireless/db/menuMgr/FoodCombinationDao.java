package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorLevel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodCombo;
import com.wireless.pojo.ppMgr.PricePlan;

public class FoodCombinationDao {
	
	/**
	 * 
	 * @param extraCondition
	 * @return
	 * @throws SQLException
	 */
	public static List<FoodCombo> getFoodCombination(String extraCondition) throws SQLException{
		List<FoodCombo> list = new ArrayList<FoodCombo>();
		FoodCombo tempItem = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String sql = "SELECT A.restaurant_id, A.food_id, A.food_alias, A.name, "
						 + " A.kitchen_alias, A.kitchen_id, A.status, A.pinyin, A.taste_ref_type, A.desc, A.img," 
						 + " B.sub_food_id, B.amount, " 
						 + " C.dept_id, C.name AS kitchen_name,"
						 + " FPP.unit_price "
						 + " FROM " + Params.dbName + ".food A JOIN " + Params.dbName + ".combo B ON A.food_id = B.sub_food_id "
						 + " LEFT JOIN " + Params.dbName + ".kitchen C ON A.kitchen_id = C.kitchen_id"
						 + " JOIN food_price_plan FPP ON A.food_id = FPP.food_id "
						 + " JOIN price_plan PP ON FPP.price_plan_id = PP.price_plan_id AND PP.status = " + PricePlan.Status.ACTIVITY.getVal()
						 + " WHERE 1=1 "
						 + (extraCondition == null ? "" : extraCondition)
						 + " ORDER BY A.food_alias desc";
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs != null && dbCon.rs.next()){
				tempItem = new FoodCombo();
				tempItem.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				tempItem.setParentId(dbCon.rs.getInt("food_id"));
				tempItem.setFoodId(dbCon.rs.getInt("sub_food_id"));
				tempItem.setAliasId(dbCon.rs.getInt("food_alias"));
				tempItem.setName(dbCon.rs.getString("name"));
				tempItem.getKitchen().setId(dbCon.rs.getInt("kitchen_id"));
				tempItem.getKitchen().setAliasId(dbCon.rs.getShort("kitchen_alias"));
				tempItem.setStatus(dbCon.rs.getByte("status"));
				tempItem.setPinyin(dbCon.rs.getString("pinyin"));
				tempItem.setTasteRefType(dbCon.rs.getShort("taste_ref_type"));
				tempItem.setDesc(dbCon.rs.getString("desc"));
				tempItem.setImage(dbCon.rs.getString("img"));
				tempItem.setPrice(dbCon.rs.getFloat("unit_price"));
				
				tempItem.setAmount(dbCon.rs.getInt("amount"));
				
				list.add(tempItem);
			}
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param parent
	 * @param list
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodCombination(FoodCombo parent, FoodCombo[] list) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			if(parent == null){
				throw new BusinessException("操作失败, 获取菜品信息失败!", ErrorLevel.ERROR);
			}
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String deleteSQL = "delete from " + Params.dbName + ".combo where food_id = " + parent.getParentId() + " and restaurant_id = " + parent.getRestaurantId();
			String udpateSQL = "";
			StringBuffer insertSQL = new StringBuffer();
			
			dbCon.stmt.executeUpdate(deleteSQL);
			
			if(list != null && list.length > 0){
				int temp = parent.getStatus();
				parent.setStatus(temp |= Food.COMBO);
				insertSQL.append("insert into " + Params.dbName + ".combo ");
				insertSQL.append(" (food_id,sub_food_id,restaurant_id,amount) ");
				insertSQL.append("values");
				for(int i = 0; i < list.length; i++){
					insertSQL.append(i > 0 ? "," : "");
					insertSQL.append("(");
					insertSQL.append(parent.getParentId());
					insertSQL.append(",");
					insertSQL.append(list[i].getFoodId());
					insertSQL.append(",");
					insertSQL.append(parent.getRestaurantId());
					insertSQL.append(",");
					insertSQL.append(list[i].getAmount());
					insertSQL.append(")");
				}
				dbCon.stmt.executeUpdate(insertSQL.toString());
			}else{
				parent.setStatus((short)(parent.getStatus() & ~Food.COMBO));
			}
			
			udpateSQL = "update " + Params.dbName + ".food set status = " + parent.getStatus() + " where food_id = " + parent.getParentId() + " and restaurant_id = " + parent.getRestaurantId();
			dbCon.stmt.executeUpdate(udpateSQL);
			
			dbCon.conn.commit();
			
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param parent
	 * @param content
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodCombination(FoodCombo parent, String content) throws BusinessException, SQLException {
		FoodCombo[] list = null;
		FoodCombo item = null;
		String[] sl = content.split("<split>");
		if(sl != null && sl.length != 0){
			if(sl.length == 1 && sl[0].trim().length() == 0){
				list = null;
			}else{
				list = new FoodCombo[sl.length];
				for(int i = 0; i < sl.length; i++){
					String[] temp = sl[i].split(",");
					item = new FoodCombo();
					item.setParentId(parent.getFoodId());
					item.setRestaurantId(parent.getRestaurantId());
					item.setFoodId(Integer.parseInt(temp[0]));
					item.setAmount(Integer.parseInt(temp[1]));
					list[i] = item;
					item = null;
				}
			}
		}
		FoodCombinationDao.updateFoodCombination(parent, list);
		
	}
	
	/**
	 * 
	 * @param parentFoodID
	 * @param restaurantID
	 * @param status
	 * @param content
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodCombination(long parentFoodID, int restaurantID, int status, String content) throws BusinessException, SQLException {
		FoodCombo parent = new FoodCombo();
		parent.setParentId(parentFoodID);
		parent.setRestaurantId(restaurantID);
		parent.setStatus(status);
		FoodCombinationDao.updateFoodCombination(parent, content);
	}
}
