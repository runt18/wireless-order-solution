package com.wireless.db.foodMgr;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.foodMgr.Food;
import com.wireless.protocol.Terminal;
public class FoodDAO{
	public static boolean add(Terminal terminal,Food food) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, terminal, food);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean add(DBCon dbCon,Terminal terminal,Food food) throws SQLException{
		String sql = "INSERT INTO "+Params.dbName+".food("+
							"wireless_order_db.food.food_id,"+
							"wireless_order_db.food.food_alias,"+
							"wireless_order_db.food.restaurant_id,"+
							"wireless_order_db.food.name,"+
							"wireless_order_db.food.pinyin,"+
							"wireless_order_db.food.kitchen_id,"+
							"wireless_order_db.food.kitchen_alias,"+
							"wireless_order_db.food.status,"+
							"wireless_order_db.food.stock_status,"+
							"wireless_order_db.food.taste_ref_type,"+
							"wireless_order_db.food.desc,"+
							"wireless_order_db.food.img) values ("+
							"0,"+
							((food.getFoodAlias() != null) ?(food.getFoodAlias()) : "0")+","+
							((food.getRestaurantId() != null) ?(food.getRestaurantId()) : "0")+","+
							((food.getName() != null) ?("'"+food.getName()+"'") : "(NULL)")+","+
							((food.getPinyin() != null) ?("'"+food.getPinyin()+"'") : "(NULL)")+","+
							((food.getKitchenId() != null) ?(food.getKitchenId()) : "0")+","+
							((food.getKitchenAlias() != null) ?(food.getKitchenAlias()) : "0")+","+
							((food.getStatus() != null) ?(food.getStatus()) : "0")+","+
							((food.getStockStatus() != null) ?(food.getStockStatus()) : "0")+","+
							((food.getTasteRefType() != null) ?(food.getTasteRefType()) : "0")+","+
							((food.getDesc() != null) ?("'"+food.getDesc()+"'") : "(NULL)")+","+
							((food.getImg() != null) ?("'"+food.getImg()+"'") : "(NULL)")+");";
		return dbCon.stmt.execute(sql);
	}
	public static boolean remove(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return remove(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean remove(DBCon dbCon,Terminal terminal,String whereCondition)  throws SQLException{
		String sql = "DELETE FROM wireless_order_db.food "+whereCondition;
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static List<Food> query(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return query(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static List<Food> query(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		List<Food> foods = new ArrayList<Food>();
		String sql = "SELECT F.*,K.name AS kname FROM wireless_order_db.food F,wireless_order_db.kitchen K  WHERE K.kitchen_id = F.kitchen_id "+whereCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Food food = new Food();
			food.setFoodId((Integer)dbCon.rs.getObject("food_id"));
			food.setFoodAlias((Integer)dbCon.rs.getObject("food_alias"));
			food.setRestaurantId((Integer)dbCon.rs.getInt("restaurant_id"));
			food.setName((String)dbCon.rs.getObject("name"));
			food.setPinyin((String)dbCon.rs.getObject("pinyin"));
			food.setKitchenId((Integer)dbCon.rs.getObject("kitchen_id"));
			food.setKname((String)dbCon.rs.getObject("kname"));
			food.setKitchenAlias((Integer)dbCon.rs.getObject("kitchen_alias"));
			food.setStatus((Integer)dbCon.rs.getObject("status"));
			food.setStockStatus((Integer)dbCon.rs.getObject("stock_status"));
			food.setTasteRefType((Integer)dbCon.rs.getObject("taste_ref_type"));
			food.setDesc((String)dbCon.rs.getObject("desc"));
			food.setImg((String)dbCon.rs.getObject("img"));
			foods.add(food);
		}
		return foods;
	}
	public static boolean update(Terminal terminal,Food food,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return update(dbCon, terminal, food, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean update(DBCon dbCon,Terminal terminal,Food food,String whereCondition)  throws SQLException{
		String sql = "UPDATE wireless_order_db.food SET "+
							"wireless_order_db.food.food_alias = "+((food.getFoodAlias() != null) ?(food.getFoodAlias()) : "0")+","+
							"wireless_order_db.food.restaurant_id = "+((food.getRestaurantId() != null) ?(food.getRestaurantId()) : "0")+","+
							"wireless_order_db.food.name = "+((food.getName() != null) ?("'"+food.getName()+"'") : "(NULL)")+","+
							"wireless_order_db.food.pinyin = "+((food.getPinyin() != null) ?("'"+food.getPinyin()+"'") : "(NULL)")+","+
							"wireless_order_db.food.kitchen_id = "+((food.getKitchenId() != null) ?(food.getKitchenId()) : "0")+","+
							"wireless_order_db.food.kitchen_alias = "+((food.getKitchenAlias() != null) ?(food.getKitchenAlias()) : "0")+","+
							"wireless_order_db.food.status = "+((food.getStatus() != null) ?(food.getStatus()) : "0")+","+
							"wireless_order_db.food.stock_status = "+((food.getStockStatus() != null) ?(food.getStockStatus()) : "0")+","+
							"wireless_order_db.food.taste_ref_type = "+((food.getTasteRefType() != null) ?(food.getTasteRefType()) : "0")+","+
							"wireless_order_db.food.desc = "+((food.getDesc() != null) ?("'"+food.getDesc()+"'") : "(NULL)")+","+
							"wireless_order_db.food.img = "+((food.getImg() != null) ?("'"+food.getImg()+"'") : "(NULL)")+" "+whereCondition+";";
		return dbCon.stmt.executeUpdate(sql)>0;
	}
}
