package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

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
		public int totalExpiredOrder;				//�ѹ��ڵ��˵���¼��
		public int totalExpiredOrderDetail;			//�ѹ��ڵ��˵���ϸ��¼��
		public int totalExpiredShift;				//�ѹ��ڵĽ����¼��
		public int totalExpiredDailySettle;			//�ѹ��ڵ��ս��¼��
	}
	
	private static class RecAlive{
		int restaurantID = 0;
		long recordAlive = 0;
		RecAlive(int restaurantID, long recordAlive){
			this.restaurantID = restaurantID;
			this.recordAlive = recordAlive;
		}
	}
	
	public static Result exec() throws SQLException{
		
		Result result = new Result();
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			/* Task1 */
			ArrayList<RecAlive> recAlives = new ArrayList<RecAlive>();

			/**
			 * Get all the restaurant id and record alive except the reserved restaurants.
			 * Note that the record alive is zero means never expired.
			 */
			String sql = "SELECT id, record_alive FROM " + Params.dbName + 
						".restaurant WHERE id NOT IN (" + Restaurant.ADMIN + "," + Restaurant.IDLE + "," + Restaurant.DISCARD + "," + 
						Restaurant.RESERVED_1 + "," + Restaurant.RESERVED_2 + "," + Restaurant.RESERVED_3 + "," + Restaurant.RESERVED_4 + "," + 
						Restaurant.RESERVED_5 + "," + Restaurant.RESERVED_6 + "," + Restaurant.RESERVED_7 + 
						") AND record_alive <> 0";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				recAlives.add(new RecAlive(dbCon.rs.getInt("id"), dbCon.rs.getLong("record_alive")));
			}
			
			/**
			 * Get the order id matches the restaurant id and its order record is expired from "order_histroy" table
			 */
			ArrayList<Long> expiredOrders = new ArrayList<Long>();
			for(int i = 0; i < recAlives.size(); i++){
				sql = "SELECT id FROM " + Params.dbName + 
						".order_history WHERE restaurant_id=" + recAlives.get(i).restaurantID + 
						" AND (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(order_date) > " + recAlives.get(i).recordAlive;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					expiredOrders.add(new Long(dbCon.rs.getLong("id")));
				}
			}
			
			dbCon.conn.setAutoCommit(false);


			for(Long expiredOrderID : expiredOrders){
				/**
				 * Delete all the food records matches the order which has been expired from "order_food_hisotry" table.
				 */
				sql = "DELETE FROM " + Params.dbName + 
					  ".order_food_history WHERE order_id=" + expiredOrderID;
				result.totalExpiredOrderDetail += dbCon.stmt.executeUpdate(sql);
				
				/**
				 * Delete all the expired order record from "order_history" table
				 */
				sql = "DELETE FROM " + Params.dbName + 
						  ".order_history WHERE id=" + expiredOrderID;
				result.totalExpiredOrder += dbCon.stmt.executeUpdate(sql);
			}
			
			/**
			 * Delete all the expired shift records from "shift_history"
			 */
			for(RecAlive recAlive : recAlives){
				sql = "DELETE FROM " + Params.dbName + ".shift_history" + 
					  " WHERE " +
					  "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(off_duty)) > " + recAlive.recordAlive +
					  " AND " +
					  "restaurant_id=" + recAlive.restaurantID;
				result.totalExpiredShift += dbCon.stmt.executeUpdate(sql);
			}
			
			/**
			 * Delete all the expired daily settle records from "daily_settle_history"
			 */
			for(RecAlive recAlive : recAlives){
				sql = "DELETE FROM " + Params.dbName + ".daily_settle_history" + 
					  " WHERE " +
					  "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(off_duty)) > " + recAlive.recordAlive +
					  " AND " +
					  "restaurant_id=" + recAlive.restaurantID;
				result.totalExpiredDailySettle += dbCon.stmt.executeUpdate(sql);
			}
			
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
