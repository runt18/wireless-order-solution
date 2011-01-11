package com.wireless.protocol;

public class RespPackage extends ProtocolPackage{
	public RespPackage(ProtocolHeader reqHeader){
		header.mode = reqHeader.mode;
		header.seq = reqHeader.seq;
		for(int i = 0; i < header.pin.length; i++){
			header.pin[i] = reqHeader.pin[i];
		}
	}
}
