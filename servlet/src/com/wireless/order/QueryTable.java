package com.wireless.order;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.wireless.protocol.Table;


public class QueryTable {
	

	public static Table[] exec(String pin) throws Exception{
		
		int restaurantID = Verify.exec(pin);
		
		//open the database
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {   		
			
			Class.forName("com.mysql.jdbc.Driver");   
		
			dbCon = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
			stmt = dbCon.createStatement();   	
			
			ArrayList<Table> tables = new ArrayList<Table>();
			
			//get the idle table 
			String sql = "SELECT table_id FROM " + Params.dbName + ".order a WHERE id = (SELECT max(id) FROM " + 
						Params.dbName + ".order b WHERE a.table_id = b.table_id AND a.restaurant_id=b.restaurant_id) " +
						"AND total_price IS NOT NULL AND restaurant_id=" + restaurantID;
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				Table idleTable = new Table();
				idleTable.alias_id = rs.getShort(1);
				idleTable.status = Table.TABLE_IDLE;
				tables.add(idleTable);
			}
			
			sql = "SELECT alias_id FROM " + Params.dbName + ".table WHERE alias_id NOT IN (SELECT distinct table_id FROM " +
					Params.dbName + ".order WHERE restaurant_id=" + restaurantID + ")" + " AND restaurant_id=" + restaurantID;
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				Table idleTable = new Table();
				idleTable.alias_id = rs.getShort(1);
				tables.add(idleTable);
			}
			
			sql = "SELECT table_id, custom_num FROM " + Params.dbName + ".order a WHERE id=(SELECT max(id) FROM " + Params.dbName + 
				".order b WHERE a.table_id = b.table_id and a.restaurant_id=b.restaurant_id) and total_price IS NULL AND restaurant_id="+
				restaurantID;
			
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				Table busyTable = new Table();
				busyTable.alias_id = rs.getShort("table_id");
				busyTable.custom_num = rs.getShort("custom_num");
				busyTable.status = Table.TABLE_BUSY;
				tables.add(busyTable);
			}			

			return tables.toArray(new Table[tables.size()]);
			
		}catch(ClassNotFoundException e){
			e.printStackTrace();
			throw new Exception("获取餐台信息不成功");
			
		}catch(SQLException e){
			e.printStackTrace();
			throw new Exception("获取餐台信息不成功");

		}finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(stmt != null){
					stmt.close();
					stmt = null;
				}
				if(dbCon != null){
					dbCon.close();
					dbCon = null;
				}
			}catch(SQLException e){
				System.err.println(e.toString());
			}
		}
	}
	
}
