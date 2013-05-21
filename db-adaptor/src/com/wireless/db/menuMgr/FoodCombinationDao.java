package com.wireless.db.menuMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.menuMgr.FoodCombination;

public class FoodCombinationDao {
	
	/**
	 * 
	 * @param extraCondition
	 * @return
	 * @throws Exception
	 */
	public static FoodCombination[] getFoodCombination(String extraCondition) throws Exception{
		FoodCombination[] list = new FoodCombination[0];
		List<FoodCombination> tempList = new ArrayList<FoodCombination>();
		FoodCombination tempItem = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String sql = "select A.restaurant_id, A.food_id, A.food_alias, A.name, "
						 + " A.kitchen_alias, A.kitchen_id, A.status, A.pinyin, A.taste_ref_type, A.desc, A.img," 
						 + " B.sub_food_id, B.amount, " 
						 + " C.dept_id, C.name AS kitchen_name"
						 + " from " + Params.dbName + ".food A join " + Params.dbName + ".combo B on A.food_id = B.sub_food_id left join " + Params.dbName + ".kitchen C on A.kitchen_id = C.kitchen_id" 
						 + " where 1=1 "
						 + (extraCondition == null ? "" : extraCondition)
						 + " order by A.food_alias desc";
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs != null && dbCon.rs.next()){
				tempItem = new FoodCombination();
				tempItem.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				tempItem.setParentFoodID(dbCon.rs.getInt("food_id"));
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
				
				tempItem.setAmount(dbCon.rs.getInt("amount"));
				tempItem.getKitchen().setName(dbCon.rs.getString("kitchen_name"));
				
				tempList.add(tempItem);
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		list =  tempList.toArray(new FoodCombination[tempList.size()]);
		return list;
	}
	
	/**
	 * 
	 * @param parent
	 * @param list
	 * @throws Exception
	 */
	public static void updateFoodCombination(FoodCombination parent, FoodCombination[] list) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			if(parent == null){
				throw new Exception("操作失败,获取菜品信息失败!");
			}
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String deleteSQL = "delete from " + Params.dbName + ".combo where food_id = " + parent.getParentFoodID() + " and restaurant_id = " + parent.getRestaurantId();
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
					insertSQL.append(parent.getParentFoodID());
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
			
			udpateSQL = "update " + Params.dbName + ".food set status = " + parent.getStatus() + " where food_id = " + parent.getParentFoodID() + " and restaurant_id = " + parent.getRestaurantId();
			dbCon.stmt.executeUpdate(udpateSQL);
			
			dbCon.conn.commit();
		} catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			
		}
	}
	
	/**
	 * 
	 * @param parent
	 * @param content
	 * @throws Exception
	 */
	public static void updateFoodCombination(FoodCombination parent, String content) throws Exception{
		try{
			FoodCombination[] list = null;
			FoodCombination item = null;
			String[] sl = content.split("<split>");
			if(sl != null && sl.length != 0){
				if(sl.length == 1 && sl[0].trim().length() == 0){
					list = null;
				}else{
					list = new FoodCombination[sl.length];
					for(int i = 0; i < sl.length; i++){
						String[] temp = sl[i].split(",");
						item = new FoodCombination();
						item.setParentFoodID(parent.getFoodId());
						item.setRestaurantId(parent.getRestaurantId());
						item.setFoodId(Integer.parseInt(temp[0]));
						item.setAmount(Integer.parseInt(temp[1]));
						list[i] = item;
						item = null;
					}
				}
			}
			FoodCombinationDao.updateFoodCombination(parent, list);
		} catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param foodID
	 * @param restaurant
	 * @param content
	 * @throws Exception
	 */
	public static void updateFoodCombination(long parentFoodID, int restaurantID, int status, String content) throws Exception{
		try{
			FoodCombination parent = new FoodCombination();
			parent.setParentFoodID(parentFoodID);
			parent.setRestaurantId(restaurantID);
			parent.setStatus(status);
			FoodCombinationDao.updateFoodCombination(parent, content);
		}catch(Exception e){
			throw e;
		}
	}
}
