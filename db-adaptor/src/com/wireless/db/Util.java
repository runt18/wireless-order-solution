package com.wireless.db;

import java.sql.SQLException;

import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;

public class Util {
	/**
	 * Get the unpaid order id according to the specific table if the table is busy,
	 * otherwise throw a business exception with the TABLE_IDLE error code.
	 * @param dbCon 
	 * 			the database connection
	 * @param table 
	 * 			the table information containing the alias id and associated restaurant id
	 * @return the unpaid order id to this table
	 * @throws BusinessException 
	 * 			Throws if either of cases below.<br>
	 * 			1 - The table to query is IDLE.<br>
	 * 			2 - The order to the this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static int getUnPaidOrderID(DBCon dbCon, Table table) throws BusinessException, SQLException{
		if(table.status == Table.TABLE_IDLE){
			throw new BusinessException("The table(alias_id=" + table.aliasID + ", restaurant_id=" + table.restaurantID + ") to query is IDLE.", ErrorCode.TABLE_IDLE);			
		}else{
			//query the order id associated with the this table
			String sql = "SELECT id FROM `" + Params.dbName + 
						"`.`order` WHERE (table_alias = " + table.aliasID +
						" OR table2_alias = " + table.aliasID + ")" +
						" AND restaurant_id = " + table.restaurantID +
						" AND total_price IS NULL";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				return dbCon.rs.getInt("id");
			}else{
				throw new BusinessException("The un-paid order id to table(alias_id=" + table.aliasID + ", restaurant_id=" + table.restaurantID + ") does NOT exist.", ErrorCode.TABLE_IDLE);
			}			
		}
	}
	
	/**
	 * Calculate the total price according to the type of price tail
	 * @param priceTail
	 * @param totalPrice
	 * @return
	 */
	public static float calcByTail(int priceTail, float totalPrice){
		if(priceTail == Setting.TAIL_DECIMAL_CUT){
			//小数抹零
			return new Float(totalPrice).intValue();
		}else if(priceTail == Setting.TAIL_DECIMAL_ROUND){
			//四舍五入
			return Math.round(totalPrice);
		}else{
			//不处理
			return totalPrice;
		}
	}
}
