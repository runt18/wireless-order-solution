package com.wireless.protocol;

import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class StaffTerminal implements Parcelable{
	
	public final static byte ST_PARCELABLE_COMPLEX = 0;
	public final static byte ST_PARCELABLE_SIMPLE = 1;
	
	//the id to this staff
	public long id;
	//the alias id to this staff
	public int aliasID;
	//the name to this staff
	public String name;
	//the password to this staff
	public String pwd;
	//the terminal pin this staff attached to
	public long pin;
	//the terminal id this staff attached to
	public int terminalId;
	//the gift quota represented as an integer
	int giftQuota = -1;
	
	public int type;
	
	public void setGiftQuota(Float quota){
		giftQuota = NumericUtil.float2Int(quota);
	}
	
	/**
	 * Get the gift quota.
	 * @return null if no quota limit, otherwise an Float object
	 */
	public Float getGiftQuota(){
		return giftQuota < 0 ? new Float(giftQuota) : NumericUtil.int2Float(giftQuota);
	}
	
	//the gift amount represented as an integer
	int giftAmount = 0;
	
	public void setGiftAmount(Float amount){
		giftAmount = NumericUtil.float2Int(amount);
	}
	
	public Float getGiftAmount(){
		return NumericUtil.int2Float(giftAmount);
	}
	
	public StaffTerminal(){
		
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == ST_PARCELABLE_SIMPLE){
			
		}else if(flag == ST_PARCELABLE_COMPLEX){
			dest.writeString(this.name);
			dest.writeLong(this.pin);
			dest.writeString(this.pwd);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == ST_PARCELABLE_SIMPLE){
			
		}else if(flag == ST_PARCELABLE_COMPLEX){
			this.name = source.readString();
			this.pin = source.readLong();
			this.pwd = source.readString();
		}
	}
	
	public final static Parcelable.Creator<StaffTerminal> ST_CREATOR = new Parcelable.Creator<StaffTerminal>() {
		
		public StaffTerminal[] newInstance(int size) {
			return new StaffTerminal[size];
		}
		
		public StaffTerminal newInstance() {
			return new StaffTerminal();
		}
	};

}
