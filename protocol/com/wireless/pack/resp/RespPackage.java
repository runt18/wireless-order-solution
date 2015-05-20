package com.wireless.pack.resp;

import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;

public class RespPackage extends ProtocolPackage{
	
	public RespPackage(ProtocolHeader reqHeader){
		this.fillHeader(reqHeader);
		this.body = new byte[0];
	}
	
	private void fillHeader(ProtocolHeader header){
		this.header.mode = header.mode;
		this.header.type = Type.ACK;
		this.header.seq = header.seq;
		System.arraycopy(header.staffId, 0, header.staffId, 0, header.staffId.length);
	}
}
