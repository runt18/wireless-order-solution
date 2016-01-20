package com.wireless.db.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.MemberType.DiscountType;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Staff;

public class MemberTypeDao {

	public static class ExtraCond{
		private int id;
		private MemberType.Type type;
		private MemberType.Attribute attribute;
		private String name;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setType(MemberType.Type type){
			this.type = type;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setAttribute(MemberType.Attribute attribute){
			this.attribute = attribute;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND MT.member_type_id = " + id);
			}
			if(type != null){
				extraCond.append(" AND MT.type = " + type.getVal());
			}
			if(name != null){
				extraCond.append(" AND MT.name like '%" + name.trim() + "%'");
			}
			if(attribute != null){
				extraCond.append(" AND MT.attribute = " + attribute.getVal());
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new member type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new member type
	 * @return the id to member type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, MemberType.InsertBuilder builder) throws SQLException{
		MemberType mt = builder.build();
		// 插入新数据
		String sql = " INSERT INTO " + Params.dbName + ".member_type " 
				   + " ( `restaurant_id`, `name`, `type`, `exchange_rate`, `charge_rate`, `attribute`, `initial_point`, `desc` )"
				   + " VALUES("
				   + staff.getRestaurantId() + ","	
				   + "'" + mt.getName() + "',"
				   + mt.getType().getVal() + "," +
				   + mt.getExchangeRate() + ","	
				   + mt.getChargeRate() + "," 
				   + mt.getAttribute().getVal() + "," 
				   + mt.getInitialPoint() + "," +
				   "'" + mt.getDesc() + "'" +
				   ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			mt.setId(dbCon.rs.getInt(1));
		}else{
			throw new SQLException("Failed to generated the member type id.");
		}
		
		//Insert the discounts associated with this member type.
		for(Discount discount : mt.getDiscounts()){
			sql = " INSERT INTO " + Params.dbName + ".member_type_discount" +
				  " (`member_type_id`, `discount_id`, `type`) " +
				  " VALUES (" +
				  mt.getId() + "," +
				  discount.getId() + "," +
				  MemberType.DiscountType.NORMAL.getVal() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}

		//Update the default discount.
		sql = " UPDATE " + Params.dbName + ".member_type_discount" +
			  " SET type = " + MemberType.DiscountType.DEFAULT.getVal() +
			  " WHERE member_type_id = " + mt.getId() +
			  " AND discount_id = " + mt.getDefaultDiscount().getId();
		dbCon.stmt.executeUpdate(sql);

		//Insert the price plans associated with this member type.
		for(PricePlan plan : mt.getPrices()){
			sql = " INSERT INTO " + Params.dbName + ".member_type_price" +
				  " (`member_type_id`, `price_plan_id`, `type`) VALUES (" +
				  mt.getId() + "," +
				  plan.getId() + "," +
				  MemberType.PriceType.NORMAL.getVal() +
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}

		if(mt.hasDefaultPrice()){
			//Update the default price plan.
			sql = " UPDATE " + Params.dbName + ".member_type_price" +
				  " SET type = " + MemberType.PriceType.DEFAULT.getVal() +
				  " WHERE member_type_id = " + mt.getId() +
				  " AND price_plan_id = " + mt.getDefaultPrice().getId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Update the chain discounts.
		for(MemberType.Discount4Chain chainDiscount : builder.getChainDiscounts()){
			for(Discount discount : chainDiscount.getDiscounts()){
				sql = " INSERT INTO " + Params.dbName + ".member_chain_discount " +
					  " (group_member_type_id, branch_id, discount_id, type) VALUES ( " +
					  mt.getId() + "," +
					  chainDiscount.getBranchId() + "," +
					  discount.getId() + "," +
					  DiscountType.NORMAL.getVal() + 
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
			sql = " UPDATE " + Params.dbName + ".member_chain_discount SET type = " + DiscountType.DEFAULT.getVal() + 
				  " WHERE group_member_type_id = " + mt.getId() +
				  " AND branch_id = " + chainDiscount.getBranchId() +
				  " AND discount_id = " + chainDiscount.getDefaultDiscount().getId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Update the chain prices.
		for(MemberType.Price4Chain chainPrice : builder.getChainPrices()){
			for(PricePlan plan : chainPrice.getPrices()){
				sql = " INSERT INTO " + Params.dbName + ".member_chain_price " +
					  " (group_member_type_id, branch_id, price_plan_id, type) VALUES ( " +
					  mt.getId() + "," +
					  chainPrice.getBranchId() + "," +
					  plan.getId() + "," +
					  MemberType.PriceType.NORMAL.getVal() + 
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
			sql = " UPDATE " + Params.dbName + ".member_chain_price SET type = " + MemberType.PriceType.DEFAULT.getVal() + 
				  " WHERE group_member_type_id = " + mt.getId() +
				  " AND branch_id = " + chainPrice.getBranchId() +
				  " AND price_plan_id = " + chainPrice.getDefaultPrice().getId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Update the chain prices.
		for(MemberType.Price4Chain chainPrice : builder.getChainPrices()){
			for(PricePlan plan : chainPrice.getPrices()){
				sql = " INSERT INTO " + Params.dbName + ".member_chain_price " +
					  " (group_member_type_id, branch_id, price_plan_id, type) VALUES ( " +
					  mt.getId() + "," +
					  chainPrice.getBranchId() + "," +
					  plan.getId() + "," +
					  MemberType.PriceType.NORMAL.getVal() + 
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
			sql = " UPDATE " + Params.dbName + ".member_chain_price SET type = " + MemberType.PriceType.DEFAULT.getVal() + 
				  " WHERE group_member_type_id = " + mt.getId() +
				  " AND branch_id = " + chainPrice.getBranchId() +
				  " AND price_plan_id = " + chainPrice.getDefaultPrice().getId();
			dbCon.stmt.executeUpdate(sql);
		}
		return mt.getId();
	}
	
	/**
	 * Insert a new member type.
	 * @param builder
	 * 			the builder to insert a new member type
	 * @param staff
	 * 			the staff to perform this action
	 * @return the id to member type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, MemberType.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = insert(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * Delete a member type according to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param memberTypeId
	 * 			the member type id to delete
	 * @param staff
	 * 			the staff to perform this action
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int memberTypeId) throws BusinessException, SQLException{
		String sql;
		
		//Check to see any member associated with this type exist.
		sql = "SELECT COUNT(*) FROM " + Params.dbName + ".member WHERE restaurant_id = " + staff.getRestaurantId() + " AND member_type_id = " + memberTypeId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt(1) > 0){
				throw new BusinessException(MemberError.TYPE_DELETE_FAIL_BECAUSE_MEMBER_NOT_EMPTY);
			}
		}
		dbCon.rs.close();
		
		//Check to see whether the member level related to this type is in used.
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member_level WHERE member_type_id = " + memberTypeId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt(1) > 0){
				throw new BusinessException(MemberError.TYPE_DELETE_FAIL_BECAUSE_LEVEL_IN_USED);
			}
		}
		dbCon.rs.close();
		
		//Delete the member level associated with this member type
		sql = " DELETE FROM " + Params.dbName + ".member_level WHERE member_type_id = " + memberTypeId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the discounts associated with this member type.
		sql = " DELETE FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + memberTypeId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the price plans associated with this member type.
		sql = " DELETE FROM " + Params.dbName + ".member_type_price WHERE member_type_id = " + memberTypeId;
		dbCon.stmt.executeUpdate(sql);
		
		if(staff.isGroup()){
			//Delete the chain discounts associated with this member type.
			sql = " DELETE FROM " + Params.dbName + ".member_chain_discount " + 
				  " WHERE 1 = 1 " +
				  " AND group_member_type_id = " + memberTypeId +
				  " AND branch_id IN ( SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + staff.getRestaurantId() + ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the chain price plans associated with this member type.
			sql = " DELETE FROM " + Params.dbName + ".member_chain_price " + 
				  " WHERE 1 = 1 " +
				  " AND group_member_type_id = " + memberTypeId +
				  " AND branch_id IN ( SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + staff.getRestaurantId() + ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Delete the member type.
		sql = " DELETE FROM " + Params.dbName + ".member_type WHERE member_type_id = " + memberTypeId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.TYPE_DELETE_FAIL_BECAUSE_MEMBER_NOT_EMPTY);
		}
	}
	
	/**
	 * Delete a member type according to specific id
	 * @param memberTypeId
	 * 			the member type id to delete
	 * @param staff
	 * 			the staff to perform this action
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(Staff staff, int memberTypeId) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, staff, memberTypeId);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Update the member type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a member type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, MemberType.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		MemberType mt = builder.build();
		// 更新数据
		sql = " UPDATE " + Params.dbName + ".member_type SET " +
			  " member_type_id = " + mt.getId() +
			  (builder.isNameChanged() ? " ,name = '" + mt.getName() + "'" : "") +
			  (builder.isExchangRateChanged() ? " ,exchange_rate = " + mt.getExchangeRate() : "") +
			  (builder.isChargeRateChanged() ? " ,charge_rate = " + mt.getChargeRate() : "") + 
			  (builder.isAttributeChanged() ? " ,attribute = " + mt.getAttribute().getVal() : "") + 
			  (builder.isInitialPointChanged() ? " ,initial_point = " + mt.getInitialPoint() : "") +
			  (builder.isDescChanged() ? ",`desc` = '" + mt.getDesc() + "'" : "") +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  " AND member_type_id = " + mt.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.MEMBER_TYPE_NOT_EXIST);
		}
		
		if(builder.isDiscountChanged() || builder.isDefaultDiscountChanged()){
			sql = " DELETE FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + mt.getId();
			dbCon.stmt.executeUpdate(sql); 
			
			//Insert the discounts associated with this member type.
			for(Discount discount : mt.getDiscounts()){
				sql = " INSERT INTO " + Params.dbName + ".member_type_discount" +
					  " (`member_type_id`, `discount_id`, `type`) " +
					  " VALUES (" +
					  mt.getId() + "," +
					  discount.getId() + "," +
					  MemberType.DiscountType.NORMAL.getVal() +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}

			//Update the default discount.
			sql = " UPDATE " + Params.dbName + ".member_type_discount" +
				  " SET type = " + MemberType.DiscountType.DEFAULT.getVal() +
				  " WHERE member_type_id = " + mt.getId() +
				  " AND discount_id = " + mt.getDefaultDiscount().getId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		if(builder.isPriceChanged() || builder.isDefaultPriceChanged()){
			sql = " DELETE FROM " + Params.dbName + ".member_type_price WHERE member_type_id = " + mt.getId();
			dbCon.stmt.executeUpdate(sql); 
			
			//Insert the price plan associated with this member type.
			for(PricePlan price : mt.getPrices()){
				sql = " INSERT INTO " + Params.dbName + ".member_type_price" +
					  " (`member_type_id`, `price_plan_id`, `type`) " +
					  " VALUES (" +
					  mt.getId() + "," +
					  price.getId() + "," +
					  MemberType.PriceType.NORMAL.getVal() +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}

			if(mt.hasDefaultPrice()){
				//Update the default price plan.
				sql = " UPDATE " + Params.dbName + ".member_type_price" +
					  " SET type = " + MemberType.PriceType.DEFAULT.getVal() +
					  " WHERE member_type_id = " + mt.getId() +
					  " AND price_plan_id = " + mt.getDefaultPrice().getId();
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		//Update the chain discounts.
		if(builder.isChainDiscountChanged() && staff.isGroup()){
			sql = " DELETE FROM " + Params.dbName + ".member_chain_discount " + 
				  " WHERE 1 = 1 " +
				  " AND group_member_type_id = " + mt.getId() +
				  " AND branch_id IN ( SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + staff.getRestaurantId() + ")";
			dbCon.stmt.executeUpdate(sql);
			
			for(MemberType.Discount4Chain chainDiscount : builder.getChainDiscounts()){
				//Check to see whether the branch belongs to the group.
				sql = " SELECT COUNT(*) FROM " + Params.dbName + ".restaurant_chain WHERE branch_id = " + chainDiscount.getBranchId() + " AND group_id = " + staff.getRestaurantId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					if(dbCon.rs.getInt(1) == 0){
						continue;
					}
				}
				dbCon.rs.close();
				
				for(Discount discount : chainDiscount.getDiscounts()){
					sql = " INSERT INTO " + Params.dbName + ".member_chain_discount " +
						  " (group_member_type_id, branch_id, discount_id, type) VALUES ( " +
						  mt.getId() + "," +
						  chainDiscount.getBranchId() + "," +
						  discount.getId() + "," +
						  DiscountType.NORMAL.getVal() + 
						  ")";
					dbCon.stmt.executeUpdate(sql);
				}
				sql = " UPDATE " + Params.dbName + ".member_chain_discount SET type = " + DiscountType.DEFAULT.getVal() + 
					  " WHERE group_member_type_id = " + mt.getId() + 
					  " AND branch_id = " + chainDiscount.getBranchId() +
					  " AND discount_id = " + chainDiscount.getDefaultDiscount().getId();
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		//Update the chain prices.
		if(builder.isChainPricesChanged() && staff.isGroup()){
			sql = " DELETE FROM " + Params.dbName + ".member_chain_price " + 
				  " WHERE 1 = 1 " +
				  " AND group_member_type_id = " + mt.getId() +
				  " AND branch_id IN ( SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + staff.getRestaurantId() + ")";
			dbCon.stmt.executeUpdate(sql);
			
			for(MemberType.Price4Chain chainPrice : builder.getChainPrices()){
				//Check to see whether the branch belongs to the group.
				sql = " SELECT COUNT(*) FROM " + Params.dbName + ".restaurant_chain WHERE branch_id = " + chainPrice.getBranchId() + " AND group_id = " + staff.getRestaurantId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					if(dbCon.rs.getInt(1) == 0){
						continue;
					}
				}
				dbCon.rs.close();
				
				for(PricePlan pp : chainPrice.getPrices()){
					sql = " INSERT INTO " + Params.dbName + ".member_chain_price " +
						  " (group_member_type_id, branch_id, price_plan_id, type) VALUES ( " +
						  mt.getId() + "," +
						  chainPrice.getBranchId() + "," +
						  pp.getId() + "," +
						  DiscountType.NORMAL.getVal() + 
						  ")";
					dbCon.stmt.executeUpdate(sql);
				}
				sql = " UPDATE " + Params.dbName + ".member_chain_price SET type = " + MemberType.PriceType.DEFAULT.getVal() + 
					  " WHERE group_member_type_id = " + mt.getId() + 
					  " AND branch_id = " + chainPrice.getBranchId() +
					  " AND price_plan_id = " + chainPrice.getDefaultPrice().getId();
				dbCon.stmt.executeUpdate(sql);
			}
		}
	}
	
	/**
	 * Update the member type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a member type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type to update does NOT exist
	 */
	public static void update(Staff staff, MemberType.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberTypeDao.update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type according to specific extra condition and order clause.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause	
	 * 			the order clause
	 * @return the result list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any discount associated with the member type is NOT found
	 */
	public static List<MemberType> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		final List<MemberType> result = new ArrayList<MemberType>();
		String sql;
		sql = " SELECT " +
			  " member_type_id, restaurant_id, exchange_rate, charge_rate, name, attribute, initial_point, type, `desc` " +
			  " FROM " + Params.dbName + ".member_type MT " +
			  " WHERE 1 = 1 " +
			  " AND MT.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond.toString() : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			MemberType mt = new MemberType(dbCon.rs.getInt("member_type_id"));
			mt.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mt.setName(dbCon.rs.getString("name"));
			mt.setType(MemberType.Type.valueOf(dbCon.rs.getInt("type")));
			mt.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			mt.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			mt.setAttribute(dbCon.rs.getInt("attribute"));
			mt.setInitialPoint(dbCon.rs.getInt("initial_point"));
			mt.setDesc(dbCon.rs.getString("desc"));
			result.add(mt);
		}
		dbCon.rs.close();
		
		for(MemberType eachType : result){
			if(staff.isBranch()){
				//Get the chain discount associated with this member type.
				sql = " SELECT discount_id, type FROM " + Params.dbName + ".member_chain_discount WHERE group_member_type_id = " + eachType.getId() + " AND branch_id = " + staff.getRestaurantId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					Discount distToMemberType = DiscountDao.getById(staff, dbCon.rs.getInt("discount_id"));
					eachType.addDiscount(distToMemberType);
					if(MemberType.DiscountType.valueOf(dbCon.rs.getInt("type")) == MemberType.DiscountType.DEFAULT){
						eachType.setDefaultDiscount(distToMemberType);
					}
				}
				dbCon.rs.close();
				
				//Get the chain price associated with this member type.
				sql = " SELECT price_plan_id, type FROM " + Params.dbName + ".member_chain_price WHERE group_member_type_id = " + eachType.getId() + " AND branch_id = " + staff.getRestaurantId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					PricePlan pp = PricePlanDao.getById(staff, dbCon.rs.getInt("price_plan_id"));
					eachType.addPricePlan(pp);
					if(MemberType.PriceType.valueOf(dbCon.rs.getInt("type")) == MemberType.PriceType.DEFAULT){
						eachType.setDefaultPrice(pp);
					}
				}
				dbCon.rs.close();
			}else{
				//Get the discount associated with this member type.
				sql = " SELECT discount_id, type FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + eachType.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					Discount distToMemberType = DiscountDao.getById(staff, dbCon.rs.getInt("discount_id"));
					eachType.addDiscount(distToMemberType);
					if(MemberType.DiscountType.valueOf(dbCon.rs.getInt("type")) == MemberType.DiscountType.DEFAULT){
						eachType.setDefaultDiscount(distToMemberType);
					}
				}
				dbCon.rs.close();
				
				//Get the price plan associated with this member type.
				sql = " SELECT price_plan_id, type FROM " + Params.dbName + ".member_type_price WHERE member_type_id = " + eachType.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					PricePlan price = PricePlanDao.getById(staff, dbCon.rs.getInt("price_plan_id"));
					eachType.addPricePlan(price);
					if(dbCon.rs.getInt("type") == MemberType.PriceType.DEFAULT.getVal()){
						eachType.setDefaultPrice(price);
					}
				}
				dbCon.rs.close();
			}

		}
		return result;
	}
	
	/**
	 * Get the member type according to specific extra condition and order clause.
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause	
	 * 			the order clause
	 * @return the result list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any discount associated with the member type is NOT found
	 */
	public static List<MemberType> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberTypeDao.getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type according to a specified id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the member type id
	 * @return	the member type to a specified id
	 * @throws SQLException
	 * 			throws if fails to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the member type to this specified id is NOT found
	 */
	public static MemberType getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<MemberType> list = MemberTypeDao.getByCond(dbCon, staff, new ExtraCond().setId(id), null);
		if(list.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_TYPE_NOT_EXIST);
		}else{
			return list.get(0);
		}
	}
	
	/**
	 * Get the member type according to a specified id
	 * @param id
	 * 			the member type id
	 * @param staff
	 * 			the staff to perform this action
	 * @return	the member type to a specified id
	 * @throws SQLException
	 * 			throws if fails to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the member type to this specified id is NOT found
	 */
	public static MemberType getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberTypeDao.getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type belongs to weixin.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the member type to weixin
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 */
	public static MemberType getWxMemberType(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getWxMemberType(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type belongs to weixin.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the member type to weixin
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 */
	public static MemberType getWxMemberType(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		List<MemberType> list = MemberTypeDao.getByCond(dbCon, staff, new ExtraCond().setType(MemberType.Type.WEIXIN), null);
		if(list.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_TYPE_NOT_EXIST);
		}else{
			return list.get(0);
		}
	}
	
	public static List<MemberType> getNotBelongMemberType(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getNotBelongMemberType(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<MemberType> getNotBelongMemberType(DBCon dbCon, Staff staff) throws SQLException{
		List<MemberType> list = new ArrayList<MemberType>();
		String sql = "SELECT member_type_id, name, attribute FROM " + Params.dbName + ".member_type WHERE restaurant_id = "+staff.getRestaurantId() +
					" AND member_type_id NOT IN (SELECT member_type_id FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + staff.getRestaurantId() + ")";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MemberType mType = new MemberType(-1);
			mType.setId(dbCon.rs.getInt("member_type_id"));
			mType.setName(dbCon.rs.getString("name"));
			mType.setAttribute(dbCon.rs.getInt("attribute"));
			list.add(mType);
		}
		return list;
	}
}
