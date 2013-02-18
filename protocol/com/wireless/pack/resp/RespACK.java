package com.wireless.pack.resp;

import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.Type;

public class RespACK extends RespPackage{
	/**
	 * Construct the ACK response.
	 * @param reqHeader the request header help to build the NAK
	 */
	public RespACK(ProtocolHeader reqHeader){
		super(reqHeader);
		header.type = Type.ACK;
	}
	
	/**
	 * Construct the ACK response with a reserved value.
	 * @param reqHeader the request header help to build the NAK
	 * @param reserved the extra reserved value
	 */
	public RespACK(ProtocolHeader reqHeader, byte reserved){
		this(reqHeader);
		header.reserved = reserved;
	}
}
