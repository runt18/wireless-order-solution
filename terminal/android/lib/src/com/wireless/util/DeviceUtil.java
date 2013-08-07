package com.wireless.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class DeviceUtil {

	private DeviceUtil(){
		
	}
	
	public static String getDeviceId(Context context){
		String deviceId = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if(deviceId == null){
			deviceId = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
		}
		return deviceId;
	}
	
}
