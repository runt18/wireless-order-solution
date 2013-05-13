package com.wireless.db.menuMgr;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.exception.MaterialError;
import com.wireless.exception.PlanError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.protocol.Food;
import com.wireless.protocol.Terminal;
import com.wireless.util.SQLUtil;

public class FoodDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insertFoodBaisc(DBCon dbCon, Terminal term, Food fb) throws BusinessException, SQLException{
		int count = 0;
		String insertSQL = "", querySQL = "";
		// 检查菜品是否存在
		querySQL = "SELECT count(food_alias) count FROM " + Params.dbName + ".food WHERE restaurant_id = " + fb.getRestaurantId() + " AND food_alias = " + fb.getAliasId();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt("count");
			break;
		}
		if(count > 0){
			throw new BusinessException("操作失败,该编号菜品已经存在!");
		}
		// 新增菜品信息
		insertSQL = "INSERT INTO " + Params.dbName + ".food" 
				+ " ( food_alias, name, pinyin, restaurant_id, kitchen_id, kitchen_alias, status, taste_ref_type, food.desc, food.stock_status ) "
				+ "values("
				+ fb.getAliasId() + ", " 
				+ "'" + fb.getName() + "', " 
				+ "'" + fb.getPinyin() + "', " 
				+ fb.getRestaurantId() + ", " 
				+ fb.getKitchen().getId() + ", " 
				+ fb.getKitchen().getAliasId() + ", " 
				+ fb.getStatus() + ", " 
				+ Food.TasteRef.SMART.getVal() + ", "
				+ "'" + fb.getDesc() + "', "
				+ fb.getStockStatus().getVal()
				+ ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		// 获取新增菜品数据编号
		dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);
		if(dbCon.rs != null && dbCon.rs.next()){
			fb.setFoodId(dbCon.rs.getInt(1));
			dbCon.rs = null;
		}
		
		// 新增菜谱价格方案信息
		insertSQL = "INSERT INTO food_price_plan (restaurant_id, food_id, price_plan_id, unit_price)"
				  + " SELECT " + fb.getRestaurantId() + "," + fb.getFoodId() + ",price_plan_id," + fb.getPrice() + " FROM price_plan WHERE restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException(PlanError.PRICE_FOOD_INSERT);
		}
		
		// 
		try{
			Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + fb.getFoodId(), null);
			if(updateFood.length != 0){
				TasteRefDao.execByFood(updateFood[0]);
			}
		} catch(Exception e){
			throw new BusinessException(FoodError.TASTE_UPDATE_FAIL);
		}	
		
		// 处理库存
		if(fb.getStockStatus() == Food.StockStatus.NONE){
			// 无需处理
		}else if(fb.getStockStatus() == Food.StockStatus.GOOD){
			MaterialDao.insertGood(dbCon, term, (int)fb.getFoodId(), fb.getName());
		}else if(fb.getStockStatus() == Food.StockStatus.MATERIAL){
			// 无需处理
		}
		return count;
	}
	
	/**
	 * 
	 * @param term
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insertFoodBaisc(Terminal term, Food fb) throws BusinessException, SQLException{		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = insertFoodBaisc(dbCon, term, fb);
			dbCon.conn.commit();
			if(count == 0){
				throw new BusinessException(FoodError.INSERT_FAIL);
			}
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param term
	 * @param fb
	 * @param content
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insertFoodBaisc(Terminal term, Food fb, String content) throws BusinessException, SQLException{
		FoodDao.insertFoodBaisc(term, fb);
		try{
			Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + fb.getFoodId(), null);
			if(updateFood.length != 0){
				TasteRefDao.execByFood(updateFood[0]);
			}
			FoodCombinationDao.updateFoodCombination(fb.getFoodId(), fb.getRestaurantId(), fb.getStatus(), content);
		} catch(Exception e){
			throw new BusinessException(FoodError.COMBO_UPDATE_FAIL);
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updateFoodBaisc(DBCon dbCon, Terminal term, Food fb) throws BusinessException, SQLException{
		Food old = MenuDao.getFoodById(dbCon, (int)fb.getFoodId());
		int count = 0;
		String updateSQL = "", deleteSQL = "";
		// 修改当前活动价格方案信息 
		updateSQL = "UPDATE " + Params.dbName + ".food_price_plan SET unit_price = " + fb.getPrice()
				  + " WHERE food_id = " + fb.getFoodId()
				  + " AND price_plan_id = (SELECT price_plan_id FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + fb.getRestaurantId() + " AND status = " + PricePlan.Status.ACTIVITY.getVal() + ")";
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException(FoodError.UPDATE_PRICE_FAIL);
		}
		// 修改菜品基础信息
		updateSQL = "UPDATE " + Params.dbName + ".food "
				  + " SET name = '" + fb.getName() + "', "
				  + " pinyin = '"+ fb.getPinyin() + "', "
				  + " kitchen_id =  " + (fb.getKitchen().getId() < 0 ? null : fb.getKitchen().getId()) + ", " 
				  + " kitchen_alias = " + fb.getKitchen().getAliasId() + ", "
				  + " status =  " + fb.getStatus() + ", "
				  + " food.desc = " + (fb.getDesc() == null || fb.getDesc().trim().length() == 0 ? null : "'" + fb.getDesc() + "'") + ", "
				  + " food.stock_status = " + fb.getStockStatus().getVal()
				  +" WHERE restaurant_id=" + fb.getRestaurantId() + " and food_id = " + fb.getFoodId();
		
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException(FoodError.UPDATE_FAIL);
		}
		
		// 处理库存资料
		if(fb.getStockStatus() == Food.StockStatus.NONE){
			if(old.getStockStatus() == Food.StockStatus.NONE){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.GOOD){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.MATERIAL){
				// 删除原料出库关系
				deleteSQL = "DELETE FROM food_material"
						  + " WHERE food_id = " + fb.getFoodId()
						  + " AND material_id NOT IN (SELECT material_id FROM material T1, material_cate T2 WHERE T1.cate_id = T2.cate_id AND T2.type = " + MaterialCate.Type.GOOD + ")";
				dbCon.stmt.executeUpdate(deleteSQL);
			}
		}else if(fb.getStockStatus() == Food.StockStatus.GOOD){
			if(old.getStockStatus() == Food.StockStatus.NONE){
				// 添加新商品库存资料
				MaterialDao.insertGood(dbCon, term, (int)fb.getFoodId(), fb.getName());
			}else if(old.getStockStatus() == Food.StockStatus.GOOD){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.MATERIAL){
				// 删除原料出库关系
				deleteSQL = "DELETE FROM food_material"
						  + " WHERE food_id = " + fb.getFoodId()
						  + " AND material_id NOT IN (SELECT material_id FROM material T1, material_cate T2 WHERE T1.cate_id = T2.cate_id AND T2.type = " + MaterialCate.Type.GOOD + ")";
				dbCon.stmt.executeUpdate(deleteSQL);
				// 添加新商品库存资料
				MaterialDao.insertGood(dbCon, term, (int)fb.getFoodId(), fb.getName());
			}
		}else if(fb.getStockStatus() == Food.StockStatus.MATERIAL){
			if(old.getStockStatus() == Food.StockStatus.NONE){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.GOOD){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.MATERIAL){
				// 无需处理
			}
		}
		
		return count;
	}
	
	/**
	 * 
	 * @param term
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodBaisc(Terminal term, Food fb) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = FoodDao.updateFoodBaisc(dbCon, term, fb);
			dbCon.conn.commit();
			if(count == 0){
				throw new BusinessException(FoodError.UPDATE_FAIL);
			}
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int deleteFood(DBCon dbCon, Food fb) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "", deleteSQL = "", tableIDList = "";
		
		// 验证删除菜品是否正在营业使用过程中
		querySQL = "SELECT A.id, A.table_id, A.table_alias, A.table_name, B.food_id, SUM(B.order_count) order_count "
				 + " FROM " + Params.dbName + ".order A, " + Params.dbName + ".order_food B"
				 + " WHERE A.id = B.order_id AND A.status = " + Order.STATUS_UNPAID + " AND A.restaurant_id = " + fb.getRestaurantId()
				 + " GROUP BY B.order_id, B.food_id "
				 + " HAVING B.food_id = " + fb.getFoodId()
				 + " ORDER BY A.table_alias ";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		int index = 0;
		while(dbCon.rs != null && dbCon.rs.next()){
			if(index >= 0 && index < 5){
				tableIDList += ((index > 0 ? "," : "") + dbCon.rs.getInt("table_alias"));
			}else{
				tableIDList += ".";
			}
			index++;
		}
		if(!tableIDList.trim().isEmpty()){
			throw new BusinessException(FoodError.DELETE_FAIL_IS_USED);
		}
		
		// delete food
		deleteSQL = "DELETE FROM " + Params.dbName + ".food WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(FoodError.DELETE_FAIL);
		
		// delete foodTaste
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_taste_rank WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(FoodError.TASTE_DELETE_FAIL);
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_taste WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(FoodError.TASTE_DELETE_FAIL);
		
		// delete foodCombination
		deleteSQL = "DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(FoodError.COMBO_DELETE_FAIL);
		
		// delete foodPricePlan
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_price_plan WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(PlanError.PRICE_FOOD_DELETE);
		
		//delete material
		deleteSQL = "DELETE FROM material WHERE "
				  + " material_id = (SELECT material_id FROM " + Params.dbName + ".food_material WHERE food_id = " + fb.getFoodId() + " AND restaurant_id = " + fb.getRestaurantId() + ") "
				  + " AND cate_id = (SELECT cate_id FROM " + Params.dbName + ".material_cate WHERE restaurant_id = " + fb.getRestaurantId() + " AND type = " + MaterialCate.Type.GOOD.getValue() + ") ";
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(MaterialError.DELETE_FAIL);
		
		//delete foodMaterial
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_material WHERE food_id = " + fb.getFoodId() + " AND restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			new BusinessException(MaterialError.BINDING_DELETE_FAIL);
		
		return count;
	}
	
	/**
	 * 
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void deleteFood(Food fb) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = FoodDao.deleteFood(dbCon, fb);
			dbCon.conn.commit();
			if(count == 0){
				throw new BusinessException(FoodError.DELETE_FAIL);
			}
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param restaurantId
	 * @param foodId
	 * @param imgName
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodImageName(int restaurantId, int foodId, String imgName) throws BusinessException, SQLException{
		Food fb = new Food();
		fb.setRestaurantId(restaurantId);
		fb.setFoodId(foodId);
		fb.setImage(imgName);
		updateFoodImageName(fb);
	}
	
	/**
	 * 
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodImageName(Food fb) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String updateSQL = "update " + Params.dbName + ".food set "
						       + " img = " + (fb.getImage() == null || fb.getImage().trim().length() == 0 ? null : "'" + fb.getImage() + "'") 
						       + " where food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
			
			int count = dbCon.stmt.executeUpdate(updateSQL);
			if(count <= 0){
				throw new BusinessException("操作失败,未更新编号为" + fb.getFoodId() + "的菜品图片信息!");
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param fb
	 * @return
	 * @throws SQLException
	 */
	public static Food getFoodBasicImage(Food fb) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String selectSQL = "select A.img" 
						       + " from " + Params.dbName + ".food A"
						       + " where A.food_id = " + fb.getFoodId() + " and A.restaurant_id = " + fb.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			if(dbCon.rs != null && dbCon.rs.next()){
				fb.setImage(dbCon.rs.getString("img"));
			}
		}finally{
			dbCon.disconnect();
		}
		return fb;
	}
	
}
