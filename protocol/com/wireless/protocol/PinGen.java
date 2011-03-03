package com.wireless.protocol;

public interface PinGen {
	public int getDeviceId();
	public short getDeviceType();
	
	public final static short BLACK_BERRY = 0x0000; 
}
