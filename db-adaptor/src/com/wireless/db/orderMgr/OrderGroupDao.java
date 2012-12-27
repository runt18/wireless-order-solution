package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.InsertOrder;
import com.wireless.db.Params;
import com.wireless.db.QueryTable;
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
			
			try{
				
				//dbCon.conn.setAutoCommit(false);
				
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
				
				//dbCon.conn.commit();				
				
			}catch(SQLException e){
				//dbCon.conn.rollback();
				throw e;
				
			}catch(BusinessException e){
				//dbCon.conn.rollback();
				throw e;
				
			}catch(Exception e){
				//dbCon.conn.rollback();
				throw new BusinessException(e.getMessage());
				
			}finally{
				//dbCon.conn.setAutoCommit(true);
			}
			
			return parentOrder;
			
		}else{
			throw new BusinessException("The parent order to insert has NOT any child order.");
		}
		
	}
	
	public static void update(DBCon dbCon, Terminal term, Order destParent) throws BusinessException, SQLException{
		if(destParent.hasChildOrder()){
			
			Order srcParent = QueryOrderDao.execByID(dbCon, destParent.getId(), QueryOrderDao.QUERY_TODAY);
			
			DiffResult diffResult = diff(srcParent, destParent);
			
			//Join the tables to source parent order.
			for(Table tbl : diffResult.tblToJoin){
				join(dbCon, term, srcParent, tbl);
			}
			
			//Leave the tables to soruce parent order.
			for(Table tbl : diffResult.tblToLeave){
				leave(dbCon, term, srcParent, tbl);
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
		tableToJoin = QueryTable.exec(term, tableToJoin.aliasID);
		tableToJoin.setCustomNum(customNum);
		
		Order orderToJoinedTbl = new Order();
		orderToJoinedTbl.setDestTbl(tableToJoin);
		orderToJoinedTbl.setCustomNum(tableToJoin.getCustomNum());
		orderToJoinedTbl.setCategory(Order.CATE_MERGER_TABLE);
		
		//Insert a new order if the table is idle,
		//otherwise get the unpaid order id associated with this table.
		if(tableToJoin.isIdle()){
			orderToJoinedTbl.setId(InsertOrder.exec(dbCon, term, orderToJoinedTbl).getId());
			
		}else{
			int[] unpaidID = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tableToJoin);
			if(unpaidID.length < 2){
				orderToJoinedTbl.setId(unpaidID[0]);
			}else{
				throw new BusinessException("The table(alias_id = " + tableToJoin.aliasID + ", restaurant_id = " + tableToJoin.restaurantID + ") to be joined in a group can NOT be merged.");
			}
		}
		
		String sql;
		
		//Update the category of each child order's table to child merged.
		sql = " UPDATE " + Params.dbName + ".table SET " +
		      " category = " + Order.CATE_MERGER_CHILD +
		      " WHERE table_id = " + tableToJoin.tableID;
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
			  orderToJoinedTbl.getDestTbl().tableID + ", " +
			  "'" + orderToJoinedTbl.getDestTbl().name + "'" + 
			  ")";
		dbCon.stmt.executeUpdate(sql);		
		
		return orderToJoinedTbl;
		
	}
	
	static void leave(DBCon dbCon, Terminal term, Order parentRemoveFrom, Table tableToLeave) throws BusinessException, SQLException{
		
		tableToLeave = QueryTable.exec(dbCon, term, tableToLeave.aliasID);
		
		int[] unpaidIDs = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tableToLeave);
		
		if(unpaidIDs.length > 1){
			if(parentRemoveFrom.getId() == unpaidIDs[1]){
				//Delete the sub order 
				String sql;
				sql = " DELETE FROM " + Params.dbName + ".sub_order " +
					  " WHERE order_id = " + unpaidIDs[0];
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the order group
				sql = " DELETE FROM " + Params.dbName + ".order_group " +
					  " WHERE sub_order_id = " + unpaidIDs[0];
				dbCon.stmt.executeUpdate(sql);
				
				//Update the sub order category to normal
				sql = " UPDATE " + Params.dbName + ".order SET " +
					  " category = " + Order.CATE_NORMAL +
					  " WHERE id = " + unpaidIDs[0];
				dbCon.stmt.executeUpdate(sql);
				
				//Update the left table category to normal
				sql = " UPDATE " + Params.dbName + ".table SET " +
					  " category = " + Order.CATE_NORMAL +
					  " WHERE table_id = " + tableToLeave.tableID;
				dbCon.stmt.executeUpdate(sql);

				//TODO Whether to delete the parent if no child order exist ???
				
			}else{
				throw new BusinessException("The parent order(id=" + unpaidIDs[1] + ")this table(id=" + tableToLeave.aliasID + ", restuarnt_id=" + tableToLeave.restaurantID + ")belongs to is NOT the same as the parent order(id=" + parentRemoveFrom.getId() + ") removed from.");
			}
		}else{
			throw new BusinessException("The table(alias_id=" + tableToLeave.aliasID + ",restaurant_id=" + tableToLeave.restaurantID + ") to leaved is NOT merged.");
		}
	}
	
	public static void cancel() throws SQLException{
		
	}
	
}
