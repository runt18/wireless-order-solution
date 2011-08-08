package com.wireless.protocol;

public class RespNAK extends RespPackage{
	/**
	 * Construct the NAK response with the "unknown" error code.
	 * @param reqHeader the request header help to build the NAK
	 */
	public RespNAK(ProtocolHeader reqHeader){
		super(reqHeader);
		header.type = Type.NAK;
		header.reserved[0] = ErrorCode.UNKNOWN;
	}
	
	/**
	 * Construct the NAK response with the specific error code
	 * @param reqHeader the request header help to build the NAK
	 * @param errCode one of the error values specified in class ErrorCode 
	 */
	public RespNAK(ProtocolHeader reqHeader, byte errCode){
		super(reqHeader);
		header.type = Type.NAK;
		header.reserved[0] = errCode;
	}
}
