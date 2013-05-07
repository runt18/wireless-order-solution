package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class OrderGroupDao {

	/**
	 * Insert a new order group comprising the specific tables.
	 * Create a new order associated with the table in case of idle.
	 * Attached the order associated with the table in case of busy.
	 * @param term
	 * 			the terminal
	 * @param tableToGrouped
	 * 			the tables to be grouped
	 * @return the generated id to parent order if succeed to insert 
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 *			1 - Any table is merged.<br>
	 */
	public static int insert(Terminal term, Table[] tableToGrouped) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Order[] childOrders = new Order[tableToGrouped.length];
			for(int i = 0; i < childOrders.length; i++){
				tableToGrouped[i] = TableDao.getTableByAlias(dbCon, term, tableToGrouped[i].getAliasId());
				if(tableToGrouped[i].isIdle()){
					childOrders[i] = new Order();
					childOrders[i].setDestTbl(tableToGrouped[i]);
					childOrders[i].setCustomNum(tableToGrouped[i].getCustomNum());
					
				}else if(tableToGrouped[i].isBusy()){
					childOrders[i] = QueryOrderDao.execByTable(dbCon, term, tableToGrouped[i].getAliasId());
					
				}else if(tableToGrouped[i].isMerged()){
					throw new BusinessException("The " + tableToGrouped[i] + " to insert is merged.");
				}
			}
			Order parentOrder = new Order();
			parentOrder.setChildOrder(childOrders);
			
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
	 * @return the generated id to parent order if succeed to insert 
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The order to join does NOT exist.<br>
	 * 		    2 - The table associated with the new order is NOT idle.	
	 */
	public static int insert(Terminal term, Order parentOrder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
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
	 * @return the generated id to parent order if succeed to insert 
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The order to join does NOT exist.<br>
	 * 		    2 - The table associated with the new order is NOT idle.	
	 */
	public static int insert(DBCon dbCon, Terminal term, Order parentOrder) throws SQLException, BusinessException{
		if(parentOrder.hasChildOrder()){
			
			boolean isAutoCommit = dbCon.conn.getAutoCommit();
			
			try{
				dbCon.conn.setAutoCommit(false);
				
				//Get the price plan which is in use to this restaurant.
				parentOrder.setPricePlan(PricePlanDao.getActivePricePlan(dbCon, term));
	
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
				
				//Join each child order to parent order generated just now.
				for(Order childOrder : parentOrder.getChildOrder()){
					join(dbCon, term, parentOrder, childOrder);
					parentOrder.setCustomNum(parentOrder.getCustomNum() + childOrder.getCustomNum());
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
			
			return parentOrder.getId();
			
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
			try{
				childOrders[i] = QueryOrderDao.execByTable(dbCon, term, tblToUpdate[i].getAliasId());
				childOrders[i].setCategory(Order.CATE_MERGER_CHILD);
			}catch(BusinessException e){
				childOrders[i].setId(0);
				childOrders[i].setDestTbl(tblToUpdate[i]);
				childOrders[i].setCustomNum(tblToUpdate[i].getCustomNum());
			}
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
	 * 			1 - The parent order group does NOT exist.<br>
	 * 			2 - Any order to join does NOT exist.<br>
	 * 			3 - The table to order being joined is NOT idle.<br> 
	 * 			4 - The order to remove does NOT belong to any order group.<br>
	 *			5 - The order to remove does NOT belong to parent group removed from.<br>
	 *			6 - The order update
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static void update(DBCon dbCon, Terminal term, Order parentToUpdate) throws BusinessException, SQLException{
		
		if(parentToUpdate.hasChildOrder()){
			
			Order srcParent = QueryOrderDao.execByID(dbCon, parentToUpdate.getId(), QueryOrderDao.QUERY_TODAY);
			
			// Compared the details between original and new order group to get the difference result.
			DiffResult diffResult = diff(srcParent, parentToUpdate);
			try{
				
				dbCon.conn.setAutoCommit(false);
				
				//Join the new order to parent order group.
				for(Order order : diffResult.orderToJoin){
					join(dbCon, term, srcParent, order);
				}
				
				//Leave the order from parent order group.
				for(Order order : diffResult.orderToLeave){
					leave(dbCon, term, srcParent, order);
				}
				
				//Update the order already exist in parent order group.
				for(Order order : diffResult.orderToUpdate){
					order.setCategory(Order.CATE_MERGER_CHILD);
					UpdateOrder.execByIdAsync(dbCon, term, order);
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
				Order destOrder = iterDest.next();
				Iterator<Order> iterSrc = srcChildren.iterator();
				while(iterSrc.hasNext()){
					Order srcOrder = iterSrc.next();
					if(destOrder.getId() == srcOrder.getId()){
						iterDest.remove();
						iterSrc.remove();
						diffResult.orderToUpdate.add(destOrder);
						break;
					}
				}
			}
			
			for(Order dest : destChildren){
				diffResult.orderToJoin.add(dest);
			}
			
			for(Order src : srcChildren){
				diffResult.orderToLeave.add(src);
			}
			
			return diffResult;
			
		}else{
			throw new BusinessException("The order group to be compared does NOT contain any child orders.");
		}
	}
	
	private static class DiffResult{	
		List<Order> orderToJoin = new ArrayList<Order>();
		List<Order> orderToLeave = new ArrayList<Order>();
		List<Order> orderToUpdate = new ArrayList<Order>();
	}
	
	/**
	 * Join an order to the parent order group.
	 * If the id of order to join is zero, means to insert a new order.
	 * Otherwise having the order to join attached to parent order group.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param parentJoinedTo
	 * 			the parent order which is joined to
	 * @param orderToJoin
	 * 			the order to be joined
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The order to join does NOT exist.<br>
	 * 		    2 - The table associated with the new order is NOT idle.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	static void join(DBCon dbCon, Terminal term, Order parentJoinedTo, Order orderToJoin) throws BusinessException, SQLException{
		
		if(orderToJoin.getId() == 0){
			// Insert a new order if the order id is zero.
			orderToJoin.setCategory(Order.CATE_MERGER_CHILD);
			InsertOrder.execAsync(dbCon, term, orderToJoin);
		}else{
			// Get the order detail if the order id exist.
			orderToJoin = QueryOrderDao.execByID(dbCon, orderToJoin.getId(), QueryOrderDao.QUERY_TODAY);
		}
		
		String sql;
		
		//Update the category of each child order's table to child merged.
		sql = " UPDATE " + Params.dbName + ".table SET " +
		      " category = " + Order.CATE_MERGER_CHILD +
		      " WHERE table_id = " + orderToJoin.getDestTbl().getTableId();
		dbCon.stmt.executeUpdate(sql);					

		
		//Update the category of each child order to child merged.
		sql = " UPDATE " + Params.dbName + ".order SET " +
		      " category = " + Order.CATE_MERGER_CHILD +
		      " WHERE id = " + orderToJoin.getId();
		dbCon.stmt.executeUpdate(sql);					
		
		//Insert each entry map (parent - child) to order group. 
		sql = " INSERT INTO " + Params.dbName + ".order_group " +
			  " (`order_id`, `sub_order_id`, `restaurant_id`) " +
			  " VALUES (" +
			  parentJoinedTo.getId() + ", " +
			  orderToJoin.getId() + ", " +
			  term.restaurantID + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Insert the sub order.
		sql = " INSERT INTO " + Params.dbName + ".sub_order " +
			  " (`order_id`, `table_id`, `table_name`) " + 
			  " VALUES (" +
			  orderToJoin.getId() + ", " +
			  orderToJoin.getDestTbl().getTableId() + ", " +
			  "'" + orderToJoin.getDestTbl().getName() + "'" + 
			  ")";
		dbCon.stmt.executeUpdate(sql);		
	}
	
	/**
	 * Join a table to a parent order group.
	 * If the table to join is idle, means to insert a new order along with this table.
	 * If the table to join is busy, having the associated order attached to parent order gruop.
	 * Otherwise fails to join a table to order group.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param parentJoinedTo
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
	static void join(DBCon dbCon, Terminal term, Order parentJoinedTo, Table tableToJoin) throws BusinessException, SQLException{
		
		// Get the detail to table associated with order to join.
		tableToJoin = TableDao.getTableByAlias(dbCon, term, tableToJoin.getAliasId());
		
		Order orderToJoin = new Order();
		if(tableToJoin.isIdle()){
			// Set order id to zero if the table is idle.
			orderToJoin.setId(0);
			
		}else if(tableToJoin.isBusy()){
			// Set the order id associated with this table if busy
			orderToJoin.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tableToJoin)[0]);
			
		}else if(tableToJoin.isMerged()){
			throw new BusinessException("The " + tableToJoin + " can't be joined because it is merged now." );
			
		}else{
			throw new BusinessException("The " + tableToJoin + " can't be joined because its status is incorrect. ");
		}
		
		orderToJoin.setDestTbl(tableToJoin);
		orderToJoin.setCustomNum(tableToJoin.getCustomNum());
		
		join(dbCon, term, parentJoinedTo, orderToJoin);
		
		
	}
	
	/**
	 * Leave an order from its parent order group.
	 * If the order to leave is empty(means NOT has any order food), then delete it.
	 * Otherwise restore the leaved order to normal. 
	 * @param dbCon
	 * @param term
	 * @param parentRemovedFrom
	 * @param orderToRemove
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The order to remove does NOT belong to any order group.<br>
	 * 			2 - The order to remove does NOT belong to parent group removed from.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static void leave(DBCon dbCon, Terminal term, Order parentRemovedFrom, Order orderToRemove) throws BusinessException, SQLException{
		
		String sql;

		sql = " SELECT order_id FROM " + Params.dbName + ".order_group" +
			  " WHERE " + " sub_order_id = " + orderToRemove.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		if(dbCon.rs.next()){
		
			//Check to see whether the parent to order removed is the same as parent removed from.
			if(parentRemovedFrom.getId() == dbCon.rs.getInt("order_id")){

				/*
				 * Check to see whether the child food is empty or NOT.
				 * Delete the child order in case of empty.
				 * Otherwise just to update its status.
				 */
				sql = " SELECT COUNT(*) FROM " + Params.dbName + ".order_food " +
					  " WHERE order_id = " + orderToRemove.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				int orderAmount = 0;
				if(dbCon.rs.next()){
					orderAmount = dbCon.rs.getInt(1);
				}				
				dbCon.rs.close();
				
				//Delete the sub order 
				sql = " DELETE FROM " + Params.dbName + ".sub_order " +
					  " WHERE order_id = " + orderToRemove.getId();
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the order group
				sql = " DELETE FROM " + Params.dbName + ".order_group " +
					  " WHERE sub_order_id = " + orderToRemove.getId();
				dbCon.stmt.executeUpdate(sql);
				
				//Get the table id to order removed
				int tblToOrderLeved = 0;
				sql = " SELECT table_id FROM " + Params.dbName + ".order " +
					  " WHERE id = " + orderToRemove.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					tblToOrderLeved = dbCon.rs.getInt("table_id");
				}
				
				/*
				 * If the order to remove is empty, then delete it.
				 * Otherwise make the leaved order restore to normal.
				 */
				if(orderAmount == 0){
					//Update the status to table associated with child order.
					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " status = " + Table.Status.IDLE.getVal() + ", " +
						  " custom_num = NULL, " +
						  " category = NULL " +
						  " WHERE table_id = " + tblToOrderLeved;
					dbCon.stmt.executeUpdate(sql);
					
					//Delete the child order.
					sql = " DELETE FROM " + Params.dbName + ".order WHERE id = " + orderToRemove.getId();
					dbCon.stmt.executeUpdate(sql);
					
				}else{
					
					//Update the child order category to normal
					sql = " UPDATE " + Params.dbName + ".order SET " +
						  " category = " + Order.CATE_NORMAL +
						  " WHERE id = " + orderToRemove.getId();
					dbCon.stmt.executeUpdate(sql);
					
					//Update the leaved table category to normal
					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " category = " + Order.CATE_NORMAL +
						  " WHERE table_id = " + tblToOrderLeved;
					dbCon.stmt.executeUpdate(sql);
				}
				
			}else{
				throw new BusinessException("The parent order(id=" + orderToRemove.getId() + ") is NOT the same as the parent order(id=" + parentRemovedFrom.getId() + ") removed from.");
			}
		}else{
			throw new BusinessException("The order(id = " + orderToRemove.getId() + ") to remove does NOT belong to any order group.");
		}
	}
	
	/**
	 * Have a table leaving from a parent order.
	 * Delete the order associated with table if the order does NOT contain any order food.
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
	 *			2 - The order associated with this table does NOT belongs to the parent order.	
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	static void leave(DBCon dbCon, Terminal term, Order parentRemoveFrom, Table tableToLeave) throws BusinessException, SQLException{
		
		Order orderToLeave = new Order();
		orderToLeave.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tableToLeave)[0]);
		leave(dbCon, term, parentRemoveFrom, orderToLeave);
		
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
		
		int[] unpaidIDs = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, TableDao.getTableByAlias(dbCon, term, tblToCancel.getAliasId()));
		
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
				leave(dbCon, term, parentToCancel, childOrder);
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

}
