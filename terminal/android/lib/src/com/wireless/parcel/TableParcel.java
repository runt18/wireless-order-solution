package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Table;
import com.wireless.protocol.Util;

public class TableParcel extends Table implements Parcelable {

	public static final String KEY_VALUE = "com.wireless.common.TableParcel";
	
	public TableParcel(Table table){
		this.restaurantID = table.restaurantID;
		this.tableID = table.tableID;
		this.aliasID = table.aliasID;
		this.category = table.category;
		this.status = table.status;
		this.customNum = table.customNum;
		this.name = new String(table.name);
		this.regionID = table.regionID;
		this.setMinimumCost(Float.valueOf(table.getMinimumCost()));
		this.setServiceRate(Float.valueOf(table.getServiceRate()));
	}
	
	TableParcel(Parcel in){
		this.restaurantID = in.readInt();
		this.tableID = in.readInt();
		this.aliasID = in.readInt();
		this.category = (short)in.readInt();
		this.status = (short)in.readInt();
		this.customNum = (short)in.readInt();
		this.name = in.readString();
		this.regionID = (short)in.readInt();
		this.setMinimumCost(Util.int2Float(in.readInt()));
		this.setServiceRate(Util.int2Float(in.readInt()));
	}
	
	public static final Parcelable.Creator<TableParcel> CREATOR = new Parcelable.Creator<TableParcel>() {
		public TableParcel createFromParcel(Parcel in) {
			return new TableParcel(in);
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
		parcel.writeInt(this.restaurantID);
		parcel.writeInt(this.tableID);
		parcel.writeInt(this.aliasID);
		parcel.writeInt(this.category);
		parcel.writeInt(this.status);
		parcel.writeInt(this.customNum);
		parcel.writeString(this.name);
		parcel.writeInt(this.regionID);
		parcel.writeInt(Util.float2Int(this.getMinimumCost()));
		parcel.writeInt(Util.float2Int(this.getServiceRate()));
	}

}
