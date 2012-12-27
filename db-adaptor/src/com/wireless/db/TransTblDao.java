package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class TransTblDao {
	
	/**
	 * Perform to transfer table.
	 * @param term
	 * 			the terminal information
	 * @param srcTbl
	 * 			the source table wants to transfer
	 * @param destTbl
	 * 			the destination table to be transferred
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @return the order ID associated with the destination table after table transfer
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			1 - the source table is IDLE or merged<br>
	 * 			2 - the destination table is BUSY or merged<br>
	 */
	public static int exec(Terminal term, Table srcTbl, Table destTbl) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, srcTbl, destTbl);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to transfer table.
	 * Note that database should be connected before invoking this method
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal information
	 * @param srcTbl
	 * 			the source table wants to transfer
	 * @param destTbl
	 * 			the destination table to be transferred
	 * @return the order ID associated with the destination table after table transfer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			1 - the source table is IDLE or merged<br>
	 * 			2 - the destination table is BUSY or merged<br>
	 */
	public static int exec(DBCon dbCon, Terminal term, Table srcTbl, Table destTbl) throws SQLException, BusinessException{		
		
		srcTbl = QueryTable.exec(dbCon, term, srcTbl.aliasID);

		destTbl = QueryTable.exec(dbCon, term, destTbl.aliasID);

		/**
		 * Need to assure two conditions before table transfer 
		 * 1 - the source table remains in busy or merged 
		 * 2 - the destination table is idle or merged now
		 */
		if(srcTbl.isMerged()){
			throw new BusinessException("The source table(restaurant_id=" + srcTbl.restaurantID +
										", alias_id=" + srcTbl.aliasID + ")" +
										" wants to transfer is merged.", ErrorCode.TABLE_MERGED);
			
		}else if(srcTbl.isIdle()) {
			throw new BusinessException("The source table(restaurant_id=" + srcTbl.restaurantID +
										", alias_id=" + srcTbl.aliasID + ")" +
										" wants to transfer is IDLE.", ErrorCode.TABLE_IDLE);

		}else if(destTbl.isBusy()) {
			throw new BusinessException("The destination table(restaurant_id=" + destTbl.restaurantID +
										", alias_id=" + destTbl.aliasID + ")" +
										" wants to be transferred is BUSY.", ErrorCode.TABLE_BUSY);

		}else if(destTbl.isMerged()){
			throw new BusinessException("The destination table(restaurant_id=" + destTbl.restaurantID +
									    ", alias_id=" + destTbl.aliasID + ")" +
									    " wants to be transferred is merged.", ErrorCode.TABLE_MERGED);

		}else {

			int orderID = com.wireless.db.orderMgr.QueryOrderDao.getOrderIdByUnPaidTable(dbCon, srcTbl)[0];

			try{
				
				dbCon.conn.setAutoCommit(false);
				
				// update the order
				String sql = " UPDATE "	+ 
							 Params.dbName	+ ".order SET "	+ 
							 " table_id= " + destTbl.tableID + ", " +
							 " table_alias= " + destTbl.aliasID + " " + 
							 ((destTbl.name == null) ? "" : ", " + " table_name=' " + destTbl.name + "'") + 
							 " WHERE id= " + orderID;
				dbCon.stmt.executeUpdate(sql);

				// Update the destination table to busy
				sql = " UPDATE " + 
					  Params.dbName + ".table SET " +
					  " status = " + Table.TABLE_BUSY + ", " +
					  " category = " + srcTbl.getCategory() + ", " +
					  " custom_num = " + srcTbl.getCustomNum() + 
					  " WHERE restaurant_id = " + destTbl.restaurantID + 
					  " AND " +
					  " table_alias = " + destTbl.aliasID;
				
				dbCon.stmt.executeUpdate(sql);
			

				// update the source table status to idle
				sql = " UPDATE " + 
				      Params.dbName + ".table SET " +
				      " status = " + Table.TABLE_IDLE + "," + 
				      " custom_num = NULL," +
					  " category = NULL " + 
				      " WHERE " +
				      " restaurant_id = " + srcTbl.restaurantID + 
				      " AND " +
				      " table_alias = " + srcTbl.aliasID;
				dbCon.stmt.executeUpdate(sql);
				
				dbCon.conn.commit();

				return orderID;
				
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
	
}
