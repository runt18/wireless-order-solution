package com.wireless.db.restaurantMgr;

import java.sql.ResultSet;
import com.wireless.db.DBCon;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.test.db.TestInit;

public class RestaurantDao {
	/**
	 * 根据餐厅ID查询餐厅信息
	 * @param restaurantID
	 * @return
	 */
	public static Restaurant queryByID(int restaurantID){
		Restaurant restaurant = null;
		try{
			DBCon dbCon = new DBCon();
			String sql = "SELECT * FROM restaurant WHERE restaurant.id = "+restaurantID+";";
			dbCon.connect();
			ResultSet rs = dbCon.stmt.executeQuery(sql);
			while(rs.next()){
				restaurant = new Restaurant();
				restaurant.setAccount(rs.getString("account"));
				restaurant.setId(rs.getInt("id"));
				restaurant.setPwd(rs.getString("pwd"));
				restaurant.setPwd2(rs.getString("pwd2"));
				restaurant.setPwd3(rs.getString("pwd3"));
				restaurant.setPwd4(rs.getString("pwd4"));
				restaurant.setPwd5(rs.getString("pwd5"));
				restaurant.setRecordAlive(rs.getInt("record_alive"));
				restaurant.setRestaurantInfo(rs.getString("restaurant_info"));
				restaurant.setRestaurantName(rs.getString("restaurant_name"));
				restaurant.setTele1(rs.getString("tele1"));
				restaurant.setTele2(rs.getString("tele2"));
				restaurant.setAddress(rs.getString("address"));
				break;
			}
			rs.close();
			dbCon.disconnect();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return restaurant;
	}
	/**
	 * 根据餐厅ID修改餐厅信息
	 * @param restaurant
	 * @return
	 */
	public static boolean update(int restaurantID,Restaurant restaurant){
		boolean success = false;
		try{
			DBCon dbCon = new DBCon();
			String sql = "UPDATE restaurant SET restaurant.restaurant_info = '"+restaurant.getRestaurantInfo()+"',restaurant.restaurant_name='"+restaurant.getRestaurantName()+"',address='"+restaurant.getAddress()+"',restaurant.tele1='"+restaurant.getTele1()+"',restaurant.tele2='"+restaurant.getTele2()+"' WHERE restaurant.id = "+restaurantID+"";
			dbCon.connect();
			int rs = dbCon.stmt.executeUpdate(sql);
			if(rs > 0){
				success = true;
			}
			else{
				success = false; 
			}
			dbCon.disconnect();
		}
		catch(Exception e){
			success = false;
			e.printStackTrace();
		}
		return success;
	}
}
