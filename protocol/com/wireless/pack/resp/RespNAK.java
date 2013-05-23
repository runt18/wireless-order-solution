package com.wireless.pack.resp;

import com.wireless.exception.ErrorCode;
import com.wireless.exception.ErrorEnum;
import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.Type;

public class RespNAK extends RespPackage{
	/**
	 * Construct the NAK response with the "unknown" error code.
	 * @param reqHeader the request header help to build the NAK
	 */
	public RespNAK(ProtocolHeader reqHeader){
		super(reqHeader);
		header.type = Type.NAK;
		fillBody(ErrorEnum.UNKNOWN, ErrorCode.ER_PARCELABLE_SIMPLE);
	}
	
	/**
	 * Construct the NAK response with the specific error code
	 * @param reqHeader the request header help to build the NAK
	 * @param errCode one of the error values specified in class ErrorCode 
	 */
	public RespNAK(ProtocolHeader reqHeader, ErrorCode errCode){
		super(reqHeader);
		header.type = Type.NAK;
		fillBody(errCode, ErrorCode.ER_PARCELABLE_SIMPLE);
	}
}
