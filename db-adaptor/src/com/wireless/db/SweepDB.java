package com.wireless.db;

import java.sql.SQLException;

import com.wireless.protocol.Restaurant;

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
	
	public static class Result{
		public int totalExpiredOrder;				//the expired amount of order
		public int totalExpiredOrderDetail;			//the expired amount of order food
		public int totalExpiredOrderGroup;			//the expired amount of order group
		public int totalExpiredSubOrder;			//the expired amount of sub order
		public int totalExpiredTG;					//the expired amount of taste group records 
		public int totalExpiredNormalTG;			//the expired amount of normal taste group records
		public int totalExpiredShift;				//the expired amount of shift
		public int totalExpiredDailySettle;			//the expired amount of daily settle
		
		@Override
		public String toString(){
			return "expired order: " + totalExpiredOrder +
				   ", expired order detail: " + totalExpiredOrderDetail +
				   ", expired order group : " + totalExpiredOrderGroup +
				   ", expired sub order: " + totalExpiredSubOrder +
				   ", expired taste group: " + totalExpiredTG +
				   ", expired normal taste group: " + totalExpiredNormalTG +
				   ", expired shift: " + totalExpiredShift +
				   ", expired daily shift: " + totalExpiredDailySettle;
		}
	}
	
	public static Result exec() throws SQLException{
		
		Result result = new Result();
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			dbCon.conn.setAutoCommit(false);
						
			String sql;
			
			// Delete the history order which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".order_history " +
				  " WHERE id IN ( " +
						" SELECT OH_TO_REMOVE.id FROM (" + 
							" SELECT OH.id FROM " + Params.dbName + 
								".order_history OH, " +
								" ( SELECT id AS restaurant_id, record_alive FROM " + Params.dbName + ".restaurant " +
								" WHERE id > " + Restaurant.RESERVED_7 + " AND " + " record_alive <> 0 ) AS REST " +
								" WHERE 1 = 1 " +
								" AND OH.restaurant_id = REST.restaurant_id " +
								" AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(OH.order_date) > REST.record_alive ) AS OH_TO_REMOVE " + 
							")"; 
			
			result.totalExpiredOrder = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history order food which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".order_food_history " +
				  " WHERE id IN (" +
						" SELECT OF_TO_REMOVE.id FROM " + "(" +
				  			" SELECT id FROM " + Params.dbName + ".order_food_history OFH " + 
				  			" WHERE order_id NOT IN ( SELECT id FROM " + Params.dbName + ".order_history )) AS OF_TO_REMOVE " +
				  		")";
			
			result.totalExpiredOrderDetail = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history taste group which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".taste_group_history " +
				  " WHERE taste_group_id IN ( " +
						" SELECT TG_TO_REMOVE.taste_group_id FROM " + "(" +
							" SELECT taste_group_id FROM " + Params.dbName + ".taste_group_history TGH " +
							" WHERE taste_group_id NOT IN ( SELECT taste_group_id FROM " + Params.dbName + ".order_food_history )) AS TG_TO_REMOVE " +
						")";
			result.totalExpiredTG = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history normal taste group which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".normal_taste_group_history " +
				  " WHERE normal_taste_group_id IN ( " +
				  		" SELECT NTG_TO_REMOVE.normal_taste_group_id FROM " + "(" +
				  			" SELECT normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group_history NTGH " +
				  			" WHERE normal_taste_group_id NOT IN ( SELECT normal_taste_group_id FROM " + Params.dbName + ".taste_group_history)) AS NTG_TO_REMOVE " +
				  		")";
			result.totalExpiredNormalTG = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history order group which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".order_group_history " +
				  " WHERE order_id IN ( " +
						" SELECT OG_TO_REMOVE.order_id FROM " + "(" +
							" SELECT order_id FROM " + Params.dbName + ".order_group_history " +
							" WHERE order_id NOT IN ( SELECT id FROM " + Params.dbName + ".order_history)) AS OG_TO_REMOVE " +
						")";
			result.totalExpiredOrderGroup = dbCon.stmt.executeUpdate(sql);

			// Delete the history sub order which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".sub_order_history " +
				  " WHERE order_id IN ( " +
						" SELECT SO_TO_REMOVE.order_id FROM (" +
							" SELECT order_id FROM " + Params.dbName + ".sub_order_history " +
							" WHERE order_id NOT IN ( SELECT sub_order_id FROM " + Params.dbName + ".order_group_history)) AS SO_TO_REMOVE " +
						")";
			result.totalExpiredSubOrder = dbCon.stmt.executeUpdate(sql);
			
			// Delete the history shift which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".shift_history " +
					  " WHERE id IN ( " +
							" SELECT SH_TO_REMOVE.id FROM (" + 
								" SELECT SH.id FROM " + Params.dbName + 
									".shift_history SH, " +
									" ( SELECT id AS restaurant_id, record_alive FROM " + Params.dbName + ".restaurant " +
									" WHERE id > " + Restaurant.RESERVED_7 + " AND " + " record_alive <> 0 ) AS REST " +
									" WHERE 1 = 1 " +
									" AND SH.restaurant_id = REST.restaurant_id " +
									" AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(SH.off_duty) > REST.record_alive ) AS SH_TO_REMOVE " + 
								")";
			result.totalExpiredShift = dbCon.stmt.executeUpdate(sql);

			// Delete the history daily shift which has been expired.
			sql = " DELETE FROM " + Params.dbName + ".daily_settle_history " +
					  " WHERE id IN ( " +
							" SELECT DSH_TO_REMOVE.id FROM (" + 
								" SELECT DSH.id FROM " + Params.dbName + 
									".daily_settle_history DSH, " +
									" ( SELECT id AS restaurant_id, record_alive FROM " + Params.dbName + ".restaurant " +
									" WHERE id > " + Restaurant.RESERVED_7 + " AND " + " record_alive <> 0 ) AS REST " +
									" WHERE 1 = 1 " +
									" AND DSH.restaurant_id = REST.restaurant_id " +
									" AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(DSH.off_duty) > REST.record_alive ) AS DSH_TO_REMOVE " + 
								")";
			result.totalExpiredDailySettle = dbCon.stmt.executeUpdate(sql);
			
			
			/**
			 * Delete all the expired shift records from "shift_history"
			 */
//			for(RecAlive recAlive : recAlives){
//				sql = "DELETE FROM " + Params.dbName + ".shift_history" + 
//					  " WHERE " +
//					  "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(off_duty)) > " + recAlive.recordAlive +
//					  " AND " +
//					  "restaurant_id=" + recAlive.restaurantID;
//				result.totalExpiredShift += dbCon.stmt.executeUpdate(sql);
//			}
//			
//			/**
//			 * Delete all the expired daily settle records from "daily_settle_history"
//			 */
//			for(RecAlive recAlive : recAlives){
//				sql = "DELETE FROM " + Params.dbName + ".daily_settle_history" + 
//					  " WHERE " +
//					  "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(off_duty)) > " + recAlive.recordAlive +
//					  " AND " +
//					  "restaurant_id=" + recAlive.restaurantID;
//				result.totalExpiredDailySettle += dbCon.stmt.executeUpdate(sql);
//			}
			
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
