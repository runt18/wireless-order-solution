package com.wireless.db.printScheme;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PrintSchemeError;
import com.wireless.pojo.printScheme.PrinterConnection;
import com.wireless.pojo.staffMgr.Staff;

public class PrinterConnectionDao {
	
	public static class ExtraCond4ExcludeLocal extends ExtraCond{
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			StringBuilder destCond = new StringBuilder();
			try {
				for (Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces(); n.hasMoreElements();) {
					NetworkInterface e = n.nextElement();
					//System.out.println("Interface: " + e.getName() + ",isLoopback: " + e.isLoopback() + ",isUp: " + e.isUp() + ",isP2P: " + e.isPointToPoint() + ",isVirtual: " + e.isVirtual());
					if(e.isUp()){
						for (Enumeration<InetAddress> a = e.getInetAddresses(); a.hasMoreElements();) {
							InetAddress addr = a.nextElement();
							//System.out.println("  " + addr.getHostAddress());
							if(addr.getAddress().length == 4){
								if(destCond.length() == 0){
									destCond.append("'" + addr.getHostAddress() + "'");
								}else{
									destCond.append(",'" + addr.getHostAddress() + "'");
								}
							}
						}
					}
				}
			} catch (SocketException ignored) {
				ignored.printStackTrace();
			}
			if(destCond.length() != 0){
				extraCond.append(" AND dest NOT IN (" + destCond.toString() + ")");
			}
			//System.out.println(extraCond);
			return extraCond.toString();
		}
	}
	
	public static class ExtraCond{
		private int id;
		private String source;
		private final List<String> destes = new ArrayList<String>();
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setSource(String source){
			this.source = source;
			return this;
		}
		
		public ExtraCond addDest(String dest){
			this.destes.add(dest);
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			if(source != null){
				extraCond.append(" AND source = '" + source + "'");
			}
			StringBuilder destCond = new StringBuilder();
			for(String dest : destes){
				if(destCond.length() == 0){
					destCond.append("'" + dest + "'");
				}else{
					destCond.append(",'" + dest + "'");
				}
			}
			if(destCond.length() != 0){
				extraCond.append(" AND dest IN(" + destCond.toString() + ")");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the print server according to specific builder {@link PrinterConnection#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert print server {@link PrinterConnection#InsertBuilder}
	 * @return the id to print server just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, PrinterConnection.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the print server according to specific builder {@link PrinterConnection#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert print server {@link PrinterConnection#InsertBuilder}
	 * @return the id to print server just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, PrinterConnection.InsertBuilder builder) throws SQLException{
		PrinterConnection ps = builder.build();
		List<PrinterConnection> result = getByCond(dbCon, staff, new ExtraCond().addDest(ps.getDest()));
		String sql;
		if(result.isEmpty()){
			sql = " INSERT INTO " + Params.dbName + ".printer_connection " +
				  " (restaurant_id, source, dest, last_connected) VALUES( " +
				  staff.getRestaurantId() + "," +
				  "'" + ps.getSource() + "'," +
				  "'" + ps.getDest() + "'," +
				  " NOW() " +
				  ")";
			
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			int id = 0;
			if(dbCon.rs.next()){
				id = dbCon.rs.getInt(1);
			}else{
				throw new SQLException("The print server id is NOT generated successfully.");
			}
			dbCon.rs.close();
			
			return id;
			
		}else{
			sql = " UPDATE " + Params.dbName + ".printer_connection SET " +
				  " id = " + result.get(0).getId() +
				  " ,last_connected = NOW() " +
				  " ,source = '" + result.get(0).getSource() + "'" +
				  " WHERE id = " + result.get(0).getId();
			dbCon.stmt.executeUpdate(sql);
			return result.get(0).getId();
		}
	}
	
	/**
	 * Get the print server to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to print server 
	 * @return the print server to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print server to this id does NOT exist
	 */
	public static PrinterConnection getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the print server to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to print server 
	 * @return the print server to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print server to this id does NOT exist
	 */
	public static PrinterConnection getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<PrinterConnection> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(PrintSchemeError.PRINT_SERVER_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the print server to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to print servers
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrinterConnection> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the print server to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to print servers
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrinterConnection> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".printer_connection " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		
		List<PrinterConnection> result = new ArrayList<PrinterConnection>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PrinterConnection ps = new PrinterConnection(dbCon.rs.getInt("id"));
			ps.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			ps.setSource(dbCon.rs.getString("source"));
			ps.setDest(dbCon.rs.getString("dest"));
			ps.setLastConnected(dbCon.rs.getTimestamp("last_connected").getTime());
			result.add(ps);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Delete the print server to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to print server
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print server to this id does NOT exist
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
	 * Delete the print server to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to print server
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print server to this id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(PrintSchemeError.PRINT_SERVER_NOT_EXIST);
		}
	}

	/**
	 * Delete the print server to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to print server deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the print server to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to print server deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(PrinterConnection ps : getByCond(dbCon, staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".printer_connection WHERE id = " + ps.getId();
			dbCon.stmt.executeUpdate(sql);
			amount++;
		}
		return amount;
	}

	public static void deleteLocal(Staff staff) throws SQLException, UnknownHostException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteLocal(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void deleteLocal(DBCon dbCon, Staff staff) throws SQLException, UnknownHostException{
		PrinterConnectionDao.ExtraCond extraCond = new PrinterConnectionDao.ExtraCond();
		try {
			for (Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces(); n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();
				if(e.isLoopback() || !e.isUp()){
					continue;
				}
				for (Enumeration<InetAddress> a = e.getInetAddresses(); a.hasMoreElements();) {
					InetAddress addr = a.nextElement();
					if(addr.getAddress().length == 4){
						extraCond.addDest(addr.getHostAddress());
					}
				}
			}
		} catch (SocketException ignored) {
			ignored.printStackTrace();
		}
		PrinterConnectionDao.deleteByCond(dbCon, staff, extraCond);
	}
}
