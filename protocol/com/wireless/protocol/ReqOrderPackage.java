package com.wireless.protocol;

import com.wireless.protocol.PinGen;

public class ReqOrderPackage extends ReqPackage{
	
	private static PinGen _gen = null;
	
	public static void setGen(PinGen gen){
		_gen = gen;
	}
	
	public ReqOrderPackage(){
		super();
		header.pin[0] = (byte)(_gen.getDeviceId() & 0x000000FF);
		header.pin[1] = (byte)((_gen.getDeviceId() & 0x0000FF00) >> 8);
		header.pin[2] = (byte)((_gen.getDeviceId() & 0x00FF0000) >> 16);
		header.pin[3] = (byte)((_gen.getDeviceId() & 0xFF000000) >> 24);
		header.pin[4] = (byte)(_gen.getDeviceType() & 0x00FF);
		header.pin[5] = (byte)((_gen.getDeviceType() & 0xFF00) >> 8);
	}	
}
