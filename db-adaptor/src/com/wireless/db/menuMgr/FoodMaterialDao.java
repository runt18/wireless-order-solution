package com.wireless.db.menuMgr;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.menuMgr.FoodMaterial;

public class FoodMaterialDao {
	
	/**
	 * 
	 * @param item
	 * @throws Exception
	 */
	public static void updateFoodMaterial(FoodMaterial item) throws Exception{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			String sql = "update " + Params.dbName + ".food_material set consumption = " + item.getConsumption()
						 + " where"
						 + " restaurant_id = " + item.getRestaurantId()
						 + " and food_id = " + item.getFoodId()
						 + " and material_id = " + item.getMaterialID();
			
			dbCon.stmt.executeUpdate(sql);
			
		} catch(Exception e){
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param list
	 * @throws Exception
	 */
	public static void updateFoodMaterial(FoodMaterial parent, FoodMaterial[] list) throws Exception{
		
		DBCon dbCon = new DBCon();
		
		try{
			if(parent == null){
				throw new Exception("操作失败,获取菜品信息失败!");
			}
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String deleteSQL = "delete from " + Params.dbName + ".food_material where food_id = " + parent.getFoodId() + " and restaurant_id = " + parent.getRestaurantId();
			StringBuffer insertSQL = new StringBuffer();
			
			dbCon.stmt.executeUpdate(deleteSQL);
			
			if(list != null && list.length > 0){
				insertSQL.append("insert into " + Params.dbName + ".food_material ");
				insertSQL.append("(food_id,material_id,restaurant_id,consumption) ");
				insertSQL.append(" values");
				for(int i = 0; i < list.length; i++){
					insertSQL.append(i > 0 ? "," : "");
					insertSQL.append("(");
					insertSQL.append(parent.getFoodId());
					insertSQL.append(',');
					insertSQL.append(list[i].getMaterialID());
					insertSQL.append(',');
					insertSQL.append(parent.getRestaurantId());
					insertSQL.append(',');
					insertSQL.append(list[i].getConsumption());
					insertSQL.append(")");
				}
				dbCon.stmt.executeUpdate(insertSQL.toString());
			}
			dbCon.conn.commit();
		} catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param parent
	 * @param content
	 * @throws Exception
	 */
	public static void updateFoodMaterial(FoodMaterial parent, String content) throws Exception{
		try{
			FoodMaterial[] list = null;
			FoodMaterial item = null;
			String[] sl = content.split("<split>");
			if(sl != null && sl.length != 0){
				if(sl.length == 1 && sl[0].trim().length() == 0){
					list = null;
				}else{
					list = new FoodMaterial[sl.length];
					for(int i = 0; i < sl.length; i++){
						String[] temp = sl[i].split(",");
						item = new FoodMaterial();
						item.setFoodId(parent.getFoodId());
						item.setRestaurantId(parent.getRestaurantId());
						item.setMaterialID(Integer.parseInt(temp[0]));
						item.setConsumption(Float.parseFloat(temp[1]));
						list[i] = item;
						item = null;
					}
				}
				
			}
			FoodMaterialDao.updateFoodMaterial(parent, list);
		} catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param foodID
	 * @param restaurantID
	 * @param content
	 * @throws Exception
	 */
	public static void updateFoodMaterial(int foodID, int restaurantID, String content) throws Exception{
		try{
			FoodMaterial parent = new FoodMaterial();
			parent.setFoodId(foodID);
			parent.setRestaurantId(restaurantID);
			FoodMaterialDao.updateFoodMaterial(parent, content);
		}catch(Exception e){
			throw e;
		}
	}
}
