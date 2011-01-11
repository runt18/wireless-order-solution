package com.wireless.protocol;

import net.rim.device.api.system.DeviceInfo;

public class ReqOrderPackage extends ReqPackage{
	public ReqOrderPackage(){
		super();
		header.pin[0] = (byte)(DeviceInfo.getDeviceId() & 0x000000FF);
		header.pin[1] = (byte)((DeviceInfo.getDeviceId() & 0x0000FF00) >> 8);
		header.pin[2] = (byte)((DeviceInfo.getDeviceId() & 0x00FF0000) >> 16);
		header.pin[3] = (byte)((DeviceInfo.getDeviceId() & 0xFF000000) >> 24);
		header.pin[4] = 0;
		header.pin[5] = 0;
	}
}
