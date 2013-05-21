package com.wireless.pack.req;

import com.wireless.pack.ProtocolPackage;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class RequestPackage extends ProtocolPackage{
	private final static Object syncObj = new Object();
	private static byte seq = Byte.MIN_VALUE;
	
	public final static PinGen EMPTY_PIN = new PinGen(){

		public long getDeviceId() {
			return 0;
		}

		public short getDeviceType() {
			return 0;
		}
		
	};
	
	public RequestPackage(PinGen gen){
		this.fillHeader(gen);
		this.body = new byte[0];
	}
	
	public RequestPackage(PinGen gen, Parcel parcel){
		this.fillHeader(gen);
		this.fillBody(parcel);
	}
	
	public RequestPackage(PinGen gen, Parcelable parcelable, int flag){
		this.fillHeader(gen);
		this.fillBody(parcelable, flag);
	}
	
	public RequestPackage(PinGen gen, Parcelable[] parcelableArray, int flag){
		this.fillHeader(gen);
		this.fillBody(parcelableArray, flag);
	}
	
	private void fillHeader(PinGen gen){
		synchronized(syncObj){
			if(++seq == Byte.MAX_VALUE){
				seq = Byte.MIN_VALUE;
			}
		}
		header.seq = seq;
		
		if(gen != null){
			header.pin[0] = (byte)(gen.getDeviceId() & 0x000000FF);
			header.pin[1] = (byte)((gen.getDeviceId() & 0x0000FF00) >> 8);
			header.pin[2] = (byte)((gen.getDeviceId() & 0x00FF0000) >> 16);
			header.pin[3] = (byte)((gen.getDeviceId() & 0xFF000000) >> 24);
			header.pin[4] = (byte)(gen.getDeviceType() & 0x00FF);
			header.pin[5] = (byte)((gen.getDeviceType() & 0xFF00) >> 8);			
		}else{
			header.pin[0] = 0;
			header.pin[1] = 0;
			header.pin[2] = 0;
			header.pin[3] = 0;
			header.pin[4] = 0;
			header.pin[5] = 0;
		}
	}
}

