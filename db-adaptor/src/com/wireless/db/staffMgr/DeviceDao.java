package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeviceError;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Device.Model;
import com.wireless.pojo.staffMgr.Device.Status;

public class DeviceDao {

	public static class ExtraCond{
		private int id;
		private String deviceId;
		private Device.Status status;
		private int restaurantId;
		private String restaurantName;
		private boolean isOnlyAmount = false;
		
		public ExtraCond setOnlyAmount(boolean onOff){
			this.isOnlyAmount = onOff;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setDeviceId(String deviceId){
			this.deviceId = deviceId;
			return this;
		}
		
		public ExtraCond setStatus(Device.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public ExtraCond setRestaurant(String name){
			this.restaurantName = name;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND DEV.id = " + id);
			}
			if(deviceId != null){
				extraCond.append(" AND DEV.device_id = '" + deviceId + "'" + " AND DEV.device_id_crc = CRC32('" + deviceId + "')");
			}
			if(status != null){
				extraCond.append(" AND DEV.status = " + status.getVal());
			}
			if(restaurantId != 0){
				extraCond.append(" AND DEV.restaurant_id = " + restaurantId);
			}
			if(restaurantName != null){
				extraCond.append(" AND RES.restaurant_name LIKE '%" + restaurantName + "%' ");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get the devices to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantId
	 * 			the restaurant id
	 * @return the devices to this specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Device> getDevicesByRestaurant(DBCon dbCon, int restaurantId) throws SQLException{
		return getByCond(dbCon, new ExtraCond().setRestaurant(restaurantId), null);
	}
	
	/**
	 * Get the working device to specific device id.
	 * @param deviceId
	 * 			the device id
	 * @return the device to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the specific device to search does NOT exist
	 */
	public static Device getWorkingDevices(String deviceId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getWorkingDevices(dbCon, deviceId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the working device to specific device id.
	 * @param dbCon
	 * 			the database connection
	 * @param deviceId
	 * 			the device id
	 * @return the device to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the specific device to search does NOT exist
	 */
	public static Device getWorkingDevices(DBCon dbCon, String deviceId) throws SQLException, BusinessException{
		deviceId = deviceId.trim().toUpperCase(Locale.getDefault());
		List<Device> result = getByCond(dbCon, new ExtraCond().setDeviceId(deviceId).setStatus(Device.Status.WORK), null);
		if(result.isEmpty()){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the device to specific device id.
	 * @param deviceId
	 * 			the device id
	 * @return the device to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the specific device to search does NOT exist
	 */
	public static Device getById(String deviceId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, deviceId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the device to specific device id.
	 * @param dbCon
	 * 			the database connection
	 * @param deviceId
	 * 			the device id
	 * @return the device to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the specific device to search does NOT exist
	 */
	public static Device getById(DBCon dbCon, String deviceId) throws SQLException, BusinessException{
		deviceId = deviceId.trim().toUpperCase(Locale.getDefault());
		List<Device> result = getByCond(dbCon, new ExtraCond().setDeviceId(deviceId), null);
		if(result.isEmpty()){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the device to specific device id.
	 * @param id
	 * 			the device id
	 * @return the device to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the specific device to search does NOT exist
	 */
	public static Device getById(int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the device to specific device id.
	 * @param dbCon
	 * 			the database connection
	 * @param deviceId
	 * 			the device id
	 * @return the device to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the specific device to search does NOT exist
	 */
	public static Device getById(DBCon dbCon, int id) throws SQLException, BusinessException{
		List<Device> result = getByCond(dbCon, new ExtraCond().setId(id), null);
		if(result.isEmpty()){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}

	public static List<Device> getByCond(ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	private static List<Device> getByCond(DBCon dbCon, ExtraCond extraCond, String orderClause) throws SQLException{
		
		String sql;
		sql = " SELECT " +
			  (extraCond.isOnlyAmount ? 
			  " COUNT(*)" : " DEV.id, restaurant_id, device_id, model_id, status, restaurant_name ") + 
			  " FROM " + Params.dbName + ".device DEV " +
			  " JOIN " + Params.dbName + ".restaurant RES " +
		      " ON DEV.restaurant_id = RES.id " +
			  " WHERE 1 = 1 " +
		      (extraCond != null ? extraCond : " ") +
		      (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<Device> result;
		if(extraCond.isOnlyAmount){
			if(dbCon.rs.next()){
				result = Collections.nCopies(dbCon.rs.getInt(1), null);
			}else{
				result = Collections.emptyList();
			}
		}else{
			 result = new ArrayList<Device>();
			while(dbCon.rs.next()){
				Device device = new Device(dbCon.rs.getString("device_id"));
				device.setId(dbCon.rs.getInt("id"));
				device.setModel(Model.valueOf(dbCon.rs.getInt("model_id")));
				device.setStatus(Status.valueOf(dbCon.rs.getInt("status")));
				device.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				device.setRestaurantName(dbCon.rs.getString("restaurant_name"));
				result.add(device);
			}
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Insert a new device.
	 * @param builder
	 * 			the builder to new device
	 * @return the id to the device just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the device id to insert has been EXIST before
	 */
	public static int insert(Device.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new device.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to new device
	 * @return the id to the device just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the device id to insert has been EXIST before
	 */
	public static int insert(DBCon dbCon, Device.InsertBuilder builder) throws SQLException, BusinessException{
		
		Device device = builder.build();
		
		checkDeviceDuplicated(dbCon, device.getDeviceId());
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".device " +
			  " (`restaurant_id`, `device_id`, `device_id_crc`, `model_id`, `status`) " +
			  " VALUES (" +
			  device.getRestaurantId() + "," +
			  "'" + device.getDeviceId() + "'," +
			  "CRC32('" + device.getDeviceId() + "')," +
			  device.getModel().getVal() + "," +
			  device.getStatus().getVal() +
			  " ) ";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of device is not generated successfully.");
		}
	}
	
	/**
	 * Update the device according to a specific id.
	 * @param builder
	 * 			the builder to update a device
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the device to update does NOT exist
	 */
	public static void update(Device.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the device according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to update a device
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the device to update does NOT exist
	 */
	public static void update(DBCon dbCon, Device.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT id FROM " + Params.dbName + ".device WHERE device_id = '" + builder.getDeviceId() + "'" + " AND " + " id <> " + builder.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		try{
			if(dbCon.rs.next()){
				throw new BusinessException(DeviceError.DEVICE_ID_DUPLICATE);
			}
		}finally{
			dbCon.rs.close();
		}
		
		sql = " UPDATE " + Params.dbName + ".device SET " +
			  " device_id = '" + builder.getDeviceId() + "'" +
			  " ,device_id_crc = CRC32('" + builder.getDeviceId() + "')" +
			  " ,restaurant_id = " + builder.getRestaurantId() + 
			  (builder.getModel() != null ? " ,model_id = " + builder.getModel().getVal() : "") +
			  (builder.getStatus() != null ? " ,status = " + builder.getStatus().getVal() : "") +
			  " WHERE id = " + builder.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}
	}
	
	private static void checkDeviceDuplicated(DBCon dbCon, String deviceId) throws BusinessException, SQLException{
		String sql;
		sql = " SELECT id FROM " + Params.dbName + ".device WHERE device_id = '" + deviceId.trim().toUpperCase() + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		try{
			if(dbCon.rs.next()){
				throw new BusinessException(DeviceError.DEVICE_ID_DUPLICATE);
			}
		}finally{
			dbCon.rs.close();
		}
	}
	
	/**
	 * Delete the device to a specific device id.
	 * @param id
	 * 			the device id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the device to this specific id does NOT exist
	 */
	public static void deleteById(int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the device to a specific device id.
	 * @param dbCon
	 * 			the database connection
	 * @param id
	 * 			the device id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the device to this specific id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, int id) throws SQLException, BusinessException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".device WHERE id = " + id + "";
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}
	}

	/**
	 * FIXME
	 * @param deviceId
	 * @param pin
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void insert(String deviceId, String pin) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String sql;
			sql = " SELECT restaurant_id FROM " + Params.dbName + ".terminal " +
				  " WHERE pin = " + "0x" + pin;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			int restaurantId = -1;
			if(dbCon.rs.next()){
				restaurantId = dbCon.rs.getInt("restaurant_id");
			}
			
			dbCon.rs.close();
			
			if(restaurantId > 0){
				Device.InsertBuilder builder = new Device.InsertBuilder(deviceId, restaurantId);
				insert(dbCon, builder);
			}
			
		}finally{		
			dbCon.disconnect();
		}
	}
}
