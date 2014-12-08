package com.wireless.db.printScheme;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PrintSchemeError;
import com.wireless.pojo.printScheme.PrintLoss;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class PrintLossDao {

	public static class ExtraCond{
		private int id;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND print_loss_id = " + id);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the print loss according to specific builder {@link PrintLoss#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert print loss
	 * @return the id to print loss
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, PrintLoss.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the print loss according to specific builder {@link PrintLoss#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert print loss
	 * @return the id to print loss
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, PrintLoss.InsertBuilder builder) throws SQLException{
		PrintLoss loss = builder.build();
		
		String sql = " INSERT INTO " + Params.dbName + ".print_loss (`restaurant_id`, `content`, `birth_date`) VALUES (?, ?, ?)";
        PreparedStatement pstmt = dbCon.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, staff.getRestaurantId());
        pstmt.setBytes(2, loss.getContent());
        pstmt.setString(3, DateUtil.format(System.currentTimeMillis(), DateUtil.Pattern.DATE_TIME));
        pstmt.executeUpdate();
        
		dbCon.rs = pstmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the print loss id.");
		}
		dbCon.rs.close();
		
		return id;
	}
	
	/**
	 * Get the print loss to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to print loss
	 * @return the print loss to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print loss to this id does NOT exist
	 */
	public static PrintLoss getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the print loss to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to print loss
	 * @return the print loss to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print loss to this id does NOT exist
	 */
	public static PrintLoss getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<PrintLoss> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(PrintSchemeError.PRINT_LOSS_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the print loss to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to print losses
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrintLoss> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql; 
		sql = " SELECT * FROM " + Params.dbName + ".print_loss WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() + 
			  (extraCond != null ? extraCond.toString() : "");
		
		List<PrintLoss> result = new ArrayList<PrintLoss>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PrintLoss loss = new PrintLoss(dbCon.rs.getInt("print_loss_id"));
			loss.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			Blob content = dbCon.rs.getBlob("content");
			loss.setContent(content.getBytes(1L, (int)content.length()));
			loss.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			result.add(loss);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Delete the print loss to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id 
	 * 			the id to print loss
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print loss to this id does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the print loss to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id 
	 * 			the id to print loss
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print loss to this id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(PrintSchemeError.PRINT_LOSS_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the print loss according to specific extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to print losses deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(PrintLoss loss : getByCond(dbCon, staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".print_loss WHERE print_loss_id = " + loss.getId();
			dbCon.stmt.executeUpdate(sql);
			amount++;
		}
		return amount;
	}
	
	public static class StatResult{
		final Map<String, Integer> details = new HashMap<>();
		private StatResult(){
			
		}
		@Override
		public String toString(){
			int restaurantAmount = details.size();
			int lossAmount = 0;
			StringBuilder detail = new StringBuilder();
			for(Entry<String, Integer> entry : details.entrySet()){
				lossAmount += entry.getValue();
				detail.append("|-").append(entry.getKey() + ", " + entry.getValue()).append(System.getProperty("line.separator"));
			}
			
			StringBuilder status = new StringBuilder();
			status.append(restaurantAmount + " restaurant(s), " + lossAmount + " receipt(s)").append(System.getProperty("line.separator"))
				  .append(detail.toString());
			
			return status.toString();
		}
	}
	
	public static StatResult stat() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String sql;
			sql = " SELECT R.restaurant_name, COUNT(*) AS print_loss_amount " + 
				  " FROM " + Params.dbName + ".print_loss P " +
				  " JOIN " + Params.dbName + ".restaurant R ON P.restaurant_id = R.id " +
				  " GROUP BY P.restaurant_id " +
				  " ORDER BY P.restaurant_id ";
			
			StatResult result = new StatResult();
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				result.details.put(dbCon.rs.getString("restaurant_name"), dbCon.rs.getInt("print_loss_amount"));
			}
			dbCon.rs.close();
			
			return result;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Clean up the print losses.
	 * @return the amount of print losses to clean up 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int cleanup() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".print_loss";
			return dbCon.stmt.executeUpdate(sql);
		}finally{
			dbCon.disconnect();
		}
	}
}

