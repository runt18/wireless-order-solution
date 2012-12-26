package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Table;
import com.wireless.protocol.Util;

public class TableParcel extends Table implements Parcelable {

	private boolean nIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.TableParcel";
	
	public TableParcel(Table table){
		if(table != null){
			this.restaurantID = table.restaurantID;
			this.tableID = table.tableID;
			this.aliasID = table.aliasID;
			this.setCategory(table.getCategory());
			this.setStatus(table.getStatus());
			this.customNum = table.customNum;
			this.name = table.name;
			this.regionID = table.regionID;
			this.setMinimumCost(table.getMinimumCost());
			this.setServiceRate(table.getServiceRate());			
		}else{
			nIsNull = true;
		}
	}
	
	private TableParcel(Parcel in){
		this.restaurantID = in.readInt();
		this.tableID = in.readInt();
		this.aliasID = in.readInt();
		this.setCategory((short)in.readInt());
		this.setStatus((short)in.readInt());
		this.customNum = (short)in.readInt();
		this.name = in.readString();
		this.regionID = (short)in.readInt();
		this.setMinimumCost(Util.int2Float(in.readInt()));
		this.setServiceRate(Util.int2Float(in.readInt()));
	}
	
	public static final Parcelable.Creator<TableParcel> CREATOR = new Parcelable.Creator<TableParcel>() {
		public TableParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new TableParcel(in);
			}
		}

		public TableParcel[] newArray(int size) {
			return new TableParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		if(nIsNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(this.restaurantID);
			parcel.writeInt(this.tableID);
			parcel.writeInt(this.aliasID);
			parcel.writeInt(this.getCategory());
			parcel.writeInt(this.getStatus());
			parcel.writeInt(this.customNum);
			parcel.writeString(this.name);
			parcel.writeInt(this.regionID);
			parcel.writeInt(Util.float2Int(this.getMinimumCost()));
			parcel.writeInt(Util.float2Int(this.getServiceRate()));
		}
	}

}
