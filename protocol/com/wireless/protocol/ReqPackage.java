package com.wireless.protocol;
 
public class ReqPackage extends ProtocolPackage{
	private static Object syncObj = new Object();
	private static byte seq = Byte.MIN_VALUE;
	public ReqPackage(){
		synchronized(syncObj){
			if(++seq == Byte.MAX_VALUE){
				seq = Byte.MIN_VALUE;
			}
		}
		header.seq = seq;
	}
}

