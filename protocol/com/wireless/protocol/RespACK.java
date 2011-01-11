package com.wireless.protocol;

public class RespACK extends RespPackage{
	public RespACK(ProtocolHeader reqHeader){
		super(reqHeader);
		header.type = Type.ACK;
	}
}
