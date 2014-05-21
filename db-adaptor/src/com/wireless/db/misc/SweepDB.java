package com.wireless.db.misc;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.restaurantMgr.Restaurant;

/**
 * This sweep db task is designed to accomplish the goals below.
 * 1 - Sweep the paid order record (the "total_price" is NOT NULL) from "order_history" table 
 * 	   and corresponding order details from "order_food_history" table, whose order date is expired (exceed the "record_alive" in "restaurant" table).
 * 2 - Sweep the shift history record from "shift_history" whose off_duty has been expired (means exceed the "record_alive").
 * 3 - Sweep the daily settle record from "daily_settle_history" whose off_duty has been expired (means exceed the "record_alive"). 
 * 
 * We would use this scheduled task to sweep the expired order record and
 * check to see which foods can be deleted every specific time (maybe 30 days)
 */
public class SweepDB {
	
	public static final class Result{
		private int elapsedTime;					//the elapsed time to sweep db
		private int totalExpiredOrder;				//the expired amount of order
		private int totalExpiredOrderDetail;		//the expired amount of order food
		private int totalExpiredTG;					//the expired amount of taste group records 
		private int totalExpiredNormalTG;			//the expired amount of normal taste group records
		private int totalExpiredShift;				//the expired amount of shift
		private int totalExpiredDailySettle;		//the expired amount of daily settle
		private int totalExpiredMemberOperation;	//the expired amount of member operation
		private int totalExpiredSMSDetail;			//the expired amount of sms detail
		
		@Override
		public String toString(){
			final String sep = System.getProperty("line.separator");
			
			StringBuilder taskInfo = new StringBuilder();
			taskInfo.append("Sweeper task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date())).append(sep);
			taskInfo.append("info : ").append(getTotalExpiredOrderDetail()).append(" record(s) are deleted from \"order_food_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredOrder()).append(" record(s) are deleted from \"order_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredTG()).append(" record(s) are deleted from \"taste_group_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredNormalTG()).append(" record(s) are deleted from \"normal_taste_group_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredShift()).append(" record(s) are deleted from \"shift_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredDailySettle()).append(" record(s) are deleted from \"daily_settle_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredMemberOperation()).append(" record(s) are deleled from \"member_operation_history\" table").append(sep);
			taskInfo.append("info : ").append(getTotalExpiredSMSDetail()).append(" record(s) are deleted from \"sms_detail\" table").append(sep);
			taskInfo.append("info : sweep db takes ").append(getElapsed()).append(" sec.").append(sep);
			
			return taskInfo.toString();
		}

		public int getTotalExpiredSMSDetail(){
			return this.totalExpiredSMSDetail;
		}
		
		public int getTotalExpiredOrder() {
			return totalExpiredOrder;
		}

		public int getTotalExpiredOrderDetail() {
			return totalExpiredOrderDetail;
		}

		public int getTotalExpiredTG() {
			return totalExpiredTG;
		}

		public int getTotalExpiredNormalTG() {
			return totalExpiredNormalTG;
		}

		public int getTotalExpiredShift() {
			return totalExpiredShift;
		}

		public int getTotalExpiredDailySettle() {
			return totalExpiredDailySettle;
		}
		
		public int getTotalExpiredMemberOperation(){
			return totalExpiredMemberOperation;
		}
		
		public int getElapsed(){
			return this.elapsedTime;
		}

	}
	
	public static Result exec() throws SQLException{
		
		Result result = new Result();
		
		DBCon dbCon = new DBCon();
		try{
			
			long beginTime = System.currentTimeMillis();
			
			dbCon.connect();
			
			dbCon.conn.setAutoCommit(false);
						
			String sql;
			
			// Delete the history order which has been expired.
			sql = " DELETE OH FROM " + 
				  Params.dbName + ".order_history AS OH, " +
				  Params.dbName + ".restaurant AS REST " +
				  " WHERE 1 = 1 " +
				  " AND REST.id > " + Restaurant.RESERVED_7 +
				  " AND OH.restaurant_id = REST.id " +
				  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(OH.order_date) > REST.record_alive ";
			
			result.totalExpiredOrder = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history order food which has been expired.
			sql = " DELETE OFH FROM " + 
				  Params.dbName + ".order_food_history AS OFH " +
				  " LEFT JOIN " +
				  Params.dbName + ".order_history AS OH " +
				  " ON OFH.order_id = OH.id " +
				  " WHERE OH.id IS NULL ";
			
			result.totalExpiredOrderDetail = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history taste group which has been expired.
			sql = " DELETE TGH FROM " + 
				  Params.dbName + ".taste_group_history AS TGH " +
				  " LEFT JOIN " +
				  Params.dbName + ".order_food_history AS OFH " +
				  " ON TGH.taste_group_id = OFH.taste_group_id " +
				  " WHERE OFH.taste_group_id IS NULL ";
			
			result.totalExpiredTG = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history normal taste group which has been expired.
			sql = " DELETE NTGH FROM " + 
				  Params.dbName + ".normal_taste_group_history AS NTGH " +
				  " LEFT JOIN " +
				  Params.dbName + ".taste_group_history AS TGH " +
				  " ON NTGH.normal_taste_group_id = TGH.normal_taste_group_id " +
				  " WHERE TGH.normal_taste_group_id IS NULL ";
			result.totalExpiredNormalTG = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history shift which has been expired.
			sql = " DELETE SH FROM " + 
				  Params.dbName + ".shift_history AS SH, " +
				  Params.dbName + ".restaurant AS REST " +
				  " WHERE 1 = 1 " +
				  " AND REST.id > " + Restaurant.RESERVED_7 +
				  " AND SH.restaurant_id = REST.id " +
				  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(SH.off_duty) > REST.record_alive ";
			result.totalExpiredShift = dbCon.stmt.executeUpdate(sql);

			// Delete the history daily shift which has been expired.
			sql = " DELETE DSH FROM " + 
				  Params.dbName + ".daily_settle_history AS DSH, " +
				  Params.dbName + ".restaurant AS REST " +
				  " WHERE 1 = 1 " +
				  " AND REST.id > " + Restaurant.RESERVED_7 +
				  " AND DSH.restaurant_id = REST.id " +
				  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(DSH.off_duty) > REST.record_alive ";
			result.totalExpiredDailySettle = dbCon.stmt.executeUpdate(sql);
			
			// Delete the member operation history which has been expired.
			sql = " DELETE MOH FROM " + 
				  Params.dbName + ".member_operation_history AS MOH, " +
				  Params.dbName + ".restaurant AS REST " +
				  " WHERE 1 = 1 " +
				  " AND REST.id > " + Restaurant.RESERVED_7 +
				  " AND MOH.restaurant_id = REST.id " +
				  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(MOH.operate_date) > REST.record_alive ";
			result.totalExpiredMemberOperation = dbCon.stmt.executeUpdate(sql);
			
			// Delete the SMS details which has been expired.
			sql = " DELETE SMS_D FROM " + 
				  Params.dbName + ".sms_detail SMS_D, " +
				  Params.dbName + ".restaurant AS REST " +
				  " WHERE 1 = 1 " +
				  " AND REST.id > " + Restaurant.RESERVED_7 +
				  " AND SMS_D.restaurant_id = REST.id " +
				  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(SMS_D.modified) > REST.record_alive ";
			result.totalExpiredSMSDetail = dbCon.stmt.executeUpdate(sql);
			
			result.elapsedTime = ((int)(System.currentTimeMillis() - beginTime) / 1000);
			
			dbCon.conn.commit();
			
			return result;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
	}
	
}
