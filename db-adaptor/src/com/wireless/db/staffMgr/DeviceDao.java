package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
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
		return getDevices(dbCon, " AND DEV.restaurant_id = " + restaurantId, null);
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
	public static Device getWorkingDeviceById(String deviceId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getWorkingDeviceById(dbCon, deviceId);
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
	public static Device getWorkingDeviceById(DBCon dbCon, String deviceId) throws SQLException, BusinessException{
		deviceId = deviceId.trim().toUpperCase(Locale.getDefault());
		List<Device> result = getDevices(dbCon, " AND DEV.device_id = '" + deviceId + "'" + " AND device_id_crc = CRC32(" + deviceId + ")" + " AND status = " + Device.Status.WORK.getVal(), null);
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
	public static Device getDeviceById(String deviceId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDeviceById(dbCon, deviceId);
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
	public static Device getDeviceById(DBCon dbCon, String deviceId) throws SQLException, BusinessException{
		deviceId = deviceId.trim().toUpperCase(Locale.getDefault());
		List<Device> result = getDevices(dbCon, " AND DEV.device_id = '" + deviceId + "'" + " AND DEV.device_id_crc = CRC32('" + deviceId + "')", null);
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
	public static Device getDeviceById(int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDeviceById(dbCon, id);
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
	public static Device getDeviceById(DBCon dbCon, int id) throws SQLException, BusinessException{
		List<Device> result = getDevices(dbCon, " AND DEV.id = " + id, null);
		if(result.isEmpty()){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	private static List<Device> getDevices(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		List<Device> result = new ArrayList<Device>();
		
		String sql;
		sql = " SELECT id, restaurant_id, device_id, model_id, status FROM " + 
		      Params.dbName + ".device DEV" +
			  " WHERE 1 = 1 " +
		      (extraCond != null ? extraCond : " ") +
		      (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Device device = new Device(dbCon.rs.getString("device_id"));
			device.setId(dbCon.rs.getInt("id"));
			device.setModel(Model.valueOf(dbCon.rs.getInt("model_id")));
			device.setStatus(Status.valueOf(dbCon.rs.getInt("status")));
			device.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			result.add(device);
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
