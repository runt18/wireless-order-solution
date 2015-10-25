package com.wireless.db.menuMgr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.oss.OSSException;
import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.oss.OssImageDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodStatistics;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.PinyinUtil;

public class FoodDao {
	
	public static class ExtraCond4Price{
		public static enum ShowType{
			BY_PLAN("按方案显示"),
			BY_FOOD("按菜品显示");
			
			private final String desc;
			ShowType(String desc){
				this.desc = desc;
			}
			@Override
			public String toString(){
				return desc;
			}
		}
		
		private final Food food;
		private PricePlan price;
		private ShowType type = ShowType.BY_FOOD;
		
		public ExtraCond4Price(Food food){
			this.food = food;
		}
		
		public ExtraCond4Price setPricePlan(PricePlan plan){
			this.price = plan;
			return this;
		}
		
		public ExtraCond4Price setShowType(ShowType type){
			this.type = type;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(food != null){
				extraCond.append(" AND FPP.food_id = " + food.getFoodId());
			}
			if(price != null){
				extraCond.append(" AND FPP.price_plan_id = " + price.getId());
			}
			return extraCond.toString();
		}
	}
	
	public static class ExtraCond4Combo{
		private final int parentId;
		
		private int childId;
		
		public ExtraCond4Combo(Food parent){
			this.parentId = parent.getFoodId();
		}
		
		public ExtraCond4Combo(int parentId){
			this.parentId = parentId;
		}
		
		public ExtraCond4Combo setChildId(int childId){
			this.childId = childId;
			return this;
		}
		
		ExtraCond extraCond(){
			final String sql = " SELECT sub_food_id FROM " + Params.dbName + ".combo WHERE 1 = 1 " + this.toString();
			return new ExtraCond().setExtra(" FOOD.food_id IN (" + sql + ")");
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			extraCond.append(" AND combo.food_id = " + parentId);
			if(childId > 0){
				extraCond.append(" AND combo.sub_food_id = " + childId);
			}
			return extraCond.toString();
		}
	}
	
	public static class ExtraCond{
		private int id;
		private int restaurantId;
		private int kitchenId;
		private int deptId;
		private String name;
		private String pinyin;
		private float price = -1;
		private int alias = -1;
		private int special = -1;
		private int recomment = -1;
		private int sellout = -1;
		private int gift = -1;
		private int curPrice = -1;
		private int combo = -1;
		private int hot = -1;
		private int weight = -1;
		private int commission = -1;
		private int temp = -1;
		private int limit = -1;
		private final List<Short> statusList = new ArrayList<Short>();
		public int containsImage = -1;
		private String extra;
		
		ExtraCond setExtra(String extra){
			this.extra = extra;
			return this;
		}
		
		public ExtraCond setContainsImage(boolean onOff){
			containsImage = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setPrice(float price){
			this.price = price;
			return this;
		}
		
		public ExtraCond setAlias(int alias){
			this.alias = alias;
			return this;
		}
		
		public ExtraCond addStatus(Short status){
			if(!this.statusList.contains(status)){
				this.statusList.add(status);
			}
			return this;
		}
		
		public ExtraCond setLimit(boolean onOff){
			this.limit = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setTemp(boolean onOff){
			this.temp = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setCommisson(boolean onOff){
			this.commission = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setWeight(boolean onOff){
			this.weight = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setHot(boolean onOff){
			this.hot = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setCombo(boolean onOff){
			this.combo = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setCurPrice(boolean onOff){
			this.curPrice = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setGift(boolean onOff){
			this.gift = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setSellout(boolean onOff){
			this.sellout = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setRecomment(boolean onOff){
			this.recomment = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setSpecial(boolean onOff){
			this.special = onOff ? 1 : 0;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		ExtraCond setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public ExtraCond setKitchen(int kitchenId){
			this.kitchenId = kitchenId;
			return this;
		}
		
		public ExtraCond setKitchen(Kitchen kitchen){
			this.kitchenId = kitchen.getId();
			return this;
		}
		
		public ExtraCond setDepartment(Department dept){
			this.deptId = dept.getId();
			return this;
		}
		
		public ExtraCond setDepartment(int deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setPinyin(String pinyin){
			this.pinyin = pinyin;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND FOOD.food_id = " + id);
			}
			if(alias >= 0){
				extraCond.append(" AND FOOD.food_alias = " + alias);
			}
			if(restaurantId != 0){
				extraCond.append(" AND FOOD.restaurant_id = " + restaurantId);
			}
			if(kitchenId != 0){
				extraCond.append(" AND FOOD.kitchen_id = " + kitchenId);
			}
			if(deptId != 0){
				extraCond.append(" AND DEPT.dept_id = " + deptId);
			}
			if(name != null){
				extraCond.append(" AND FOOD.name like '%" + name.trim() + "%'");
			}
			if(pinyin != null){
				extraCond.append(" AND FOOD.pinyin like '" + pinyin.trim() + "%'");
			}
			if(price >= 0){
				extraCond.append(" AND FOOD.price = " + price);
			}
			if(limit >= 0){
				if(limit == 1){
					extraCond.append(" AND (FOOD.status & " + Food.LIMIT + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.LIMIT + " = 0)");
				}
			}
			if(special >= 0){
				if(special == 1){
					extraCond.append(" AND (FOOD.status & " + Food.SPECIAL + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.SPECIAL + " = 0)");
				}
			}
			if(recomment >= 0){
				if(recomment == 1){
					extraCond.append(" AND (FOOD.status & " + Food.RECOMMEND + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.RECOMMEND + " = 0)");
				}
			}
			if(sellout >= 0){
				if(sellout == 1){
					extraCond.append(" AND (FOOD.status & " + Food.SELL_OUT + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.SELL_OUT + " = 0)");
				}
			}
			if(gift >= 0){
				if(gift == 1){
					extraCond.append(" AND (FOOD.status & " + Food.GIFT + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.GIFT + " = 0)");
				}
			}
			if(curPrice >= 0){
				if(curPrice == 1){
					extraCond.append(" AND (FOOD.status & " + Food.CUR_PRICE + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.CUR_PRICE + " = 0)");
				}
			}
			if(combo >= 0){
				if(combo == 1){
					extraCond.append(" AND (FOOD.status & " + Food.COMBO + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.COMBO + " = 0)");
				}
			}
			if(hot >= 0){
				if(hot == 0){
					extraCond.append(" AND (FOOD.status & " + Food.HOT + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.HOT + " = 0)");
				}
			}
			if(weight >= 0){
				if(weight == 1){
					extraCond.append(" AND (FOOD.status & " + Food.WEIGHT + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.WEIGHT + " = 0)");
				}
			}
			if(commission >= 0){
				if(commission == 1){
					extraCond.append(" AND (FOOD.status & " + Food.COMMISSION + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.COMMISSION + " = 0)");
				}
			}
			if(temp >= 0){
				if(temp == 1){
					extraCond.append(" AND (FOOD.status & " + Food.TEMP + " <> 0)");
				}else{
					extraCond.append(" AND (FOOD.status & " + Food.TEMP + " = 0)");
				}
			}
			StringBuilder statusCond = new StringBuilder();
			for(Short status : statusList){
				if(statusCond.length() == 0){
					statusCond.append(" (FOOD.status & " + status + " <> 0) ");
				}else{
					statusCond.append(" OR (FOOD.status & " + status + " <> 0) ");
				}
			}
			if(statusCond.length() != 0){
				extraCond.append(" AND (" + statusCond.toString() + ")");
			}
			if(extra != null){
				extraCond.append(" AND " + extra);
			}
			if(containsImage != -1){
				if(containsImage == 0){
					extraCond.append(" AND FOOD.oss_image_id = 0 ");
				}else{
					extraCond.append(" AND FOOD.oss_image_id <> 0 ");
				}
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Check to see whether the food alias is duplicated.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param f
	 * 			the food to check			
	 * @return true if the food alias is duplicated, otherwise false
	 * @param foodAlias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static boolean isAliasDuplicated(DBCon dbCon, Staff staff, Food f) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".food WHERE 1 = 1 " + 
			  " AND restaurant_id = " + staff.getRestaurantId() + 
			  " AND food_alias = " + f.getAliasId() +
			  " AND food_id <> " + f.getFoodId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		boolean isDuplicated = false;
		if(dbCon.rs.next()){
			isDuplicated = (dbCon.rs.getInt(1) != 0);
		}
		dbCon.rs.close();
		
		return isDuplicated;
	}
	
	/**
	 * Insert a new food according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new food
	 * @return the id to food just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int insert(Staff staff, Food.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int foodId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return foodId;
			
		}catch(SQLException | BusinessException | OSSException | ClientException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new food according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new food
	 * @return the id to food just inserted
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	public static int insert(DBCon dbCon, Staff staff, Food.InsertBuilder builder) throws SQLException, BusinessException{
		String sql;
		Food f = builder.build();

		//Check to see whether the alias is duplicated.
		if(builder.isAliasChanged()){
			if(isAliasDuplicated(dbCon, staff, f)){
				throw new BusinessException(FoodError.DUPLICATED_FOOD_ALIAS);
			}
		}
		
		sql = " INSERT INTO " + Params.dbName + ".food" +
			  " (`name`, `food_alias`, `price`, `commission`, `restaurant_id`, `kitchen_id`, `status`, `limit_amount`, `oss_image_id`, `desc` ) VALUES ( " +
			  "'" + f.getName() + "'," +
			  f.getAliasId() + "," +
			  f.getPrice() + "," +
			  f.getCommission() + "," +
			  staff.getRestaurantId() + "," +
			  f.getKitchen().getId() + "," +
			  f.getStatus() + "," +
			  (f.isLimit() ? f.getLimitAmount() : "NULL") + "," +
			  (f.hasImage() ? f.getImage().getId() : "NULL") + "," +
			  (f.hasDesc() ? "'" + f.getDesc() + "'" : "NULL") + 
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int foodId = 0;
		if(dbCon.rs.next()){
			foodId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The food id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		//Insert the associated food units
		for(FoodUnit foodUnit : f.getFoodUnits()){
			FoodUnitDao.insert(dbCon, staff, new FoodUnit.InsertBuilder(foodId, foodUnit.getPrice(), foodUnit.getUnit()));
		}
		
		for(Map.Entry<PricePlan, Float> entry : f.getPricePlan().entrySet()){
			//Check to see whether or not the price plan exist. 
			PricePlanDao.getById(dbCon, staff, entry.getKey().getId());
			//Insert the price to this plan. 
			sql = " INSERT INTO " + Params.dbName + ".food_price_plan" +
				  " (food_id, price_plan_id, price) VALUES (" +
				  foodId + "," +
				  entry.getKey().getId() + "," +
				  entry.getValue().floatValue() +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Married the oss image with this food.
		if(f.hasImage()){
			try{
				//Associated the oss image with this food
				OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(f.getImage().getId()).setAssociated(OssImage.Type.FOOD_IMAGE, foodId));
			}catch(IOException ignored){}
		}
		
		return foodId;
	}
	
	/**
	 * Delete the food to a specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the food is still used by any order which is unpaid<br>
	 * 			<li>throws if the food is the sub to any combo<br>
	 * 			<li>throws if if the food has stock status
	 */
	public static void delete(Staff staff, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff, foodId);
			dbCon.conn.commit();
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the food to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the food is still used by any order which is unpaid<br>
	 * 			<li>throws if the food is the sub to any combo<br>
	 * 			<li>throws if if the food has stock status
	 */
	public static void delete(DBCon dbCon, Staff staff, int foodId) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the food is used by unpaid order.
		sql = " SELECT food_id " +
			  " FROM " + Params.dbName + ".order_food OF " +
			  " JOIN " + Params.dbName + ".order O ON 1 = 1 " + 
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.status = " + Order.Status.UNPAID.getVal() +
			  " WHERE OF.food_id = " + foodId +
			  " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(FoodError.FOOD_IN_USED);
		}
		dbCon.rs.close();
		
		//Check to see whether the food is the sub to any combo.
		sql = " SELECT sub_food_id FROM " + Params.dbName + ".combo WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " AND sub_food_id = " + foodId +
			  " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(FoodError.DELETE_FAIL_SINCE_COMBO_SUB_FOOD);
		}
		dbCon.rs.close();
		
		//Delete the associated combo info
		sql = " DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + foodId;
		dbCon.stmt.executeUpdate(sql);

		//Check to see whether or not the food to delete contains image.
		sql  =" SELECT oss_image_id FROM " + Params.dbName + ".food WHERE food_id = " + foodId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt("oss_image_id") != 0){
				//Delete the associated oss image.
				OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setId(dbCon.rs.getInt("oss_image_id")));
			}
		}
		dbCon.rs.close();
		
		//Delete the associated price plan.
		sql = " DELETE FROM " + Params.dbName + ".food_price_plan WHERE food_id = " + foodId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the associated food unit
		FoodUnitDao.deleteByCond(dbCon, staff, new FoodUnitDao.ExtraCond().addFood(foodId));
		
		//Delete the food info
		sql = " DELETE FROM " + Params.dbName + ".food WHERE food_id = " + foodId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
	}
	
	/**
	 * Update the food according to specific builder {@link Food#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a food {@link Food#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below 
	 * 			<li>the food to update does NOT exist
	 * 			<li>the food alias is duplicated
	 */
	public static void update(Staff staff, Food.UpdateBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the food according to specific builder {@link Food#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a food {@link Food#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below 
	 * 			<li>the food to update does NOT exist
	 * 			<li>the food alias is duplicated
	 */
	public static void update(DBCon dbCon, Staff staff, Food.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		Food f = builder.build();
		
		//Check to see whether the alias is duplicated.
		if(builder.isAliasChanged() && f.getAliasId() != 0){
			if(isAliasDuplicated(dbCon, staff, f)){
				throw new BusinessException(FoodError.DUPLICATED_FOOD_ALIAS);
			}
		}
		
		//Compare the original status against the new and set the status bit if changed.
		sql = " SELECT status, limit_amount FROM " + Params.dbName + ".food WHERE food_id = " + f.getFoodId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			f.setStatus(dbCon.rs.getInt("status"));
			f.setLimitAmount(dbCon.rs.getInt("limit_amount"));
		}else{
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
		dbCon.rs.close();
		
		Food food4Status = builder.build();
		if(builder.isCurPriceChanged()){
			f.setCurPrice(food4Status.isCurPrice());
		}
		if(builder.isGiftChanged()){
			f.setGift(food4Status.isGift());
		}
		if(builder.isSelloutChanged()){
			f.setSellOut(food4Status.isSellOut());
		}
		if(builder.isRecommendChanged()){
			f.setRecommend(food4Status.isRecommend());
		}
		if(builder.isSpecialChanged()){
			f.setSpecial(food4Status.isSpecial());
		}
		if(builder.isHotChanged()){
			f.setHot(food4Status.isHot());
		}
		if(builder.isWeightChanged()){
			f.setWeigh(food4Status.isWeight());
		}
		if(builder.isCommissionChanged()){
			f.setCommission(food4Status.isCommission());
		}
		if(builder.isComboChanged()){
			f.setCombo(food4Status.isCombo());
		}
		if(builder.isLimitChanged()){
			f.setLimit(food4Status.isLimit());
			if(food4Status.isLimit()){
				f.setLimitAmount(food4Status.getLimitAmount());
			}
		}
		f.setTemp(false);
		
		//Update the oss image.
		if(builder.isImageChanged()){
			if(f.hasImage()){
				//Married the oss image with this food.
				try{
					OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(f.getImage().getId()).setSingleAssociated(OssImage.Type.FOOD_IMAGE, f.getFoodId()));
				}catch(IOException ignored){}
				//Update the oss image id.
				sql = " UPDATE " + Params.dbName + ".food SET " +
				      " oss_image_id = " + f.getImage().getId() +
					  " WHERE food_id = " + f.getFoodId();
				dbCon.stmt.executeUpdate(sql);
			}else{
				//Delete the associated oss image.
				OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.FOOD_IMAGE, f.getFoodId()));
				//Update the oss image id to zero.
				sql = " UPDATE " + Params.dbName + ".food SET " +
				      " oss_image_id = 0 " +
					  " WHERE food_id = " + f.getFoodId();
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		//Update the price plan.
		if(builder.isPricePlanChanged()){
			for(Map.Entry<PricePlan, Float> entry : f.getPricePlan().entrySet()){
				//Check to see whether or not the price plan exist. 
				PricePlanDao.getById(dbCon, staff, entry.getKey().getId());
				//Insert or update the price to this plan. 
				if(getPricePlan(dbCon, staff, new ExtraCond4Price(f)).isEmpty()){
					sql = " INSERT INTO " + Params.dbName + ".food_price_plan" +
						  " (food_id, price_plan_id, price) VALUES (" +
						  f.getFoodId() + "," +
						  entry.getKey().getId() + "," +
						  entry.getValue().floatValue() +
						  ")";
					dbCon.stmt.executeUpdate(sql);
				}else{
					sql = " UPDATE " + Params.dbName + ".food_price_plan SET " +
						  " price = " + entry.getValue().floatValue() +
						  " WHERE food_id = " + f.getFoodId() +
						  " AND price_plan_id = " + entry.getKey().getId();
					dbCon.stmt.executeUpdate(sql);
				}
			}
		}
		
		//Update the food unit.
		if(builder.isFoodUnitChanged()){
			FoodUnitDao.deleteByCond(dbCon, staff, new FoodUnitDao.ExtraCond().addFood(f.getFoodId()));
			for(FoodUnit unit : f.getFoodUnits()){
				FoodUnitDao.insert(dbCon, staff, new FoodUnit.InsertBuilder(f.getFoodId(), unit.getPrice(), unit.getUnit()));
			}
		}
		
		sql = " UPDATE " + Params.dbName + ".food SET " +
			  " food_id = " + f.getFoodId() +
			  " ,status = " + f.getStatus() +
			  (builder.isLimitChanged() ? ",limit_amount = " + (f.isLimit() ? f.getLimitAmount() : "NULL") : "") +
			  (builder.isLimitChanged() && !f.isLimit() ? ",limit_remaing = NULL " : "") +
			  (builder.isAliasChanged() ? ",food_alias = " + f.getAliasId() : "") +
			  (builder.isNameChanged() ? ",name = '" + f.getName() + "'" : "") +
			  (builder.isKitchenChanged() ? ",kitchen_id = " + f.getKitchen().getId() : "") +
			  (builder.isPriceChanged() ? ",price = " + f.getPrice() : "") +
			  (builder.isCommissionChanged() ? ",commission = " + f.getCommission() : "") +
			  (builder.isDescChanged() ? ",`desc` = '" + f.getDesc() + "'" : "") +
			  " WHERE food_id = " + f.getFoodId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}

	}
	
	/**
	 * Update the limit remaining to specific builder {@link Food#LimitRemainingBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the limit remaining builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the food to update does NOT exist
	 * 			<li>the food does NOT belong to be limited
	 * 			<li>the limit remaining exceeds the amount
	 */
	public static void update(Staff staff, Food.LimitRemainingBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the limit remaining to specific builder {@link Food#LimitRemainingBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the limit remaining builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the food to update does NOT exist
	 * 			<li>the food does NOT belong to be limited
	 * 			<li>the limit remaining exceeds the amount
	 */
	public static void update(DBCon dbCon, Staff staff, Food.LimitRemainingBuilder builder) throws SQLException, BusinessException{
		int remaining = builder.build().getLimitRemaing();
		Food food = getById(dbCon, staff, builder.build().getFoodId());
		if(!food.isLimit()){
			throw new BusinessException("【" + food.getName() + "】不是限量估清菜品");
		}
		if(remaining > food.getLimitAmount()){
			throw new BusinessException("【" + food.getName() + "】的剩余数量不能大于限量数");
		}
		if(remaining > 0){
			update(dbCon, staff, new Food.UpdateBuilder(food.getFoodId()).setSellOut(false));
			
		}else if(remaining == 0){
			update(dbCon, staff, new Food.UpdateBuilder(food.getFoodId()).setSellOut(true));
		}
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".food SET limit_remaing = " + remaining + " WHERE food_id = " + food.getFoodId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
	}
	
	/**
	 * Build the combo according to a specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the combo builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the parent or any child food does NOT exist
	 */
	public static void buildCombo(Staff staff, Food.ComboBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			buildCombo(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Build the combo according to a specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the combo builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either cases below<br>
	 * 			<li>the parent or any child food does NOT exist
	 * 			<li>and child food belongs to combo
	 */
	public static void buildCombo(DBCon dbCon, Staff staff, Food.ComboBuilder builder) throws SQLException, BusinessException{
		Food f = builder.build();
		
		Food parent = getPureById(dbCon, staff, f.getFoodId());
		for(ComboFood child : f.getChildFoods()){
			if(child.asFood().isCombo()){
				throw new BusinessException(FoodError.CHILD_FOOD_CAN_NOT_BE_COMBO);
			}
			parent.addChildFood(child);
		}
		
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + parent.getFoodId();
		dbCon.stmt.executeUpdate(sql);
		
		if(parent.hasChildFoods()){
			update(dbCon, staff, new Food.UpdateBuilder(parent.getFoodId()).setCombo(true));
			
			for(ComboFood child : parent.getChildFoods()){
				sql = " INSERT INTO " + Params.dbName + ".combo" +
					  " (`food_id`, `sub_food_id`, `restaurant_id`, `amount`) VALUES(" +
					  parent.getFoodId() + "," +
					  child.asFood().getFoodId() + "," +
					  staff.getRestaurantId() + "," +
					  child.getAmount() +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}else{
			update(dbCon, staff, new Food.UpdateBuilder(parent.getFoodId()).setCombo(false));
		}

	}
	
	/**
	 * Get the food and its related information to the specified restaurant and id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return	the food to the specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 */	
	public static Food getById(DBCon dbCon, Staff staff, int foodId) throws SQLException, BusinessException{
		List<Food> result = FoodDao.getByCond(dbCon, staff, new ExtraCond().setId(foodId), null);
		if(result.isEmpty()){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}else{
			Food food = result.get(0);
			//Get the detail of oss image to this food.
			if(food.hasImage()){
				food.setImage(OssImageDao.getById(dbCon, staff, food.getImage().getId()));
			}
			food.setPricePlan(getPricePlan(dbCon, staff, new ExtraCond4Price(food)));
			return food;
		}
	}

	/**
	 * Get the food and its related information to the specified restaurant and id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return	the food to the specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 */
	public static Food getById(Staff staff, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, foodId);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the child foods to a specific parent food.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond4Combo}
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ComboFood> getComboByCond(DBCon dbCon, Staff staff, ExtraCond4Combo extraCond) throws SQLException{
		
		List<ComboFood> result = new ArrayList<ComboFood>();
		
		String sql;

		sql = " SELECT sub_food_id FROM " + Params.dbName + ".combo WHERE 1 = 1 " + extraCond;
		
		for(Food subFood : getPureByCond(dbCon, staff, extraCond.extraCond(), null)){
			
			sql = " SELECT amount FROM " + Params.dbName + ".combo WHERE food_id = " + extraCond.parentId + " AND sub_food_id = " + subFood.getFoodId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int amount;
			if(dbCon.rs.next()){
				amount = dbCon.rs.getInt("amount");
			}else{
				amount = 0;
			}
			dbCon.rs.close();
			
			result.add(new ComboFood(subFood, amount));
		}
		
		return result;
				
	}

	/**
	 * Get the child foods to a specific parent food.
	 * @param parentId
	 * 			the id to parent food
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond4Combo}
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ComboFood> getComboByCond(Staff staff, ExtraCond4Combo extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getComboByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Query the food according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureByCond(DBCon dbCon, ExtraCond extraCondition, String orderClause) throws SQLException{
		List<Food> foods = new ArrayList<Food>();
	    //get all the food information to this restaurant
		String sql = " SELECT " +
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, " +
					 " FOOD.name, FOOD.price, FOOD.commission, FOOD.status, FOOD.desc, " +
					 " FOOD.order_amount, FOOD.limit_amount, FOOD.limit_remaing, " +
					 " KITCHEN.kitchen_id, KITCHEN.display_id AS kitchen_display_id, KITCHEN.name AS kitchen_name, " +
					 " KITCHEN.type AS kitchen_type , KITCHEN.is_allow_temp AS is_allow_temp, " +
					 " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type, DEPT.display_id AS dept_display_id, " +
					 " OI.oss_image_id, OI.image, OI.type AS oss_image_type, " +
					 " TOI.oss_image_id AS oss_thumbnail_id, TOI.image AS oss_thumbnail_image, TOI.type AS oss_thumbnail_type " +
					 " FROM " + Params.dbName + ".food FOOD " +
					 " JOIN " + Params.dbName + ".kitchen KITCHEN ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
					 " JOIN " + Params.dbName + ".department DEPT ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " +
					 " LEFT JOIN " + Params.dbName + ".oss_image OI ON FOOD.oss_image_id = OI.oss_image_id " +
					 " LEFT JOIN " + Params.dbName + ".oss_image TOI ON OI.oss_thumbnail_id = TOI.oss_image_id " +
					 " WHERE 1 = 1 " +
					 (extraCondition == null ? "" : extraCondition.toString()) + " " +
					 (orderClause == null ? "" : orderClause); 
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
	
			int foodId = dbCon.rs.getInt("food_id");
			int restaurantId = dbCon.rs.getInt("restaurant_id");
			
			Food f = new Food(foodId);
			f.setRestaurantId(restaurantId);
			f.setAliasId(dbCon.rs.getInt("food_alias"));
			f.setName(dbCon.rs.getString("name"));
			
			//Generate the pinyin to each food
			f.setPinyin(PinyinUtil.cn2Spell(f.getName()));
			f.setPinyinShortcut(PinyinUtil.cn2FirstSpell(f.getName()));
			
			f.setPrice(dbCon.rs.getFloat("price"));
			f.setCommission(dbCon.rs.getFloat("commission"));
			f.setStatistics(new FoodStatistics(dbCon.rs.getInt("order_amount")));
			f.setStatus(dbCon.rs.getShort("status"));
			f.setLimitAmount(dbCon.rs.getInt("limit_amount"));
			f.setLimitRemaing(dbCon.rs.getInt("limit_remaing"));
			f.setDesc(dbCon.rs.getString("desc"));
			int ossImageId = dbCon.rs.getInt("oss_image_id");
			if(ossImageId != 0){
				OssImage image = new OssImage(ossImageId);
				image.setImage(dbCon.rs.getString("image"));
				image.setType(OssImage.Type.valueOf(dbCon.rs.getInt("oss_image_type")));
				image.setRestaurantId(restaurantId);
				int thumbnailId = dbCon.rs.getInt("oss_thumbnail_id");
				if(thumbnailId != 0){
					OssImage thumbnail = new OssImage(thumbnailId);
					thumbnail.setImage(dbCon.rs.getString("oss_thumbnail_image"));
					thumbnail.setType(OssImage.Type.valueOf(dbCon.rs.getInt("oss_thumbnail_type")));
					thumbnail.setRestaurantId(restaurantId);
					image.setThumbnail(thumbnail);
				}
				f.setImage(image);
			}
			f.setKitchen(new Kitchen.QueryBuilder(dbCon.rs.getInt("kitchen_id"), dbCon.rs.getString("kitchen_name")) 
									.setDisplayId(dbCon.rs.getInt("kitchen_display_id"))
	 				   				.setRestaurantId(restaurantId)
									.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
									.setType(dbCon.rs.getShort("kitchen_type"))
									.setDept(new Department(dbCon.rs.getString("dept_name"), 
 				   				    		   		  	    dbCon.rs.getShort("dept_id"), 
 				   				    		   		  	    restaurantId,
 				   				    		   		  	    Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
 				   				    		   		  	    dbCon.rs.getInt("dept_display_id")))
 			   				        .build());
			
			foods.add(f);
		}
		
		dbCon.rs.close();
		
		return foods;
	}

	/**
	 * Query the food according to extra condition.
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureByCond(ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByCond(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Query the foods to the specified restaurant defined in staff {@link Staff} according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		return getPureByCond(dbCon, extraCond != null ? extraCond.setRestaurant(staff.getRestaurantId()) : new ExtraCond().setRestaurant(staff.getRestaurantId()), orderClause);
	}

	/**
	 * Query the foods to the specified restaurant defined in staff {@link Staff} according to extra condition.
	 * @param staff
	 * 			the staff to perform this action 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the pure food to the specified restaurant and id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return the food to specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 * 			
	 */
	public static Food getPureById(DBCon dbCon, Staff staff, int foodId) throws SQLException, BusinessException{
		List<Food> result = getPureByCond(dbCon, staff, new ExtraCond().setId(foodId), null);
		if(!result.isEmpty()){
			return result.get(0);
		}else{
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
	}

	/**
	 * Get the pure food to the specified restaurant and id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return the food to specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 * 			
	 */
	public static Food getPureById(Staff staff, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureById(dbCon, staff, foodId);
		}finally{
			dbCon.disconnect();
		}
	}

	
	/**
	 * Get the food and its related information according to extra condition {@link ExtraCond} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition to SQL statement
	 * @param order clause
	 * 			the order clause to SQL statement
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */			
	public static List<Food> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{

		//Using link hash map to keep original order after retrieving the foods by order clause defined in SQL statement.
		Map<Integer, Food> foods = new LinkedHashMap<Integer, Food>();
		
		if(orderClause == null){
			orderClause = " ORDER BY FOOD.food_alias, FOOD.food_id ";
		}
		//Get the basic detail to each food.
		List<Food> pureFoods = getPureByCond(dbCon, staff, extraCond, orderClause);

		StringBuilder foodCond = new StringBuilder();
		FoodUnitDao.ExtraCond extraCond4Unit = new FoodUnitDao.ExtraCond();
		
		for(Food f : pureFoods){
			foods.put(f.getFoodId(), f);
			if(foodCond.length() == 0){
				foodCond.append(f.getFoodId());
			}else{
				foodCond.append(",").append(f.getFoodId());
			}
			
			//Generate the pinyin to each food
			f.setPinyin(PinyinUtil.cn2Spell(f.getName()));
			f.setPinyinShortcut(PinyinUtil.cn2FirstSpell(f.getName()));
			
			extraCond4Unit.addFood(f);
		}
		
		if(foodCond.length() > 0){
			//Get the associated popular tastes to each food.
			String sql;
			sql = " SELECT " +
			      " FTR.food_id, " + 
				  " TASTE.taste_id, TASTE.restaurant_id " +
				  " FROM " + Params.dbName + ".food_taste_rank FTR " +
				  " JOIN " + Params.dbName + ".taste TASTE " +
				  " ON TASTE.taste_id = FTR.taste_id " + 
				  " WHERE " + " FTR.food_id IN(" + foodCond + ")" +
				  " ORDER BY FTR.rank ";
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Food f = foods.get(dbCon.rs.getInt("food_id"));
				if(f != null){
					Taste t = new Taste(dbCon.rs.getInt("taste_id"));
					t.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
					f.addPopTaste(t);
				}
			}
			dbCon.rs.close();
			
			//Get the associated food units to each food.
			for(final FoodUnit foodUnit : FoodUnitDao.getByCond(dbCon, staff, extraCond4Unit)){
				Food f = foods.get(foodUnit.getFoodId());
				if(f != null){
					f.addFoodUnit(foodUnit);
				}
			}
		}
		
		//Get the combo detail to each food if belongs to combo. 
		for(Entry<Integer, Food> entry : foods.entrySet()){
			if(entry.getValue().isCombo()){
				entry.getValue().setChildFoods(getComboByCond(dbCon, staff, new ExtraCond4Combo(entry.getValue().getFoodId())));
			}
		}
		
		return new ArrayList<Food>(foods.values());
		
	}

	/**
	 * Get the food and its related information to the specified restaurant defined in staff {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param order clause
	 * 			the order clause to SQL statement
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */	
	public static List<Food> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the price plan to extra condition {@link ExtraCond4Price}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond4Price}
	 * @return the result to price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Map<PricePlan, Float> getPricePlan(Staff staff, ExtraCond4Price extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPricePlan(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}		
	}
	
	/**
	 * Get the price plan to extra condition {@link ExtraCond4Price}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond4Price}
	 * @return the result to price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Map<PricePlan, Float> getPricePlan(DBCon dbCon, Staff staff, ExtraCond4Price extraCond) throws SQLException{
		String sql;
		if(extraCond.type == ExtraCond4Price.ShowType.BY_PLAN){
			sql = " SELECT PP.name, PP.type, FPP.price_plan_id, IFNULL(FPP.price, -1) AS price FROM " + Params.dbName + ".price_plan PP" +
				  " LEFT JOIN " + Params.dbName + ".food_price_plan FPP ON PP.price_plan_id = FPP.price_plan_id " +
				  " WHERE PP.restaurant_id = " + staff.getRestaurantId() +
				  (extraCond != null ? extraCond.toString() : "");
		}else{
			sql = " SELECT name, type, FPP.price_plan_id, price FROM " + Params.dbName + ".food_price_plan FPP" +
				  " JOIN " + Params.dbName + ".price_plan PP ON PP.price_plan_id = FPP.price_plan_id " +
				  " WHERE 1 = 1 " +
				  " AND PP.restaurant_id = " + staff.getRestaurantId() +
				  (extraCond != null ? extraCond.toString() : "");
		}
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<PricePlan, Float> result = new HashMap<>();
		while(dbCon.rs.next()){
			PricePlan pp = new PricePlan(dbCon.rs.getInt("price_plan_id"));
			pp.setName(dbCon.rs.getString("name"));
			pp.setType(PricePlan.Type.valueOf(dbCon.rs.getInt("type")));
			if(dbCon.rs.getFloat("price") >= 0){
				result.put(pp, dbCon.rs.getFloat("price"));
			}else{
				result.put(pp, null);
			}
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Restore the food limit amount.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount to food restored
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int restoreLimit(DBCon dbCon, Staff staff) throws SQLException{
		int amount = 0;
		for(Food f : FoodDao.getPureByCond(dbCon, staff, new FoodDao.ExtraCond().setLimit(true), null)){
			try{
				FoodDao.update(dbCon, staff, new Food.LimitRemainingBuilder(f, f.getLimitAmount()));
				amount++;
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}
		return amount;
	}
	
	/**
	 * Get the food which not be the good
	 * @param staff
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Food> selectToBeFood(Staff staff, String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return selectToBeFood(dbCon, staff, extraCond, otherClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	public static List<Food> selectToBeFood(DBCon dbCon, Staff staff, String extraCond, String otherClause) throws SQLException{
		List<Food> foods = new ArrayList<>();
		
		String sql = "SELECT F.food_id, F.name FROM " + Params.dbName + ".food F WHERE F.restaurant_id = " + staff.getRestaurantId()
					+  (extraCond == null ? "" : extraCond) + " " 
					+ " AND F.food_id  NOT IN (SELECT FM.food_id FROM " + Params.dbName + ".food_material FM WHERE FM.restaurant_id = " + staff.getRestaurantId() 
					+ " AND FM.food_id = F.food_id)";
		
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while (dbCon.rs != null && dbCon.rs.next()) {
				Food f = new Food(dbCon.rs.getInt("food_id"));
				f.setName(dbCon.rs.getString("name"));
				foods.add(f);
			}
		}catch(SQLException e){
			e.printStackTrace();
			throw new SQLException("Failed to get the food");
		}
		return foods;
	}	
	
	/**
	 * Get the food content By materialId
	 * @param staff
	 * @param foodId
	 * @return
	 * @throws SQLException
	 */
	public static Food relativeToFood(Staff staff, int materialId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return relativeToFood(dbCon, staff, materialId);
		}finally{
			dbCon.disconnect();
		}
		
	}	
	
	public static Food relativeToFood(DBCon dbCon, Staff staff, int materialId) throws SQLException{
		Food food = null;
		String sql = "SELECT FM.food_id,  F.name AS foodName, F.kitchen_id, K.name AS kitchenName FROM food_material FM "
					+ " JOIN food F ON FM.food_id = F.food_id " 
					+ " JOIN kitchen K ON K.kitchen_id = F.kitchen_id " 
					+ " WHERE FM.restaurant_id = " + staff.getRestaurantId() 
					+ " AND FM.material_id = " + materialId;
		
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while (dbCon.rs != null && dbCon.rs.next()) {
				food = new Food(dbCon.rs.getInt("food_id"));
				food.setName(dbCon.rs.getString("foodName"));
				Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
				k.setName(dbCon.rs.getString("kitchenName"));
				food.setKitchen(k);
			}
		}catch(SQLException e){
			e.printStackTrace();
			throw new SQLException("Failed to get the food");
		}
		return food;
	}	
	
	
	
}
