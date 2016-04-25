package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.Material.MonthlyChangeTypeUpdateBuilder;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.PinyinUtil;
import com.wireless.util.SQLUtil;

public class MaterialDao {
	
	public static class ExtraCond{
		private int id;
		private String name;
		private MaterialCate.Type cateType;
		private int cateId;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setCateType(MaterialCate.Type cateType){
			this.cateType = cateType;
			return this;
		}
		
		public ExtraCond setCate(int cateId){
			this.cateId = cateId;
			return this;
		}
		
		public ExtraCond setCate(MaterialCate cate){
			this.cateId = cate.getId();
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND M.material_id = " + id);
			}
			if(name != null){
				extraCond.append(" AND M.name like '%" + name + "%' ");
			}
			if(cateType != null){
				extraCond.append(" AND MC.type = " + cateType.getValue());
			}
			if(cateId != 0){
				extraCond.append(" AND MC.cate_id = " + cateId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get the material according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to material list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Material> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		final List<Material> result = new ArrayList<Material>();
		
		String sql;
		sql = " SELECT M.material_id, M.restaurant_id, M.price, M.delta, M.stock, M.name, M.status, M.last_mod_staff, M.last_mod_date, M.alarm_amount, " +
			  " MC.cate_id, MC.name cate_name, MC.type cate_type " + 
			  " FROM material M " + 
			  " JOIN material_cate MC ON MC.restaurant_id = M.restaurant_id AND MC.cate_id = M.cate_id "	+
			  " WHERE M.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs != null && dbCon.rs.next()){
			Material item = new Material();
			item.setId(dbCon.rs.getInt("material_id"));
			item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.setPrice(dbCon.rs.getFloat("price"));
			item.setDelta(dbCon.rs.getFloat("delta"));
			item.setStock(dbCon.rs.getFloat("stock"));
			item.setName(dbCon.rs.getString("name"));
			item.setLastModDate(dbCon.rs.getTimestamp("last_mod_date").getTime());
			item.setLastModStaff(dbCon.rs.getString("last_mod_staff"));
			item.setStatus(dbCon.rs.getInt("status"));
			item.setAlarmAmount(dbCon.rs.getInt("alarm_amount"));
			MaterialCate cate = new MaterialCate(dbCon.rs.getInt("cate_id"));
			cate.setName(dbCon.rs.getString("cate_name"));
			cate.setType(MaterialCate.Type.valueOf(dbCon.rs.getInt("cate_type")));
			item.setCate(cate);
			if(item.getCate().getType() == MaterialCate.Type.GOOD){
				item.setGood(true);
			}
			item.setPinyin(PinyinUtil.cn2FirstSpell(dbCon.rs.getString("name")).toUpperCase());
			
			result.add(item);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * Get the material according to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to material list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Material> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MaterialDao.getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the material to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to material retrieved
	 * @return the material to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the material to this id does NOT exist
	 */
	public static Material getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		final List<Material> result = MaterialDao.getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(MaterialError.MATERIAL_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the material to specific id
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to material retrieved
	 * @return the material to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the material to this id does NOT exist
	 */
	public static Material getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MaterialDao.getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, Material m) throws SQLException{
		int count = 0;
		String insertSQL = "INSERT INTO material"
						 + " (restaurant_id, cate_id, price, stock, status, name, last_mod_staff, last_mod_date, alarm_amount)"
						 + " values("
						 + m.getRestaurantId() + ","
						 + m.getCate().getId() + ","
						 + m.getPrice() + "," 
						 + "0,"
						 + m.getStatus().getValue() + ","
						 + "'" + m.getName() + "'" + ","
						 + "'" + m.getLastModStaff() + "'" + ","
						 + "NOW()" + ","
						 + (m.hasAlarm() ? m.getAlarmAmount() : "")
						 + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insert(Material m) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialDao.insert(dbCon, m);
			if(count == 0){
				throw new BusinessException(MaterialError.INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 */
	public static int update(DBCon dbCon, Material m) throws SQLException{
		int count = 0;
		String updateSQL;
		updateSQL = "UPDATE " + Params.dbName + ".material SET "
				 + (m.getCate().getId() != 0? " cate_id = " + m.getCate().getId() : "") 
				 + (m.getPrice() != 0 ? " ,price = " + m.getPrice() : "")
				 + (m.getStock() != 0 ? " ,stock = " + m.getStock() : "")
				 + (m.getName() != null?" ,name = '" + m.getName() + "'" : "")
				 + (m.hasAlarm() ? " ,alarm_amount = " + m.getAlarmAmount() : "")
				 + " ,last_mod_staff = '" + m.getLastModStaff() + "'"
				 + " ,last_mod_date = '" + DateUtil.format(new Date().getTime()) + "'"
				 + " WHERE material_id = " + m.getId()
				 + " AND restaurant_id = " + m.getRestaurantId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * Cancel MonthlySettle.
	 * @param restaurantId
	 * @throws SQLException
	 */
	public static void canelMonthly(int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			canelMonthly(dbCon, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel MonthlySettle.
	 * @param dbCon
	 * @param restaurantId
	 * @throws SQLException
	 */
	public static void canelMonthly(DBCon dbCon, int restaurantId) throws SQLException{
		String updateSQL = "UPDATE " + Params.dbName + ".material SET "
				 + " delta = 0 "
				 + " WHERE restaurant_id = " + restaurantId;
		
		dbCon.stmt.executeUpdate(updateSQL);
	}
	
	/**
	 * Update the delta when change type.
	 * @param builder
	 * 				the detail of material
	 * @throws SQLException
	 */
	public static void updateDelta(MonthlyChangeTypeUpdateBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateDelta(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the delta when change type.
	 * @param dbCon
	 * @param builder
	 * @throws SQLException
	 */
	public static void updateDelta(DBCon dbCon, MonthlyChangeTypeUpdateBuilder builder) throws SQLException{
		Material m = builder.build();
		String updateSQL = "UPDATE " + Params.dbName + ".material SET "
				 + " delta = " + m.getDelta()
				 + " ,last_mod_staff = '" + m.getLastModStaff() + "'"
				 + " ,last_mod_date = '" + DateUtil.format(new Date().getTime()) + "'"
				 + " WHERE material_id = " + m.getId()
				 + " AND restaurant_id = " + m.getRestaurantId();
		dbCon.stmt.executeUpdate(updateSQL);
	}
	
	/**
	 * Update the price.
	 * @param restaurantId
	 * @throws SQLException
	 */
	public static void updateMonthly(int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateMonthly(dbCon, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the price.
	 * @param dbCon
	 * @param restaurantId
	 * @throws SQLException
	 */
	public static void updateMonthly(DBCon dbCon, int restaurantId) throws SQLException{
		String updateSQL = "UPDATE " + Params.dbName + ".material SET "
				 + " price = (price + delta)" 
				 + " ,delta = 0 "
				 + " WHERE delta <> 0 " 
				 + " AND restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(updateSQL);
	}
	
	/**
	 * 
	 * @param m
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void update(Material m) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialDao.update(dbCon, m);
			if(count == 0){
				throw new BusinessException(MaterialError.UPDATE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the material to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the material id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the material to this id does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, staff, id);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the material to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the material id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the material to this id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(MaterialError.MATERIAL_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the material according to extra condition{@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to material deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(Material material : getByCond(dbCon, staff, extraCond)){
			String sql;
			
			if(material.getCate().getType() == MaterialCate.Type.GOOD){
				sql = " DELETE FROM food_material WHERE material_id = " + material.getId();
				dbCon.stmt.executeUpdate(sql);				
			}
			
			sql = " DELETE FROM material WHERE material_id = " + material.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		
		return amount;
	}
	
	/**
	 * Delete the material according to extra condition{@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to material deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = deleteByCond(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return amount;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param id
//	 * @return
//	 * @throws SQLException
//	 */
//	public static int delete(DBCon dbCon, Material m) throws SQLException{
//		int count = 0;
//		//TODO 未检查历史记录
//		
//		String deleteSQL = "DELETE FROM material "
//						 + " WHERE material_id = " + m.getId();
//		count = dbCon.stmt.executeUpdate(deleteSQL);
//		
//		if(m.isGood()){
//			String deleteFoodMaterial = "DELETE FROM food_material "
//					 + " WHERE material_id = " + m.getId();
//			dbCon.stmt.executeUpdate(deleteFoodMaterial);				
//		}
//	
//		return count;
//	}
//	
//	/**
//	 * 
//	 * @param id
//	 * @throws BusinessException
//	 * @throws SQLException
//	 */
//	public static void delete(int id) throws BusinessException, SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			int count = MaterialDao.delete(dbCon, MaterialDao.getById(dbCon, id));
//			if(count == 0){
//				throw new BusinessException(MaterialError.DELETE_FAIL);
//			}
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
	public static boolean checkMaterialFoodEx(DBCon dbCon, int foodId) throws SQLException{
		boolean bool = false;
		// 检查是否已存在商品库存资料
		String querySQL = "SELECT COUNT(*) FROM food_material"
				 + " WHERE food_id = " + foodId
				 + " AND material_id IN (SELECT material_id FROM material T1, material_cate T2 WHERE T1.cate_id = T2.cate_id AND T2.type = 1)";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			int rc = dbCon.rs.getInt(1);
			if(rc > 0){
				// 如果已存在则跳过继续操作
				bool = true;
			}
		}
		dbCon.rs.close();
		return bool;
		
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param foodId
	 * @param foodName
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static Material insertGood(DBCon dbCon, Staff term, int foodId, String foodName) throws BusinessException, SQLException{
//		checkMaterialFoodEx(dbCon, foodId);
		// 查找系统保留的商品类型
		String querySQL = "SELECT cate_id FROM material_cate " 
				 + " WHERE restaurant_id = " + term.getRestaurantId() + " AND type = " + MaterialCate.Type.GOOD.getValue();
		int cateId = 0;
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			cateId = dbCon.rs.getInt("cate_id");
		}
		if(cateId <= 0){
			throw new BusinessException(FoodError.INSERT_FAIL_NOT_FIND_GOODS_TYPE);
		}
		
		// 生成新商品库存信息
		Material good = new Material(term.getRestaurantId(), 
				foodName, 
				cateId, 
				term.getName(), 
				Material.Status.NORMAL.getValue()
		);
		try{
			MaterialDao.insert(dbCon, good);
		}catch(SQLException e){
			e.printStackTrace();
			throw new BusinessException(MaterialError.INSERT_FAIL);
		}
		dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);
		if(dbCon.rs != null && dbCon.rs.next()){
			good.setId(dbCon.rs.getInt(1));
			dbCon.rs.close();
			dbCon.rs = null;
		}
		
		// 添加菜品和库存资料之间的关系
		String insertSQL = "INSERT INTO food_material (food_id, material_id, restaurant_id, consumption)"
				  + " VALUES("
				  + foodId + ", "
				  + good.getId() + ", "
				  + good.getRestaurantId() + ", "
				  + "1"
				  + ")";
		try{
			dbCon.stmt.executeUpdate(insertSQL);
		}catch(SQLException e){
			e.printStackTrace();
			throw new BusinessException(FoodError.INSERT_FAIL_BIND_MATERIAL_FAIL);
		}
		return good;
	}
	
	public static Material insertGoods(DBCon dbCon, Staff term, Food food) throws BusinessException, SQLException{
//		checkMaterialFoodEx(dbCon, foodId);
		// 查找系统保留的商品类型
		
/*		String querySQL = "SELECT cate_id, kitchen_id FROM " + Params.dbName + ".material_cate " 
				 + " WHERE restaurant_id = " + term.getRestaurantId() + " AND type = " + MaterialCate.Type.GOOD.getValue()
				 + " AND kitchen_id = " + food.getKitchen().getId();*/
		
		String querySQL = " SELECT MAX(F.kitchen_id) AS kitchen, FM.material_id, M.cate_id FROM " + Params.dbName + ".food F  " 
						+ " JOIN " + Params.dbName + ".food_material FM ON F.food_id = FM.food_id " 
						+ " JOIN " + Params.dbName + ".material M ON M.material_id = FM.material_id "
						+ " WHERE F.kitchen_id = " + food.getKitchen().getId() +" AND F.food_id IN( "
						+ " SELECT FM.food_id FROM " + Params.dbName + ".food_material FM "
						+ " WHERE FM.restaurant_id = " + term.getRestaurantId() + " AND F.food_id = FM.food_id ) ";
		
		int cateId = 0;
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			cateId = dbCon.rs.getInt("cate_id");
		}
		if(cateId <= 0){
			String insertCate = "INSERT INTO " + Params.dbName + ".material_cate (restaurant_id, name, type)" 
						+ " VALUES("
						  + term.getRestaurantId() + ", "
						  + "'" + food.getKitchen().getName() + "', "
						  + MaterialCate.Type.GOOD.getValue()
						  + ")";
			dbCon.stmt.executeUpdate(insertCate, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			if(dbCon.rs.next()){
				cateId = dbCon.rs.getInt(1);
			}
		}
		
		// 生成新商品库存信息
		Material good = new Material(term.getRestaurantId(), 
				food.getName(), 
				cateId, 
				term.getName(), 
				Material.Status.NORMAL.getValue()
		);
		try{
			MaterialDao.insert(dbCon, good);
		}catch(SQLException e){
			e.printStackTrace();
			throw new BusinessException(MaterialError.INSERT_FAIL);
		}
		dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);
		if(dbCon.rs != null && dbCon.rs.next()){
			good.setId(dbCon.rs.getInt(1));
			dbCon.rs.close();
			dbCon.rs = null;
		}
		
		// 添加菜品和库存资料之间的关系
		String insertSQL = "INSERT INTO " + Params.dbName + ".food_material (food_id, material_id, restaurant_id, consumption)"
				  + " VALUES("
				  + food.getFoodId() + ", "
				  + good.getId() + ", "
				  + good.getRestaurantId() + ", "
				  + "1"
				  + ")";
		try{
			dbCon.stmt.executeUpdate(insertSQL);
		}catch(SQLException e){
			e.printStackTrace();
			throw new BusinessException(FoodError.INSERT_FAIL_BIND_MATERIAL_FAIL);
		}
		return good;
	}	
	
	/**
	 * Get monthSettle materials  
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
//	public static List<Material> getMonthSettleMaterial(DBCon dbCon, Staff staff, String extraCond, String otherClause) throws SQLException{
//		String sql;
//		List<Material> materials = new ArrayList<Material>();
//		try{
//			sql = "SELECT M.material_id,M.cate_id, M.name, M.price, M.delta, F.kitchen_id, F.stock_status FROM " + Params.dbName + ".food F " +
//					" JOIN " + Params.dbName + ".food_material FM ON FM.food_id = F.food_id " +
//					" JOIN " + Params.dbName + ".material M ON M.material_id = FM.material_id " +
//					" WHERE F.restaurant_id = " + staff.getRestaurantId() +
//					" AND F.stock_status = " + Food.StockStatus.GOOD.getVal() +
//			  		 (extraCond == null ? "" : extraCond) + " " +
//			  		 (otherClause == null ? "" : otherClause);
//			
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//			
//			while (dbCon.rs != null && dbCon.rs.next()) {
//				Material m = new Material();
//				m.setId(dbCon.rs.getInt("material_id"));
//				m.setCate(dbCon.rs.getInt("kitchen_id"), "", dbCon.rs.getInt("stock_status"));
//				m.setName(dbCon.rs.getString("name"));
//				m.setPrice(dbCon.rs.getFloat("price"));
//				m.setDelta(dbCon.rs.getFloat("delta"));
//				m.setGood(true);
//				materials.add(m);
//			}
//		}catch(SQLException e){
//			e.printStackTrace();
//			throw new SQLException("Failed to get the materials");
//		}
//		return materials;
//	}
	
	/**
	 * Get monthSettle materials.
	 * @param staff
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
//	public static List<Material> getMonthSettleMaterial(Staff staff, String extraCond, String otherClause) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getMonthSettleMaterial(dbCon, staff, extraCond, otherClause);
//		}finally{
//			dbCon.disconnect();
//		}
//		
//	}
	
//	public static List<Material> getAllMonthSettleMaterial(DBCon dbCon, int restaurant) throws SQLException{
//		List<Material> list = new ArrayList<Material>();
//		Material item = null;
//		String querySQL = "SELECT F.kitchen_id, F.stock_status, M.material_id, M.restaurant_id, M.price, M.delta, M.stock, M.name,"
//						+ " MC.cate_id, MC.name cate_name, MC.type cate_type"
//						+ " FROM material_cate MC, material M "
//						+ " LEFT JOIN " + Params.dbName + ".food_material FM ON FM.material_id = M.material_id " 
//						+ " LEFT JOIN " + Params.dbName + ".food F ON F.food_id = FM.food_id AND F.stock_status < " + Food.StockStatus.MATERIAL.getVal() + " " 
//						+ " WHERE MC.restaurant_id = M.restaurant_id "
//						+ " AND MC.cate_id = M.cate_id " 
//						+ " AND M.restaurant_id = " + restaurant
//						+ " ORDER BY ABS(delta) DESC";
//		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
//		while(dbCon.rs != null && dbCon.rs.next()){
//			item = new Material();
//			item.setId(dbCon.rs.getInt("material_id"));
//			item.setPrice(dbCon.rs.getFloat("price"));
//			item.setDelta(dbCon.rs.getFloat("delta"));
//			item.setStock(dbCon.rs.getFloat("stock"));
//			item.setName(dbCon.rs.getString("name"));
//			if(dbCon.rs.getString("kitchen_id") != null){
//				item.setCate(dbCon.rs.getInt("kitchen_id"), dbCon.rs.getString("cate_name"), dbCon.rs.getInt("stock_status"));
//				item.setGood(true);
//			}else{
//				item.setCate(dbCon.rs.getInt("cate_id"), dbCon.rs.getString("cate_name"), dbCon.rs.getInt("cate_type"));
//			}
//			list.add(item);
//		}
//		return list;
//	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
//	public static List<Material> getAllMonthSettleMaterial(int restaurant) throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return MaterialDao.getAllMonthSettleMaterial(dbCon, restaurant);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
	
}
