package com.wireless.protocol;

public interface PinGen {
	public int getDeviceId();
	public short getDeviceType();
	
	public final static short BLACK_BERRY = 0x0000; 
	public final static short ANDROID = 0x0001;
	public final static short BROWSER = 0x00FF;
}
