package com.wireless.protocol;

/**
 * God generate the device id and type that can pass the socket server verification.
 */
public class GodGen implements PinGen {

	public int getDeviceId() {
		return 0xF324B1CF;
	}

	public short getDeviceType() {
		return (short)0xFFFF;
	}

}
