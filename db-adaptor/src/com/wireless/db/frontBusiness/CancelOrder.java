package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteGroup;

public class CancelOrder {
	
	/**
	 * Cancel the unpaid order according the specific terminal and table.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableAlias 
	 * 			the table alias id to query
	 * @throws BusinessException 
	 * 			throws if one of the cases below<br>
	 * 			<li>the terminal is NOT attached to any restaurant<br>
	 * 			<li>the terminal is expired<br>
	 * 			<li>the table associated with this order is IDLE<br>
	 * 			<li>the order to this table does NOT exist<br>
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement.
	 */
	public static void execByTable(Staff staff, int tableAlias) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			execByTable(dbCon, staff, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel the unpaid order according the specific terminal and table.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
	 * @param tableAlias 
	 * 			the table alias id to query
	 * @throws BusinessException 
	 * 			throws if one of the cases below.<br>
	 * 			<li>the terminal is NOT attached to any restaurant.<br>
	 * 		 	<li>the terminal is expired.<br>
	 * 			<li>the table associated with this order is IDLE.<br>
	 * 			<li>the order to this table does NOT exist.<br>
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement.
	 */
	public static void execByTable(DBCon dbCon, Staff staff, int tableAlias) throws BusinessException, SQLException{ 
		
		Table table = TableDao.getTableByAlias(dbCon, staff, tableAlias);
		
		int orderId = OrderDao.getOrderIdByUnPaidTable(dbCon, staff, table);
		
		String sql;
		
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
				  " OF.order_id = " + orderId +
				  " AND " +
				  " TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the records to taste group.
			sql = " DELETE FROM " + Params.dbName + ".taste_group " +
			      " WHERE taste_group_id IN (" +
				  " SELECT taste_group_id " + " FROM " + Params.dbName + ".order_food " +
			      " WHERE " + 
				  " order_id = " + orderId +
				  " AND " +
				  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//delete the records related to the order id and food id in "order_food" table
			sql = "DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id=" + orderId;
			dbCon.stmt.executeUpdate(sql);
			
			//delete the corresponding order record in "order" table
			sql = "DELETE FROM `" + Params.dbName + "`.`order` WHERE id=" + orderId;
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
			
	}
}
