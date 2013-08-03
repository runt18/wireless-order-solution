package com.wireless.pack.req;

import com.wireless.pack.ProtocolPackage;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.staffMgr.Staff;

public class RequestPackage extends ProtocolPackage{
	private final static Object syncObj = new Object();
	private static byte seq = Byte.MIN_VALUE;
	
	protected RequestPackage(Staff staff){
		this.fillHeader(staff);
		this.body = new byte[0];
	}
	
	protected RequestPackage(Staff staff, Parcel parcel){
		this.fillHeader(staff);
		this.fillBody(parcel);
	}
	
	protected RequestPackage(Staff staff, Parcelable parcelable, int flag){
		this.fillHeader(staff);
		this.fillBody(parcelable, flag);
	}
	
	protected RequestPackage(Staff staff, Parcelable[] parcelableArray, int flag){
		this.fillHeader(staff);
		this.fillBody(parcelableArray, flag);
	}
	
	private void fillHeader(Staff staff){
		synchronized(syncObj){
			if(++seq == Byte.MAX_VALUE){
				seq = Byte.MIN_VALUE;
			}
		}
		header.seq = seq;
		
		if(staff != null){
			header.staffId[0] = (byte)(staff.getId() & 0x000000FF);
			header.staffId[1] = (byte)((staff.getId() & 0x0000FF00) >> 8);
			header.staffId[2] = (byte)((staff.getId() & 0x00FF0000) >> 16);
			header.staffId[3] = (byte)((staff.getId() & 0xFF000000) >> 24);
			header.restaurantId[0] = (byte)(staff.getRestaurantId() & 0x00FF);
			header.restaurantId[1] = (byte)((staff.getRestaurantId() & 0xFF00) >> 8);			
		}else{
			header.staffId[0] = 0;
			header.staffId[1] = 0;
			header.staffId[2] = 0;
			header.staffId[3] = 0;
			header.restaurantId[0] = 0;
			header.restaurantId[1] = 0;
		}
	}
}

