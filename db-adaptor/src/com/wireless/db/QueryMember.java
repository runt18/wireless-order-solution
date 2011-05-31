package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Terminal;

public class QueryMember {
	
	/**
	 * Query the member information according to the specific restaurant and member id.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param memberID the member id to query
	 * @return the member detail information
	 * @throws BusinessException throws if one the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The member to query does NOT exist.
	 * @throws SQLException
	 */
	public static Member exec(int pin, short model, String memberID) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();

		try {   
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return exec(dbCon, term.restaurant_id, memberID);
			
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Query the member information according to the specific restaurant and member id.
	 * @param dbCon the database connection
	 * @param restaurantID the restaurant that member belong to
	 * @param memberID the member id to query
	 * @return the member information
	 * @throws BusinessException throws if the member does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	static Member exec(DBCon dbCon, int restaurantID, String memberID) throws BusinessException, SQLException{
		try{
			String sql = "SELECT name, tele, balance FROM " + Params.dbName + ".member WHERE restaurant_id=" +
			 			 restaurantID + " AND alias_id=" +
			 			 memberID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);

			if(dbCon.rs.next()){
				return new Member(memberID,
								  dbCon.rs.getString("name"),
								  dbCon.rs.getString("tele"),
								  dbCon.rs.getFloat("balance"));
			}else{
				throw new BusinessException("The member(id=" + memberID + ") does NOT exist.", ErrorCode.MEMBER_NOT_EXIST);
			}
				
		}finally{
			dbCon.rs.close();
		}
	}
	
}
