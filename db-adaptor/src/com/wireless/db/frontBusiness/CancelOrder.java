package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.util.DateType;

public class CancelOrder {
	
	/**
	 * Cancel the unpaid order according the specific terminal and table.
	 * @param term
	 * 			the terminal 
	 * @param tableAlias 
	 * 			the table alias id to query
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 						     - The table associated with this order is IDLE.<br>
	 * 							 - The order to this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static void exec(Staff term, int tableAlias) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon, term, tableAlias);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel the unpaid order according the specific terminal and table.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param tableAlias 
	 * 			the table alias id to query
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 						     - The table associated with this order is IDLE.<br>
	 * 							 - The order to this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static void exec(DBCon dbCon, Staff term, int tableAlias) throws BusinessException, SQLException{ 
		
		Table table = TableDao.getTableByAlias(dbCon, term, tableAlias);
		
		int[] unpaidIDs = OrderDao.getOrderIdByUnPaidTable(dbCon, table);
		
		String sql;
		
		if(unpaidIDs.length < 2){			
			/**
			 * Put all the UPDATE, INSERT, DELETE statements into a database transition so as to assure 
			 * the status to both table and order is consistent. 
			 */
			try{
				dbCon.conn.setAutoCommit(false);

				sql = " UPDATE " + Params.dbName + ".table SET " +
					  " status = " + Table.Status.IDLE.getVal() + ", " +
					  " custom_num = NULL, " +
					  " category = NULL " +
					  " WHERE restaurant_id = " + table.getRestaurantId() + 
					  " AND table_alias = " + table.getAliasId();
				dbCon.stmt.executeUpdate(sql);
				
				
				//Delete the records to normal taste group. 
				sql = " DELETE FROM " + Params.dbName + ".normal_taste_group" +
				      " WHERE " +
					  " normal_taste_group_id IN (" +
					  " SELECT normal_taste_group_id " +
					  " FROM " +
					  Params.dbName + ".order_food OF " +
					  " JOIN " +
					  Params.dbName + ".taste_group TG " +
					  " ON " +
					  " OF.taste_group_id = TG.taste_group_id " +
					  " WHERE " + 
					  " OF.order_id = " + unpaidIDs[0] +
					  " AND " +
					  " TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
					  " ) ";
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the records to taste group.
				sql = " DELETE FROM " + Params.dbName + ".taste_group " +
				      " WHERE taste_group_id IN (" +
					  " SELECT taste_group_id " + " FROM " + Params.dbName + ".order_food " +
				      " WHERE " + 
					  " order_id = " + unpaidIDs[0] +
					  " AND " +
					  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
					  " ) ";
				dbCon.stmt.executeUpdate(sql);
				
				//delete the records related to the order id and food id in "order_food" table
				sql = "DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id=" + unpaidIDs[0];
				dbCon.stmt.executeUpdate(sql);
				
				//delete the corresponding order record in "order" table
				sql = "DELETE FROM `" + Params.dbName + "`.`order` WHERE id=" + unpaidIDs[0];
				dbCon.stmt.executeUpdate(sql);
	
				dbCon.conn.commit();
				
			}catch(SQLException e){
				dbCon.conn.rollback();
				throw e;
				
			}catch(Exception e){
				dbCon.conn.rollback();
				throw new SQLException(e);
				
			}finally{
				dbCon.conn.setAutoCommit(true);
			}
			
		}else{
			Order parentOrder = OrderDao.getById(term, unpaidIDs[1], DateType.TODAY);
			try{
				dbCon.conn.setAutoCommit(false);
				for(Order childOrder : parentOrder.getChildOrder()){
					//Set the status to each table of child order to idle 
					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " status = " + Table.Status.IDLE.getVal() + ", " +
						  " custom_num = NULL, " +
						  " category = NULL " +
						  " WHERE restaurant_id = " + table.getRestaurantId() + 
						  " AND table_alias = " + childOrder.getDestTbl().getAliasId();
					dbCon.stmt.executeUpdate(sql);		
					
					//Delete normal taste group to child order. 
					sql = " DELETE FROM " + Params.dbName + ".normal_taste_group" +
					      " WHERE " +
						  " normal_taste_group_id IN (" +
						  " SELECT normal_taste_group_id " +
						  " FROM " +
						  Params.dbName + ".order_food OF " +
						  " JOIN " +
						  Params.dbName + ".taste_group TG " +
						  " ON " +
						  " OF.taste_group_id = TG.taste_group_id " +
						  " WHERE " + 
						  " OF.order_id = " + childOrder.getId() +
						  " AND " +
						  " TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
						  " ) ";
					dbCon.stmt.executeUpdate(sql);
					
					//Delete taste group to each child order.
					sql = " DELETE FROM " + Params.dbName + ".taste_group " +
					      " WHERE taste_group_id IN (" +
						  " SELECT taste_group_id " + " FROM " + Params.dbName + ".order_food " +
					      " WHERE " + 
						  " order_id = " + childOrder.getId() +
						  " AND " +
						  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
						  " ) ";
					dbCon.stmt.executeUpdate(sql);
					
					//delete the order food to each child order
					sql = "DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id=" + childOrder.getId();
					dbCon.stmt.executeUpdate(sql);
					
					//delete the child order
					sql = "DELETE FROM `" + Params.dbName + "`.`order` WHERE id=" + childOrder.getId();
					dbCon.stmt.executeUpdate(sql);
				}

				//Delete the sub order
				sql = " DELETE FROM " + Params.dbName + ".sub_order WHERE order_id IN (" +
					  " SELECT sub_order_id FROM " + Params.dbName + ".order_group WHERE order_id = " + parentOrder.getId() + ")";
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the order group 
				sql = " DELETE FROM " + Params.dbName + ".order_group WHERE order_id = " + parentOrder.getId();
				dbCon.stmt.executeUpdate(sql);				
				
				//Delete the order food to parent order
				sql = "DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id=" + parentOrder.getId();
				dbCon.stmt.executeUpdate(sql);

				//delete the parent order
				sql = "DELETE FROM `" + Params.dbName + "`.`order` WHERE id=" + parentOrder.getId();
				dbCon.stmt.executeUpdate(sql);
				
				dbCon.conn.commit();
				
			}finally{
				dbCon.conn.setAutoCommit(true);
			}
		}
	}
}
