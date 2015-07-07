package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TableError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.regionMgr.Table.Category;
import com.wireless.pojo.regionMgr.Table.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class TableDao {

	public static class ExtraCond{
		private int id;
		private int aliasId;
		private String name;
		private Region.RegionId regionId;
		private Table.Status status;
		private final List<Category> categorys = new ArrayList<Category>();
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setAliasId(int aliasId){
			this.aliasId = aliasId;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setStatus(Table.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setRegion(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setRegion(int regionId){
			this.regionId = Region.RegionId.valueOf(regionId);
			return this;
		}
		
		public ExtraCond addCategory(Category category){
			categorys.add(category);
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND TBL.table_id = " + id);
			}
			if(aliasId != 0){
				extraCond.append(" AND TBL.table_alias = " + aliasId);
			}
			if(name != null){
				extraCond.append(" AND TBL.name LIKE '%" + name + "%'");
			}
			if(regionId != null){
				extraCond.append(" AND REGION.region_id = " + regionId.getId());
			}
			StringBuilder categoryCond = new StringBuilder();
			for(Category category : categorys){
				if(categoryCond.length() == 0){
					categoryCond.append(category.getVal());
				}else{
					categoryCond.append(",").append(category.getVal());
				}
			}
			if(categoryCond.length() != 0){
				extraCond.append("AND TBL.category IN (" + categoryCond.toString() + ")");
			}
			if(status != null){
				if(status == Table.Status.BUSY){
					extraCond.append(" AND O.status IS NOT NULL ");
				}else if(status == Table.Status.IDLE){
					extraCond.append(" AND O.status IS NULL ");
				}
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get the table detail according to table id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getById(DBCon dbCon, Staff staff, int tableId) throws SQLException, BusinessException{
		
		List<Table> result = getByCond(dbCon, staff, new ExtraCond().setId(tableId), null);
		if(result.isEmpty()){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the table detail according to table id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getById(Staff staff, int tableId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, tableId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table detail according to table alias of a specified restaurant defined in terminal {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getByAlias(DBCon dbCon, Staff staff, int tableAlias) throws SQLException, BusinessException{
		List<Table> result = getByCond(dbCon, staff, new ExtraCond().setAliasId(tableAlias), null);
		if(result.isEmpty()){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the table detail according to table alias of a specified restaurant defined in terminal {@link Staff}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getByAlias(Staff staff, int tableAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByAlias(dbCon, staff, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the tables according to a specified restaurant defined in {@link Staff} and other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the table result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	private static List<Table> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		List<Table> result = new ArrayList<Table>();
		String sql;
		sql = " SELECT " +
			  " REGION.name AS region_name, REGION.region_id, " +
			  " TBL.restaurant_id, TBL.table_id, TBL.table_alias, TBL.name AS tbl_name, TBL.category, TBL.minimum_cost, " +
			  " O.custom_num, O.id, " + 
			  " IF(O.temp_date IS NULL, 0, 1) AS temp_paid, " +
			  " IF(O.status IS NULL, " + Table.Status.IDLE.getVal() + "," + Table.Status.BUSY.getVal() + ") AS tbl_status, " + 
			  " IF(BT.table_id IS NULL, 0, 1 ) AS book_flag " +
			  " FROM " + Params.dbName + ".table TBL " +
			  " LEFT JOIN " + Params.dbName + ".region REGION ON REGION.region_id = TBL.region_id AND REGION.restaurant_id = TBL.restaurant_id " +
			  " LEFT JOIN ( SELECT * FROM " + Params.dbName + ".order WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + Order.Status.UNPAID.getVal() + " GROUP BY table_id ) AS O ON O.table_id = TBL.table_id " +
			  " LEFT JOIN ( SELECT BT.table_id FROM " + Params.dbName + ".book_table BT " + 
			  				" JOIN " + Params.dbName + ".book B ON BT.book_id = B.book_id " +
			  				" WHERE B.restaurant_id = " + staff.getRestaurantId() + 
			  				" AND TO_DAYS(NOW()) - TO_DAYS(B.book_date) = 0 " +
			  				" GROUP BY BT.table_id ) AS BT ON BT.table_id = TBL.table_id " +
			  " WHERE 1 = 1 " +
			  " AND TBL.restaurant_id = " + staff.getRestaurantId() + " " +
			  (extraCond != null ? extraCond : "") + " " + 
			  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while (dbCon.rs.next()) {
			Table table = new Table();
			Region region = new Region(dbCon.rs.getShort("region_id"));
			region.setName(dbCon.rs.getString("region_name"));
			region.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			table.setRegion(region);
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			if(dbCon.rs.getInt("tbl_status") == Table.Status.BUSY.getVal()){
				table.setStatus(Table.Status.BUSY);
				table.setTempPaid(dbCon.rs.getBoolean("temp_paid"));
				table.setCategory(Category.valueOf(dbCon.rs.getInt("category")));
				table.setCustomNum(dbCon.rs.getInt("custom_num"));
				table.setOrderId(dbCon.rs.getInt("id"));
			}else{
				table.setStatus(Table.Status.IDLE);
			}
			table.setBook(dbCon.rs.getBoolean("book_flag"));
			table.setTableAlias(dbCon.rs.getInt("table_alias"));
			table.setId(dbCon.rs.getInt("table_id"));
			table.setTableName(dbCon.rs.getString("tbl_name"));
			result.add(table);
		}
		dbCon.rs.close();
		
		return result;		
	}
	
	/**
	 * Get the tables according to a specified restaurant defined in {@link Staff} and other condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the table result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Table> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the general information to a specified table according to builder {@link Table#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder the builder update table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Table.UpdateBuilder builder) throws SQLException, BusinessException{
		Table tblToUpdate = builder.build();
		
		String updateSQL = " UPDATE " + Params.dbName + ".table SET " +
						   " table_id = " + tblToUpdate.getId() +
						   (builder.isRegionChanged() ? " ,region_id = " + tblToUpdate.getRegion().getId() : "")+
						   (builder.isNameChanged() ? " ,name = '" + tblToUpdate.getName() + "'" : "") +
						   (builder.isMiniCostChanged() ? " ,minimum_cost = " + tblToUpdate.getMinimumCost() : "") +
						   " WHERE " +
						   " table_id = " + tblToUpdate.getId();
		if(dbCon.stmt.executeUpdate(updateSQL) == 0){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Update the general information to a specified table according to builder {@link Table#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder the builder update table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void update(Staff staff, Table.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a feast table to specific builder{@link Table#InsertBuilder4Feast}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a joined table
	 * @return the table id to just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Table.InsertBuilder4Feast builder) throws SQLException{
		return insert(dbCon, staff, builder.build());
	}
	
	/**
	 * Insert a fast table to specific builder{@link Table#InsertBuilder4Fast}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a joined table
	 * @return the table id to just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Table.InsertBuilder4Fast builder) throws SQLException{
		return insert(dbCon, staff, builder.build());
	}
	
	/**
	 * Insert a take out table to specific builder{@link Table#InsertBuilder4Takeout}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a joined table
	 * @return the table id to just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Table.InsertBuilder4Takeout builder) throws SQLException{
		return insert(dbCon, staff, builder.build());
	}
	
	/**
	 * Insert a joined table to specific builder{@link Table#InsertBuilder4Join}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a joined table
	 * @return the table id to just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the name to joined table is duplicated
	 */
	public static int insert(DBCon dbCon, Staff staff, Table.InsertBuilder4Join builder) throws SQLException, BusinessException{
		Table joinedTbl = builder.build();
		if(getByCond(dbCon, staff, new ExtraCond().addCategory(Category.JOIN).setName(joinedTbl.getName()), null).isEmpty()){
			return insert(dbCon, staff, joinedTbl);
		}else{
			throw new BusinessException("新拆台【" + joinedTbl.getName() + "】已存在，请选用其他的餐台名称", TableError.TABLE_INSERT_NOT_ALLOW);
		}
	}
	
	/**
	 * Insert a new table to a specified restaurant according to insert builder {@link Table#InsertBuilder}.
	 * The alias id to new table must be unique in a restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static int insert(Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new table to a specified restaurant according to builder{@link Table#InsertBuilder}.
	 * The alias id to new table must be unique in a restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the alias id to new table has been exist before
	 */
	public static int insert(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		Table tblToInsert = builder.build();
		
		if(tblToInsert.getCategory().isNormal()){
			String sql = " SELECT * FROM " + Params.dbName + ".table WHERE restaurant_id = " + staff.getRestaurantId() + " AND table_alias = " + tblToInsert.getAliasId() + " LIMIT 1 ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				throw new BusinessException(TableError.DUPLICATED_TABLE_ALIAS);
			}
			dbCon.rs.close();
		}
		
		return insert(dbCon, staff, tblToInsert);
	}
	
	/**
	 * Insert a new table to a specified restaurant according to insertion builder.
	 * The alias id to new table must be unique in a restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	private static int insert(DBCon dbCon, Staff staff, Table tblToInsert) throws SQLException{
		
		String sql;
		
		sql = " INSERT INTO " + Params.dbName + ".table " +
		 	  "(`table_alias`, `restaurant_id`, `name`, `region_id`, `category`, `minimum_cost`) VALUES( " +
			  (tblToInsert.getCategory().isNormal() ? tblToInsert.getAliasId() : "NULL") + ", " + 
			  staff.getRestaurantId() + ", " +
			  "'" + tblToInsert.getName() + "', " +
			  (tblToInsert.getCategory().isNormal() || tblToInsert.getCategory().isJoin() ? tblToInsert.getRegion().getId() : "NULL") + ", " +
			  tblToInsert.getCategory().getVal() + "," +
			  tblToInsert.getMinimumCost() + 
			  " ) ";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//Get the generated id to this new table. 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of new table is not generated successfully.");
		}
	}
	
	/**
	 * Insert tables according to specific builder {@link Table#BatchInsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the amount to table inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, Table.BatchInsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert tables according to specific builder {@link Table#BatchInsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the amount to table inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Table.BatchInsertBuilder builder) throws SQLException{
		int amount = 0;
		for(Table.InsertBuilder singleBuilder : builder.build()){
			try{
				insert(dbCon, staff, singleBuilder);
				amount++;
			}catch(BusinessException e){
				e.printStackTrace();
			}
		}
		return amount;
	}
	
	/**
	 * Delete a table according to table id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param tableId
	 * 			the table id to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int tableId) throws SQLException, BusinessException{
		if(delete(dbCon, staff, new ExtraCond().setId(tableId)) == 0){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Delete a table according to table id.
	 * @param staff
	 * 			the terminal
	 * @param tableId
	 * 			the table id to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int tableId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, tableId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete a table according to table alias id of a specified restaurant defined in terminal {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param tableAlias
	 * 			the table alias to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteByAliasId(DBCon dbCon, Staff staff, int tableAlias) throws SQLException, BusinessException{
		if(delete(dbCon, staff, new ExtraCond().setAliasId(tableAlias)) == 0){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Delete a table according to table alias id of a specified restaurant defined in terminal {@link Staff}.
	 * @param staff
	 * 			the terminal
	 * @param tableAlias
	 * 			the table alias to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteByAliasId(Staff staff, int tableAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteByAliasId(dbCon, staff, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the table according to extra condition of a specified restaurant defined in terminal {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int delete(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(Table table : getByCond(dbCon, staff, extraCond, null)){
			if(table.isBusy()){
				throw new BusinessException("【" + Table.Status.BUSY.getDesc() +	 "】状态的餐厅不能删除", TableError.TABLE_DELETE_NOT_ALLOW);
			}
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".table WHERE table_id = " + table.getId();
			dbCon.stmt.executeUpdate(sql);
			amount++;
		}
		return amount;
	}
	
	/**
	 * Delete the table according to extra condition of a specified restaurant defined in terminal {@link Staff}.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int delete(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = delete(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return amount;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Transfer the table to another one.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the table transfer builder {@link Table#TransferBuilder}
	 * @return the order id associated with the destination table after table transfer 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below
	 * 			<li>the source table is IDLE
	 * 			<li>the destination table is BUSY
	 **/
	public static int transfer(Staff staff, Table.TransferBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int orderId = transfer(dbCon, staff, builder);
			dbCon.conn.commit();
			return orderId;
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Transfer the table to another one.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the table transfer builder {@link Table#TransferBuilder}
	 * @return the order id associated with the destination table after table transfer 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below
	 * 			<li>the source table is IDLE
	 * 			<li>the destination table is BUSY
	 **/
	public static int transfer(DBCon dbCon, Staff staff, Table.TransferBuilder builder) throws SQLException, BusinessException{
		
		Table srcTbl = builder.getSrcTbl();
		Table destTbl = builder.getDestTbl();
		
		srcTbl = TableDao.getById(dbCon, staff, srcTbl.getId());

		destTbl = TableDao.getById(dbCon, staff, destTbl.getId());

			
		if(srcTbl.isIdle()) {
			throw new BusinessException("【" + srcTbl.getName() + "】是空闲状态, 不能转至【" + destTbl.getName() + "】",
										TableError.TABLE_TRANSFER_ERROR);

		}else if(destTbl.isBusy()) {
			throw new BusinessException("【" + destTbl.getName() + "】是就餐状态, " + "【" + srcTbl.getName() + "】不能转至【" + destTbl.getName() + "】", 
										TableError.TABLE_TRANSFER_ERROR);

		}else {

			int orderId = OrderDao.getByTableId(dbCon, staff, srcTbl.getId()).getId();

			// update the order
			String sql = " UPDATE "	+ 
						 Params.dbName	+ ".order " +
						 " SET id = " + orderId +
						 " ,table_id = " + destTbl.getId() + 
						 " ,table_alias = " + destTbl.getAliasId() + 
						 " ,table_name = " + "'" + destTbl.getName() + "'" +
						 " ,region_id = " + destTbl.getRegion().getId() + 
						 " ,region_name = '" + destTbl.getRegion().getName() + "'" +
						 " WHERE id = " + orderId;
			dbCon.stmt.executeUpdate(sql);

			//Delete the source table if belongs to temporary.
			if(srcTbl.getCategory().isTemporary()){
				deleteById(dbCon, staff, srcTbl.getId());
			}

			return orderId;
		}
	}
	
	/**
	 * Get the amount of table to a specified restaurant defined in terminal {@link Staff} and extra condition.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int getTableCount(Staff staff, String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTableCount(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the amount of table to a specified restaurant defined in terminal {@link Staff} and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int getTableCount(DBCon dbCon, Staff staff, String extraCond) throws SQLException{
		String sql;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".table TBL" +
			  " WHERE 1 = 1" + 
			  " AND TBL.restaurant_id = " + staff.getRestaurantId() + " " +	
			  (extraCond != null ? extraCond : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			return 0;
		}
	}
}
