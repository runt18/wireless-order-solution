package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

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
	public static int exec(Staff term, Table srcTbl, Table destTbl) throws SQLException, BusinessException{
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
	 * @param staff
	 * 			the staff to perform this action
	 * @param srcTbl
	 * 			the source table wants to transfer
	 * @param destTbl
	 * 			the destination table to be transferred
	 * @return the order id associated with the destination table after table transfer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			1 - the source table is IDLE or merged<br>
	 * 			2 - the destination table is BUSY or merged<br>
	 */
	public static int exec(DBCon dbCon, Staff staff, Table srcTbl, Table destTbl) throws SQLException, BusinessException{		
		
		srcTbl = TableDao.getTableByAlias(dbCon, staff, srcTbl.getAliasId());

		destTbl = TableDao.getTableByAlias(dbCon, staff, destTbl.getAliasId());

		/**
		 * Need to assure two conditions before table transfer 
		 * 1 - the source table remains in busy or merged 
		 * 2 - the destination table is idle or merged now
		 */
		if(srcTbl.isMerged()){
			throw new BusinessException("The source table(restaurant_id = " + srcTbl.getRestaurantId() +
										", alias_id=" + srcTbl.getAliasId() + ")" +
										" wants to transfer is merged.", 
										ProtocolError.TABLE_MERGED);
			
		}else if(srcTbl.isIdle()) {
			throw new BusinessException("The source table(restaurant_id = " + srcTbl.getRestaurantId() +
										", alias_id = " + srcTbl.getAliasId() + ")" +
										" wants to transfer is IDLE.",
										ProtocolError.TABLE_IDLE);

		}else if(destTbl.isBusy()) {
			throw new BusinessException("The destination table(restaurant_id = " + destTbl.getRestaurantId() +
										", alias_id = " + destTbl.getAliasId() + ")" +
										" wants to be transferred is BUSY.", 
										ProtocolError.TABLE_BUSY);

		}else if(destTbl.isMerged()){
			throw new BusinessException("The destination table(restaurant_id = " + destTbl.getRestaurantId() +
									    ", alias_id = " + destTbl.getAliasId() + ")" +
									    " wants to be transferred is merged.", 
									    ProtocolError.TABLE_MERGED);

		}else {

			int orderID = OrderDao.getOrderIdByUnPaidTable(dbCon, staff, srcTbl);

			try{
				
				dbCon.conn.setAutoCommit(false);
				
				// update the order
				String sql = " UPDATE "	+ 
							 Params.dbName	+ ".order " +
							 " SET " + 
							 " table_id = " + destTbl.getTableId() + ", " +
							 " table_alias = " + destTbl.getAliasId() + ", " +
							 " table_name = " + "'" + destTbl.getName() + "'" +
							 " WHERE id = " + orderID;
				dbCon.stmt.executeUpdate(sql);

				// Update the destination table to busy
				sql = " UPDATE " + 
					  Params.dbName + ".table SET " +
					  " status = " + Table.Status.BUSY.getVal() + ", " +
					  " category = " + srcTbl.getCategory().getVal() + ", " +
					  " custom_num = " + srcTbl.getCustomNum() + 
					  " WHERE restaurant_id = " + destTbl.getRestaurantId() + 
					  " AND " +
					  " table_alias = " + destTbl.getAliasId();
				
				dbCon.stmt.executeUpdate(sql);
			

				// update the source table status to idle
				sql = " UPDATE " + 
				      Params.dbName + ".table SET " +
				      " status = " + Table.Status.IDLE.getVal() + "," + 
				      " custom_num = NULL," +
					  " category = NULL " + 
				      " WHERE " +
				      " restaurant_id = " + srcTbl.getRestaurantId() + 
				      " AND " +
				      " table_alias = " + srcTbl.getAliasId();
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
