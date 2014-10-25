package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.NumericUtil;

public class TableParcel extends Table implements Parcelable {

	private boolean isNull = false;
	
	public static final String KEY_VALUE = TableParcel.class.getName();
	
	public TableParcel(Table table){
		if(table != null){
			this.setRestaurantId(table.getRestaurantId());
			this.setTableId(table.getTableId());
			this.setTableAlias(table.getAliasId());
			this.setCategory(table.getCategory());
			this.setStatus(table.getStatus());
			this.setCustomNum(table.getCustomNum());
			this.setTableName(table.getName());
			this.getRegion().setRegionId(table.getRegion().getId());
			this.setMinimumCost(table.getMinimumCost());
		}else{
			isNull = true;
		}
	}
	
	private TableParcel(Parcel in){
		this.setRestaurantId(in.readInt());
		this.setTableId(in.readInt());
		this.setTableAlias(in.readInt());
		this.setCategory(Order.Category.valueOf(in.readInt()));
		this.setStatus(Table.Status.valueOf(in.readInt()));
		this.setCustomNum((short)in.readInt());
		this.setTableName(in.readString());
		this.getRegion().setRegionId((short)in.readInt());
		this.setMinimumCost(NumericUtil.int2Float(in.readInt()));
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
		if(isNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(this.getRestaurantId());
			parcel.writeInt(this.getTableId());
			parcel.writeInt(this.getAliasId());
			parcel.writeInt(this.getCategory().getVal());
			parcel.writeInt(this.getStatus().getVal());
			parcel.writeInt(this.getCustomNum());
			parcel.writeString(this.getName());
			parcel.writeInt(this.getRegion().getId());
			parcel.writeInt(NumericUtil.float2Int(this.getMinimumCost()));
		}
	}

}
