package com.wireless.db.foodMgr;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.foodMgr.Kitchen;
import com.wireless.protocol.Terminal;
public class KitchenDAO{
	public static boolean add(Terminal terminal,Kitchen kitchen) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, terminal, kitchen);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean add(DBCon dbCon,Terminal terminal,Kitchen kitchen) throws SQLException{
		String sql = "INSERT INTO "+Params.dbName+".kitchen("+
				"wireless_order_db.kitchen.kitchen_id,"+
				"wireless_order_db.kitchen.restaurant_id,"+
				"wireless_order_db.kitchen.kitchen_alias,"+
				"wireless_order_db.kitchen.dept_id,"+
				"wireless_order_db.kitchen.name,"+
				"wireless_order_db.kitchen.type,"+
				"wireless_order_db.kitchen.is_allow_temp) values ("+
				"0,"+
				((kitchen.getRestaurantId() != null) ?(kitchen.getRestaurantId()) : "0")+","+
				((kitchen.getKitchenAlias() != null) ?(kitchen.getKitchenAlias()) : "0")+","+
				((kitchen.getDeptId() != null) ?(kitchen.getDeptId()) : "0")+","+
				((kitchen.getName() != null) ?("'"+kitchen.getName()+"'") : "(NULL)")+","+
				((kitchen.getType() != null) ?(kitchen.getType()) : "0")+","+
				((kitchen.getIsAllowTemp() != null) ?(kitchen.getIsAllowTemp()) : "0")+");"+"";
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
	private static boolean remove(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		String sql = "DELETE FROM wireless_order_db.kitchen "+whereCondition;
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static List<Kitchen> query(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return query(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static List<Kitchen> query(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		List<Kitchen> kitchens = new ArrayList<Kitchen>();
		String sql = "SELECT * FROM wireless_order_db.kitchen "+whereCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Kitchen kitchen = new Kitchen();
			kitchen.setKitchenId((Integer)dbCon.rs.getObject("kitchen_id"));
			kitchen.setRestaurantId((Integer)dbCon.rs.getInt("restaurant_id"));
			kitchen.setKitchenAlias((Integer)dbCon.rs.getObject("kitchen_alias"));
			kitchen.setDeptId((Integer)dbCon.rs.getObject("dept_id"));
			kitchen.setName((String)dbCon.rs.getObject("name"));
			kitchen.setType((Integer)dbCon.rs.getObject("type"));
			kitchen.setIsAllowTemp((Integer)dbCon.rs.getObject("is_allow_temp"));
			kitchens.add(kitchen);
		}
		return kitchens;
	}
	public static boolean update(Terminal terminal,Kitchen kitchen,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return update(dbCon, terminal, kitchen, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean update(DBCon dbCon,Terminal terminal,Kitchen kitchen,String whereCondition) throws SQLException{
		String sql = "UPDATE wireless_order_db.kitchen SET "+
				"wireless_order_db.kitchen.restaurant_id = "+((kitchen.getRestaurantId() != null) ?(kitchen.getRestaurantId()) : "0")+","+
				"wireless_order_db.kitchen.kitchen_alias = "+((kitchen.getKitchenAlias() != null) ?(kitchen.getKitchenAlias()) : "0")+","+
				"wireless_order_db.kitchen.dept_id = "+((kitchen.getDeptId() != null) ?(kitchen.getDeptId()) : "0")+","+
				"wireless_order_db.kitchen.name = "+((kitchen.getName() != null) ?("'"+kitchen.getName()+"'") : "(NULL)")+","+
				"wireless_order_db.kitchen.type = "+((kitchen.getType() != null) ?(kitchen.getType()) : "0")+","+
				"wireless_order_db.kitchen.is_allow_temp = "+((kitchen.getIsAllowTemp() != null) ?(kitchen.getIsAllowTemp()) : "0")+" "+whereCondition+";";
		return dbCon.stmt.executeUpdate(sql)>0;
	}
}
