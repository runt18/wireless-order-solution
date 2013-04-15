package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.PTable;
import com.wireless.util.NumericUtil;

public class TableParcel extends PTable implements Parcelable {

	private boolean nIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.TableParcel";
	
	public TableParcel(PTable table){
		if(table != null){
			this.setRestaurantId(table.getRestaurantId());
			this.setTableId(table.getTableId());
			this.setAliasId(table.getAliasId());
			this.setCategory(table.getCategory());
			this.setStatus(table.getStatus());
			this.setCustomNum(table.getCustomNum());
			this.setName(table.getName());
			this.setRegionID(table.getRegionId());
			this.setMinimumCost(table.getMinimumCost());
			this.setServiceRate(table.getServiceRate());			
		}else{
			nIsNull = true;
		}
	}
	
	private TableParcel(Parcel in){
		this.setRestaurantId(in.readInt());
		this.setTableId(in.readInt());
		this.setAliasId(in.readInt());
		this.setCategory((short)in.readInt());
		this.setStatus((short)in.readInt());
		this.setCustomNum((short)in.readInt());
		this.setName(in.readString());
		this.setRegionID((short)in.readInt());
		this.setMinimumCost(NumericUtil.int2Float(in.readInt()));
		this.setServiceRate(NumericUtil.int2Float(in.readInt()));
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
			parcel.writeInt(this.getRestaurantId());
			parcel.writeInt(this.getTableId());
			parcel.writeInt(this.getAliasId());
			parcel.writeInt(this.getCategory());
			parcel.writeInt(this.getStatus());
			parcel.writeInt(this.getCustomNum());
			parcel.writeString(this.getName());
			parcel.writeInt(this.getRegionId());
			parcel.writeInt(NumericUtil.float2Int(this.getMinimumCost()));
			parcel.writeInt(NumericUtil.float2Int(this.getServiceRate()));
		}
	}

}
