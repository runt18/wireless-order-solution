package com.wireless.test.db.deviceMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.DeviceDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeviceError;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Device.Model;
import com.wireless.pojo.staffMgr.Device.Status;
import com.wireless.test.db.TestInit;

public class TestDeviceDao {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void tesDeviceDao() throws SQLException, BusinessException{
		int deviceId = 0;
		try{
			final String newDeviceId = "ab0310021908696";
			Device.InsertBuilder builder = new Device.InsertBuilder(newDeviceId, 37)
													 .setModel(Model.ANDROID);
			deviceId = DeviceDao.insert(builder);

			Device actual = DeviceDao.getById(newDeviceId);
			
			Device expected = builder.build();
			expected.setId(deviceId);
			compareDevice(expected, actual);
			
			final String modifiedDeviceId = "m6i31a021908696";
			Device.UpdateBuilder updateBuilder = new Device.UpdateBuilder(deviceId, modifiedDeviceId, 37)
														   .setModel(Model.iOS)
														   .setStatus(Status.IDLE);
			DeviceDao.update(updateBuilder);
			
			expected.setDeviceId(modifiedDeviceId);
			expected.setModel(Model.iOS);
			expected.setStatus(Status.IDLE);
			
			actual = DeviceDao.getById(modifiedDeviceId);
			compareDevice(expected, actual);
			
		}finally{
			try{
				DeviceDao.deleteById(deviceId);
				DeviceDao.getById(deviceId);
			}catch(BusinessException e){
				assertEquals("Failed to delete the device", e.getErrCode().getCode(), DeviceError.DEVICE_NOT_EXIST.getCode());
			}
		}
	}
	
	private void compareDevice(Device expected, Device actual){
		assertEquals("device id", expected.getId(), actual.getId());
		assertEquals("device id", expected.getDeviceId(), actual.getDeviceId());
		assertEquals("device model", expected.getModel().getVal(), actual.getModel().getVal());
		assertEquals("device associated restaurant", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("device status", expected.getStatus().getVal(), actual.getStatus().getVal());
	}
}
