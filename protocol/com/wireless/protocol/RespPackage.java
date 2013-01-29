package com.wireless.protocol;

public class RespPackage extends ProtocolPackage{
	public RespPackage(ProtocolHeader reqHeader){
		header.mode = reqHeader.mode;
		header.seq = reqHeader.seq;
		System.arraycopy(reqHeader.pin, 0, header.pin, 0, header.pin.length);
	}
}
