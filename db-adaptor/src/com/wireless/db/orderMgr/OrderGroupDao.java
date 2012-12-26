package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
				childOrder.destTbl = tbl;
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
				
				/*
				 * Get the unpaid order id to each table of child order.
				 * Insert a new order if the table is IDLE.
				 */
				for(int i = 0; i < childOrders.length; i++){
					//Calculate the custom number to this parent order 
					parentOrder.setCustomNum(parentOrder.getCustomNum() + childOrders[i].getCustomNum());
					
					//Get the detail to order's destination table.
					childOrders[i].destTbl = QueryTable.exec(term, childOrders[i].destTbl.aliasID);
					
					//Insert a new order if the table is idle,
					//otherwise get the unpaid order id associated with this table.
					if(childOrders[i].destTbl.isIdle()){
						childOrders[i].setCategory(Order.CATE_MERGER_TABLE);
						childOrders[i].setId(InsertOrder.exec(dbCon, term, childOrders[i]).getId());					
					}else{
						int[] unpaidID = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, childOrders[i].destTbl);
						if(unpaidID.length < 2){
							childOrders[i].setId(unpaidID[0]);
						}else{
							throw new BusinessException("The table(alias_id = " + childOrders[i].destTbl.aliasID + ", restaurant_id = " + childOrders[i].destTbl.restaurantID + ") can NOT be merged.");
						}
					}
				}
				
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
				
				for(Order childOrder : parentOrder.getChildOrder()){

					//Update the category of each child order's table to merged.
					sql = " UPDATE " + Params.dbName + ".table SET " +
					      " category = " + Order.CATE_MERGER_TABLE +
					      " WHERE table_id = " + childOrder.destTbl.tableID;
					dbCon.stmt.executeUpdate(sql);					

					
					//Update the category of each child order to merged.
					sql = " UPDATE " + Params.dbName + ".order SET " +
					      " category = " + Order.CATE_MERGER_TABLE +
					      " WHERE id = " + childOrder.getId();
					dbCon.stmt.executeUpdate(sql);					
					
					//Insert each entry map (parent - child) to order group. 
					sql = " INSERT INTO " + Params.dbName + ".order_group " +
						  " (`order_id`, `sub_order_id`, `restaurant_id`) " +
						  " VALUES (" +
						  parentOrder.getId() + ", " +
						  childOrder.getId() + ", " +
						  term.restaurantID + ")";
					dbCon.stmt.executeUpdate(sql);
					
					//Insert the sub order.
					sql = " INSERT INTO " + Params.dbName + ".sub_order " +
						  " (`order_id`, `table_id`, `table_name`) " + 
						  " VALUES (" +
						  childOrder.getId() + ", " +
						  childOrder.destTbl.tableID + ", " +
						  "'" + childOrder.destTbl.name + "'" + 
						  ")";
					dbCon.stmt.executeUpdate(sql);
				}
				
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
			
			return parentOrder;
			
		}else{
			throw new BusinessException("The order has NOT any child order.");
		}
		
	}
	
	public static void update(Order newGroup) throws SQLException{
		
	}
	
	static void join(Order order, Table tableToJoin) throws SQLException{
		
	}
	
	static void remove(Order order, Table tableToRemove) throws SQLException{
		
	}
	
}
