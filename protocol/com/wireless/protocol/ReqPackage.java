package com.wireless.protocol;

public class ReqPackage extends ProtocolPackage{
	private static Object syncObj = new Object();
	private static byte seq = Byte.MIN_VALUE;
	
	private static PinGen _gen = null;
	
	public static void setGen(PinGen gen){
		_gen = gen;
	}
	
	public static PinGen getGen(){
		return _gen;
	}
	
	public ReqPackage(){
		synchronized(syncObj){
			if(++seq == Byte.MAX_VALUE){
				seq = Byte.MIN_VALUE;
			}
		}
		header.seq = seq;
		
		if(_gen != null){
			header.pin[0] = (byte)(_gen.getDeviceId() & 0x000000FF);
			header.pin[1] = (byte)((_gen.getDeviceId() & 0x0000FF00) >> 8);
			header.pin[2] = (byte)((_gen.getDeviceId() & 0x00FF0000) >> 16);
			header.pin[3] = (byte)((_gen.getDeviceId() & 0xFF000000) >> 24);
			header.pin[4] = (byte)(_gen.getDeviceType() & 0x00FF);
			header.pin[5] = (byte)((_gen.getDeviceType() & 0xFF00) >> 8);			
		}else{
			header.pin[0] = 0;
			header.pin[1] = 0;
			header.pin[2] = 0;
			header.pin[3] = 0;
			header.pin[4] = 0;
			header.pin[5] = 0;

		}
		
	}
}

