package com.wireless.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class DeviceUtil {

	public static enum Type{
		MOBILE("mobile"),
		PAD("pad");
		
		private final String desc;
		
		Type(String desc){
			this.desc = desc;
		}
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private DeviceUtil(){
		
	}
	
	public static String getDeviceId(Context context, Type type){
		if(type == Type.MOBILE){
			return ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		}else{
			return ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
		}
	}
	
}
