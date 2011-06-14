package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;

class Util {
	/**
	 * Get the unpaid order id according to the specific table if the table is busy,
	 * otherwise throw a business exception with the TABLE_IDLE error code.
	 * @param dbCon the db connection
	 * @param table the table information containing the alias id and associated restaurant id
	 * @return the unpaid order id to this table
	 * @throws BusinessException throws if the table to query is idle
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	static int getUnPaidOrderID(DBCon dbCon, Table table) throws BusinessException, SQLException{
		//query the order id associated with the this table
		String sql = "SELECT id FROM `" + Params.dbName + 
					"`.`order` WHERE table_id = " + table.alias_id +
					" OR table2_id = " + table.alias_id + 
					" AND restaurant_id = " + table.restaurant_id +
					" AND total_price IS NULL";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			return dbCon.rs.getInt("id");
		}else{
			throw new BusinessException("The table(alias_id=" + table.alias_id + ") to query is idle.", ErrorCode.TABLE_IDLE);
		}
	}
}
