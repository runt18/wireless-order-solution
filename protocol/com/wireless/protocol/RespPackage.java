package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class RespPackage extends ProtocolPackage{
	
	public RespPackage(ProtocolHeader reqHeader){
		
		this.header.mode = reqHeader.mode;
		this.header.type = Type.ACK;
		this.header.seq = reqHeader.seq;
		System.arraycopy(reqHeader.pin, 0, header.pin, 0, header.pin.length);
		
		this.body = new byte[0];
	}
	
	public RespPackage(ProtocolHeader header, Parcel parcel){
		this(header);
		if(parcel != null){
			this.body = parcel.marshall();
		}else{
			this.body = new byte[0];
		}
	}
	
	public RespPackage(ProtocolHeader header, Parcelable parcelable, int flag){
		this(header);
		if(parcelable != null){
			Parcel p = new Parcel();
			parcelable.writeToParcel(p, flag);
			this.body = p.marshall();
			
		}else{
			this.body = new byte[0];
		}
	}
}
