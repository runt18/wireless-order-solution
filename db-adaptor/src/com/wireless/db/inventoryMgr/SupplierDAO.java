package com.wireless.db.inventoryMgr;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.inventoryMgr.Supplier;
import com.wireless.protocol.Terminal;
public class SupplierDAO{
	public static boolean add(Terminal terminal,Supplier supplier) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, terminal, supplier);
		}
		finally{
			dbCon.disconnect();
		}
		
	}
	private static boolean add(DBCon dbCon,Terminal terminal,Supplier supplier) throws SQLException{
		supplier.setRestaurantId(terminal.restaurantID);
		String sql = "INSERT INTO "+Params.dbName+".supplier("+
								"wireless_order_db.supplier.supplier_id,"+
								"wireless_order_db.supplier.restaurant_id,"+
								"wireless_order_db.supplier.name,"+
								"wireless_order_db.supplier.tele,"+
								"wireless_order_db.supplier.addr,"+
								"wireless_order_db.supplier.contact,"+
								"wireless_order_db.supplier.comment) values ("+
								"0,"+
								((supplier.getRestaurantId() != null) ?(supplier.getRestaurantId()) : "0")+","+
								((supplier.getName() != null) ?("'"+supplier.getName()+"'") : "(NULL)")+","+
								((supplier.getTele() != null) ?("'"+supplier.getTele()+"'") : "(NULL)")+","+
								((supplier.getAddr() != null) ?("'"+supplier.getAddr()+"'") : "(NULL)")+","+
								((supplier.getContact() != null) ?("'"+supplier.getContact()+"'") : "(NULL)")+","+
								((supplier.getComment() != null) ?("'"+supplier.getComment()+"'") : "(NULL)")+");"+"";
		
		return dbCon.stmt.executeUpdate(sql)>0;
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
		String sql = "DELETE FROM wireless_order_db.supplier "+whereCondition;
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static List<Supplier> query(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return query(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static List<Supplier> query(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		List<Supplier> suppliers = new ArrayList<Supplier>();
		String sql = "SELECT * FROM wireless_order_db.supplier "+whereCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Supplier supplier = new Supplier();
			supplier.setSupplierId((Integer)dbCon.rs.getObject("supplier_id"));
			supplier.setRestaurantId((Integer)dbCon.rs.getInt("restaurant_id"));
			supplier.setName((String)dbCon.rs.getObject("name"));
			supplier.setTele((String)dbCon.rs.getObject("tele"));
			supplier.setAddr((String)dbCon.rs.getObject("addr"));
			supplier.setContact((String)dbCon.rs.getObject("contact"));
			supplier.setComment((String)dbCon.rs.getObject("comment"));
			suppliers.add(supplier);

		}
		return suppliers;
	}
	public static boolean update(Terminal terminal,Supplier supplier,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return update(dbCon, terminal, supplier, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean update(DBCon dbCon,Terminal terminal,Supplier supplier,String whereCondition) throws SQLException{
		supplier.setRestaurantId(terminal.restaurantID);
		String sql = "UPDATE wireless_order_db.supplier SET "+
								"wireless_order_db.supplier.restaurant_id = "+((supplier.getRestaurantId() != null) ?(supplier.getRestaurantId()) : "0")+","+
								"wireless_order_db.supplier.name = "+((supplier.getName() != null) ?("'"+supplier.getName()+"'") : "(NULL)")+","+
								"wireless_order_db.supplier.tele = "+((supplier.getTele() != null) ?("'"+supplier.getTele()+"'") : "(NULL)")+","+
								"wireless_order_db.supplier.addr = "+((supplier.getAddr() != null) ?("'"+supplier.getAddr()+"'") : "(NULL)")+","+
								"wireless_order_db.supplier.contact = "+((supplier.getContact() != null) ?("'"+supplier.getContact()+"'") : "(NULL)")+","+
								"wireless_order_db.supplier.comment = "+((supplier.getComment() != null) ?("'"+supplier.getComment()+"'") : "(NULL)")+" "+whereCondition+";";
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static boolean deleteByID(Terminal terminal,String id) throws SQLException{
		return remove(terminal, " WHERE "+Supplier.TableFields.SUPPLIER_ID+" = "+id+";");
	}
}
