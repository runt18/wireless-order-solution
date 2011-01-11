package com.wireless.protocol;

public class ReqPrintPackage extends ProtocolPackage{
	private static Object syncObj = new Object();
	private static byte seq = Byte.MIN_VALUE;
	public ReqPrintPackage(){
		synchronized(syncObj){
			if(++seq == Byte.MAX_VALUE){
				seq = Byte.MIN_VALUE;
			}
		}
		header.seq = seq;
		header.pin[0] = 0;
		header.pin[1] = 0;
		header.pin[2] = 0;
		header.pin[3] = 0;
		header.pin[4] = 0;
		header.pin[5] = 0;
	}
}
