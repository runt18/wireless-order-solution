package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.InsertOrder;
import com.wireless.db.Params;
import com.wireless.db.QueryTable;
import com.wireless.db.VerifyPin;
import com.wireless.db.menuMgr.QueryPricePlanDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Order;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class OrderGroupDao {

	/**
	 * Insert a new order group comprising the specific tables.
	 * @param term
	 * 			the terminal
	 * @param tableToGrouped
	 * 			the tables to be grouped
	 * @return completed information new order to be inserted
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if one of cases below.<br>
	 *			1 - Any table is merged.<br>
	 *			2 - Failed to insert a new order with the idle table. 		
	 */
	public static Order insert(Terminal term, Table[] tableToGrouped) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			List<Order> childOrders = new ArrayList<Order>(tableToGrouped.length);
			for(Table tbl : tableToGrouped){
				Order childOrder = new Order();
				childOrder.setDestTbl(tbl);
				childOrders.add(childOrder);
			}
			Order parentOrder = new Order();
			parentOrder.setChildOrder(childOrders.toArray(new Order[childOrders.size()]));
			
			return insert(dbCon, term, parentOrder);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new order group.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param parentOrder
	 * 			the order group to be inserted
	 * @return completed information new order to be inserted
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if one of cases below.<br>
	 *			1 - Any table is merged.<br>
	 *			2 - Failed to insert a new order with the idle table. 		
	 */
	static Order insert(DBCon dbCon, Terminal term, Order parentOrder) throws SQLException, BusinessException{
		if(parentOrder.hasChildOrder()){
			
			Order[] childOrders = parentOrder.getChildOrder();
			
			boolean isAutoCommit = dbCon.conn.getAutoCommit();
			
			try{
				dbCon.conn.setAutoCommit(false);
				
				//Get the price plan which is in use to this restaurant.
				PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND status = " + PricePlan.IN_USE + " AND restaurant_id = " + term.restaurantID, null);
				if(pricePlans.length > 0){
					parentOrder.setPricePlan(pricePlans[0]);
				}
	
				//Set the new group's category to merged.
				parentOrder.setCategory(Order.CATE_MERGER_TABLE);
				
				String sql;
	
				//Insert the parent order.
				sql = " INSERT INTO " + Params.dbName + ".order " +
					  "(`id`, `restaurant_id`, `category`, " +
					  " `terminal_model`, `terminal_pin`, `birth_date`, `order_date`, `custom_num`, `waiter`, `price_plan_id`)" +
					  " VALUES (" +
					  "	NULL, " + 
					  term.restaurantID + ", " + 
					  parentOrder.getCategory() + ", " +
	  				  term.modelID + ", " + 
					  term.pin + ", " +
					  " NOW() " + ", " + 
					  " NOW() " + ", " +
					  parentOrder.getCustomNum() + ", " +
					  "'" + term.owner + "'" + ", " +
					  parentOrder.getPricePlan().getId() + ")";
				
				dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
				
				//Get the generated id to parent order 
				dbCon.rs = dbCon.stmt.getGeneratedKeys();
				if(dbCon.rs.next()){
					parentOrder.setId(dbCon.rs.getInt(1));
				}else{
					throw new SQLException("The id to parent order is not generated successfully.");
				}	
				
				//Join each table to the parent order generated just now.
				for(int i = 0; i < childOrders.length; i++){				
					childOrders[i] = join(dbCon, term, parentOrder, childOrders[i].getDestTbl());
					parentOrder.setCustomNum(parentOrder.getCustomNum() + childOrders[i].getCustomNum());
				}
				
				//Update the total custom number to parent order.
				sql = " UPDATE " + Params.dbName + ".order SET " +
					  " custom_num = " + parentOrder.getCustomNum() +
					  " WHERE id = " + parentOrder.getId();
				dbCon.stmt.executeUpdate(sql);
				
				dbCon.conn.commit();				
				
			}catch(SQLException e){
				dbCon.conn.rollback();
				throw e;
				
			}catch(BusinessException e){
				dbCon.conn.rollback();
				throw e;
				
			}catch(Exception e){
				dbCon.conn.rollback();
				throw new BusinessException(e.getMessage());
				
			}finally{
				dbCon.conn.setAutoCommit(isAutoCommit);
			}
			
			return parentOrder;
			
		}else{
			throw new BusinessException("The parent order to insert has NOT any child order.");
		}
		
	}
	
	/**
	 * Update an exist parent order.
	 * @param term
	 * 			the terminal 
	 * @param parentOrderId
	 * 			the parent order id to update
	 * @param tblToUpdate
	 * 			the new tables associated with the parent order to update 
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - Any table to join was merged before.<br>
	 * 			2 - Any table to leave is NOT merged.<br>
	 * 			3 - The parent order to update does NOT exist. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void update(Terminal term, int parentOrderId, Table[] tblToUpdate) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, parentOrderId, tblToUpdate);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update an exist parent order.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param parentOrderId
	 * 			the parent order id to update
	 * @param tblToUpdate
	 * 			the new tables associated with the parent order to update 
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - Any table to join was merged before.<br>
	 * 			2 - Any table to leave is NOT merged.<br>
	 * 			3 - The parent order to update does NOT exist. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void update(DBCon dbCon, Terminal term, int parentOrderId, Table[] tblToUpdate) throws BusinessException, SQLException{
		Order orderToUpdate = new Order();
		orderToUpdate.setId(parentOrderId);
		
		Order[] childOrders = new Order[tblToUpdate.length];
		
		for(int i = 0; i < childOrders.length; i++){
			childOrders[i] = new Order();
			childOrders[i].setDestTbl(tblToUpdate[i]);
		}
		
		orderToUpdate.setChildOrder(childOrders);
		
		update(dbCon, term, orderToUpdate);
	}
	
	/**
	 * Update an exist parent order.
	 * @param term
	 * 			the terminal 
	 * @param parentToUpdate
	 * 			the parent order to update
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - Any table to join was merged before.<br>
	 * 			2 - Any table to leave is NOT merged.<br>
	 * 			3 - The parent order to update does NOT exist. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void update(Terminal term, Order parentToUpdate) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, parentToUpdate);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update an exist parent order.
	 * @param dbCon
	 * 			database connection
	 * @param term
	 * 			the terminal 
	 * @param parentToUpdate
	 * 			the parent order to update
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - Any table to join was merged before.<br>
	 * 			2 - Any table to leave is NOT merged.<br>
	 * 			3 - The parent order to update does NOT exist. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void update(DBCon dbCon, Terminal term, Order parentToUpdate) throws BusinessException, SQLException{
		
		if(parentToUpdate.hasChildOrder()){
			
			Order srcParent = QueryOrderDao.execByID(dbCon, parentToUpdate.getId(), QueryOrderDao.QUERY_TODAY);
			
			DiffResult diffResult = diff(srcParent, parentToUpdate);
			try{
				
				dbCon.conn.setAutoCommit(false);
				
				//Join the tables to source parent order.
				for(Table tbl : diffResult.tblToJoin){
					join(dbCon, term, srcParent, tbl);
				}
				
				//Leave the tables to source parent order.
				for(Table tbl : diffResult.tblToLeave){
					leave(dbCon, term, srcParent, tbl);
				}
				
				String sql;
				
				//Delete the parent if no child order exist.
				sql = " DELETE FROM " + Params.dbName + ".order " +
					  " WHERE id = " + parentToUpdate.getId() +
					  " AND NOT EXISTS (" +
					  " SELECT * FROM " + Params.dbName + ".order_group WHERE order_id = " + parentToUpdate.getId() + ")";
				dbCon.stmt.executeUpdate(sql);
				
				dbCon.conn.commit();
				
			}catch(SQLException e){
				dbCon.conn.rollback();
				throw e;
				
			}catch(BusinessException e){
				dbCon.conn.rollback();
				throw e;
				
			}catch(Exception e){
				dbCon.conn.rollback();
				throw new BusinessException(e.getMessage());
				
			}finally{
				dbCon.conn.setAutoCommit(true);
			}
			
		}else{
			throw new BusinessException("The parent order to update has NOT any child order.");
		}
	}
	
	/**
	 * Comparing the source and destination parent order to find out
	 * which tables are to be joined and which ones to be leaved. 
	 * @param srcParent
	 * 			the source parent order(means original)
	 * @param destParent
	 * 			the destination parent order(means current)
	 * @return the difference result containing the tables to be joined and leaved
	 * @throws BusinessException
	 * 			throws if either of parent order is merged
	 */
	private static DiffResult diff(Order srcParent, Order destParent) throws BusinessException{		
		
		if(srcParent.hasChildOrder() && destParent.hasChildOrder()){
			
			DiffResult diffResult = new DiffResult();
			
			List<Order> srcChildren = new ArrayList<Order>(Arrays.asList(srcParent.getChildOrder()));
			List<Order> destChildren = new ArrayList<Order>(Arrays.asList(destParent.getChildOrder()));		
			
			Iterator<Order> iterSrc = srcChildren.iterator();
			Iterator<Order> iterDest = destChildren.iterator();
			
			/**
			 * Compare the tables to source and destination parent order as below.
			 * <1> Ori - New
			 * Means the tables are only contained in the original parent order.
			 * These tables would be leaved.
			 * <2> New - Ori
			 * Means the tables are only contained in the destination parent order.
			 * These tables would be joined.
			 */
			while(iterDest.hasNext()){
				Table destTbl = iterDest.next().getDestTbl();
				while(iterSrc.hasNext()){
					Table srcTbl = iterSrc.next().getDestTbl();
					if(destTbl.equals(srcTbl)){
						iterDest.remove();
						iterSrc.remove();
						break;
					}
				}
			}
			
			for(Order dest : destChildren){
				diffResult.tblToJoin.add(dest.getDestTbl());
			}
			
			for(Order src : srcChildren){
				diffResult.tblToLeave.add(src.getDestTbl());
			}
			
			return diffResult;
			
		}else{
			throw new BusinessException("The order group to be compared does NOT contain any child orders.");
		}
	}
	
	private static class DiffResult{	
		List<Table> tblToJoin = new ArrayList<Table>();
		List<Table> tblToLeave = new ArrayList<Table>();
	}
	
	/**
	 * Join a table to the specific order
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param parentJoinTo
	 * 			the parent order the table join in
	 * @param tableToJoin
	 * 			the table to be joined
	 * @return the order associated with the table to be joined
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The table to query does NOT exist.<br>
	 *			2 - The table to be joined is merged.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	static Order join(DBCon dbCon, Terminal term, Order parentJoinTo, Table tableToJoin) throws BusinessException, SQLException{
		
		short customNum = tableToJoin.getCustomNum();
		
		//Get the detail to order's destination table.
		tableToJoin = QueryTable.exec(term, tableToJoin.getAliasId());
		tableToJoin.setCustomNum(customNum);
		
		Order orderToJoinedTbl = new Order();
		orderToJoinedTbl.setDestTbl(tableToJoin);
		orderToJoinedTbl.setCustomNum(tableToJoin.getCustomNum());
		orderToJoinedTbl.setCategory(Order.CATE_MERGER_TABLE);
		
		//Insert a new order if the table is idle,
		//otherwise get the unpaid order id associated with this table.
		if(tableToJoin.isIdle()){
			orderToJoinedTbl.setId(InsertOrder.execAsync(dbCon, term, orderToJoinedTbl).getId());
			
		}else{
			int[] unpaidID = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tableToJoin);
			if(unpaidID.length < 2){
				orderToJoinedTbl.setId(unpaidID[0]);
			}else{
				throw new BusinessException("The table(alias_id = " + tableToJoin.getAliasId() + ", restaurant_id = " + tableToJoin.restaurantID + ") to be joined in a group can NOT be merged.");
			}
		}
		
		String sql;
		
		//Update the category of each child order's table to child merged.
		sql = " UPDATE " + Params.dbName + ".table SET " +
		      " category = " + Order.CATE_MERGER_CHILD +
		      " WHERE table_id = " + tableToJoin.getTableId();
		dbCon.stmt.executeUpdate(sql);					

		
		//Update the category of each child order to child merged.
		sql = " UPDATE " + Params.dbName + ".order SET " +
		      " category = " + Order.CATE_MERGER_CHILD +
		      " WHERE id = " + orderToJoinedTbl.getId();
		dbCon.stmt.executeUpdate(sql);					
		
		//Insert each entry map (parent - child) to order group. 
		sql = " INSERT INTO " + Params.dbName + ".order_group " +
			  " (`order_id`, `sub_order_id`, `restaurant_id`) " +
			  " VALUES (" +
			  parentJoinTo.getId() + ", " +
			  orderToJoinedTbl.getId() + ", " +
			  term.restaurantID + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Insert the sub order.
		sql = " INSERT INTO " + Params.dbName + ".sub_order " +
			  " (`order_id`, `table_id`, `table_name`) " + 
			  " VALUES (" +
			  orderToJoinedTbl.getId() + ", " +
			  orderToJoinedTbl.getDestTbl().getTableId() + ", " +
			  "'" + orderToJoinedTbl.getDestTbl().name + "'" + 
			  ")";
		dbCon.stmt.executeUpdate(sql);		
		
		return orderToJoinedTbl;
		
	}
	
	/**
	 * Have a table leaving from a parent order.
	 * Note that the order associated with the table would be deleted in case of empty. 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param parentRemoveFrom
	 * 			the parent order remove from
	 * @param tableToLeave
	 * 			the table to leave
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The table to leave does NOT exist.<br>
	 *			2 - The table to leave was NOT merged before.<br>
	 *			3 - The order associated with this table does NOT belongs to the parent order.	
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	static void leave(DBCon dbCon, Terminal term, Order parentRemoveFrom, Table tableToLeave) throws BusinessException, SQLException{
		
		tableToLeave = QueryTable.exec(dbCon, term, tableToLeave.getAliasId());
		
		int[] unpaidIDs = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tableToLeave);
		
		if(unpaidIDs.length > 1){
			int childOrderId = unpaidIDs[0];
			int parentOrderId = unpaidIDs[1];
			if(parentRemoveFrom.getId() == parentOrderId){

				String sql;

				/*
				 * Check to see whether the child food is empty or NOT.
				 * Delete the child order in case of empty.
				 * Otherwise just to update its status.
				 */
				sql = " SELECT COUNT(*) FROM " + Params.dbName + ".order_food " +
					  " WHERE order_id = " + childOrderId;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				int orderAmount = 0;
				if(dbCon.rs.next()){
					orderAmount = dbCon.rs.getInt(1);
				}				
				dbCon.rs.close();
				
				//Delete the sub order 
				sql = " DELETE FROM " + Params.dbName + ".sub_order " +
					  " WHERE order_id = " + childOrderId;
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the order group
				sql = " DELETE FROM " + Params.dbName + ".order_group " +
					  " WHERE sub_order_id = " + childOrderId;
				dbCon.stmt.executeUpdate(sql);
				
				if(orderAmount == 0){
					//Update the status to table associated with child order.
					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " status = " + Table.TABLE_IDLE + ", " +
						  " custom_num = NULL, " +
						  " category = NULL " +
						  " WHERE table_id = " + tableToLeave.getTableId();
					dbCon.stmt.executeUpdate(sql);
					
					//Delete the child order.
					sql = "DELETE FROM " + Params.dbName + ".order WHERE id = " + childOrderId;
					dbCon.stmt.executeUpdate(sql);
					
				}else{
					
					//Update the child order category to normal
					sql = " UPDATE " + Params.dbName + ".order SET " +
						  " category = " + Order.CATE_NORMAL +
						  " WHERE id = " + childOrderId;
					dbCon.stmt.executeUpdate(sql);
					
					//Update the left table category to normal
					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " category = " + Order.CATE_NORMAL +
						  " WHERE table_id = " + tableToLeave.getTableId();
					dbCon.stmt.executeUpdate(sql);
				}
				
			}else{
				throw new BusinessException("The parent order(id=" + unpaidIDs[1] + ") " + tableToLeave + " belongs to is NOT the same as the parent order(id=" + parentRemoveFrom.getId() + ") removed from.");
			}
		}else{
			throw new BusinessException("The " + tableToLeave + " to leaved is NOT merged.");
		}
	}
	
	/**
	 * Cancel a parent order that the specific table belongs to.
	 * @param term
	 * 			the terminal
	 * @param tblToCancel
	 * 			the table belongs to a parent order you want to cancel
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The table does NOT exist.<br>
	 * 		    2 - The parent order this table belongs to does NOT exist.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void cancel(Terminal term, Table tblToCancel) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			cancel(dbCon, term, tblToCancel);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel a parent order that the specific table belongs to.
	 * @param dbCon
	 * 			database connection
	 * @param term
	 * 			the terminal
	 * @param tblToCancel
	 * 			the table belongs to a parent order you want to cancel
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The table does NOT exist.<br>
	 * 		    2 - The parent order this table belongs to does NOT exist.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void cancel(DBCon dbCon, Terminal term, Table tblToCancel) throws BusinessException, SQLException{
		
		int[] unpaidIDs = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, tblToCancel.getAliasId()));
		
		if(unpaidIDs.length > 1){
			
			Order parentOrder = new Order();
			parentOrder.setId(unpaidIDs[1]);
			
			cancel(dbCon, term, parentOrder);
			
		}else{
			throw new BusinessException("The parent order " + tblToCancel + " belongs to does NOT exist.");
		}
		
	}
	
	/**
	 * Cancel a specific parent order according to the specific id.
	 * @param term
	 * 			the terminal 
	 * @param parentToCancel
	 * 			the parent order to cancel
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 * @throws BusinessException
	 * 			Throws if the parent order to this id does NOT exist.
	 */
	public static void cancel(Terminal term, Order parentToCancel) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			cancel(dbCon, term, parentToCancel);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel a specific parent order according to the specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param parentToCancel
	 * 			the parent order to cancel
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 * @throws BusinessException
	 * 			Throws if the parent order to this id does NOT exist.
	 */
	public static void cancel(DBCon dbCon, Terminal term, Order parentToCancel) throws SQLException, BusinessException{
		
		parentToCancel = QueryOrderDao.execByID(parentToCancel.getId(), QueryOrderDao.QUERY_TODAY);
		
		try{
			
			dbCon.conn.setAutoCommit(false);
			
			//Have all tables removed from parent order.
			for(Order childOrder : parentToCancel.getChildOrder()){
				leave(dbCon, term, parentToCancel, childOrder.getDestTbl());
			}
			
			//Delete the parent order.
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".order " +
				  " WHERE id = " + parentToCancel.getId();
			dbCon.stmt.executeUpdate(sql);
			
			dbCon.conn.commit();
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new BusinessException(e.getMessage());
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
	}
	
	
	@BeforeClass
	public static void initDbParam(){
		Params.setDbUser("root");
		Params.setDbHost("192.168.146.100");
		Params.setDbPort(3306);
		Params.setDatabase("wireless_order_db");
		Params.setDbPwd("HelloZ315");
	}
	
	@Test 
	public void testInsert() throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		
		Table[] tblToInsert = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}	
		
		Order parentOrder = QueryOrderDao.execByID(OrderGroupDao.insert(term, tblToInsert).getId(), QueryOrderDao.QUERY_TODAY);
		//Check if parent order is merged.
		Assert.assertTrue(parentOrder.isMerged());
		
		if(parentOrder.hasChildOrder()){
			Assert.assertEquals(parentOrder.getChildOrder().length, tblToInsert.length);
			for(Order childOrder : parentOrder.getChildOrder()){
				Order orderToChild = QueryOrderDao.execByID(childOrder.getId(), QueryOrderDao.QUERY_TODAY);
				//Check if each child order is merged.
				Assert.assertTrue(orderToChild.isMergedChild());
				//Check if the table associated with each child order is merged.
				Assert.assertTrue(orderToChild.getDestTbl().isMerged());
			}
		}else{
			Assert.assertTrue("The order does NOT contain any child order.", false);
		}
		
		//Cancel the record after performing insertion.
		OrderGroupDao.cancel(term, tblToInsert[0]);
	}
	
	@Test
	public void testUpdate() throws BusinessException, SQLException{		

		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
	
		Table[] tblToInsert = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}
		int parentOrderId = OrderGroupDao.insert(term, tblToInsert).getId();
		
		Table[] tblToUpdate = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		Order parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		check(parentOrder, tblToUpdate);

		tblToUpdate = new Table[]{
			new Table(0, 1, 37),
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		check(parentOrder, tblToUpdate);
		
		tblToUpdate = new Table[]{
			new Table(0, 2, 37),
			new Table(0, 3, 37)
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		check(parentOrder, tblToUpdate);
		
		OrderGroupDao.cancel(term, tblToUpdate[0]);
	}
	
	private void check(Order orderToCheck, Table[] expectedTbls) throws BusinessException, SQLException{
		//Check if parent order is merged.
		Assert.assertTrue(orderToCheck.isMerged());
		
		if(orderToCheck.hasChildOrder()){
			Assert.assertEquals(orderToCheck.getChildOrder().length, expectedTbls.length);
			for(Order childOrder : orderToCheck.getChildOrder()){
				Order orderToChild = QueryOrderDao.execByID(childOrder.getId(), QueryOrderDao.QUERY_TODAY);
				//Check if the table to each child order is contained in expected tables.
				boolean isContained = false;
				for(Table tbl : expectedTbls){
					if(orderToChild.getDestTbl().equals(tbl)){
						isContained = true;
						break;
					}
				}
				Assert.assertTrue(isContained);
				
				//Check if each child order is merged.
				Assert.assertTrue(orderToChild.isMergedChild());
				//Check if the table associated with each child order is merged.
				Assert.assertTrue(orderToChild.getDestTbl().isMerged());
			}
		}else{
			Assert.assertTrue("The order does NOT contain any child order.", false);
		}
	}
}
