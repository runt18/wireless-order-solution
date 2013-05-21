package com.wireless.pack.resp;

import java.util.List;

import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class RespPackage extends ProtocolPackage{
	
	public RespPackage(ProtocolHeader reqHeader){
		this.fillHeader(reqHeader);
		this.body = new byte[0];
	}
	
	public RespPackage(ProtocolHeader header, Parcel parcel){
		this.fillHeader(header);
		this.fillBody(parcel);
	}
	
	public RespPackage(ProtocolHeader header, Parcelable parcelable, int flag){
		this.fillHeader(header);
		this.fillBody(parcelable, flag);
	}
	
	public RespPackage(ProtocolHeader header, Parcelable[] parcelableArray, int flag){
		this.fillHeader(header);
		this.fillBody(parcelableArray, flag);
	}
	
	public RespPackage(ProtocolHeader header, List<? extends Parcelable> parcelableList, int flag){
		this.fillHeader(header);
		this.fillBody(parcelableList, flag);
	}
	
	private void fillHeader(ProtocolHeader header){
		this.header.mode = header.mode;
		this.header.type = Type.ACK;
		this.header.seq = header.seq;
		System.arraycopy(header.pin, 0, header.pin, 0, header.pin.length);
	}
}
