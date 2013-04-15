package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.util.NumericUtil;

public class PTable implements Parcelable{
	
	public final static byte TABLE_PARCELABLE_COMPLEX = 0;
	public final static byte TABLE_PARCELABLE_SIMPLE = 1;
	
	public static final byte TABLE_IDLE = 0;
	public static final byte TABLE_BUSY = 1;

	public static final byte TABLE_NORMAL = Order.CATE_NORMAL;
	public static final byte TABLE_MERGER = Order.CATE_MERGER_TABLE;
	public static final byte TABLE_MERGER_CHILD = Order.CATE_MERGER_CHILD;
	
	//the restaurant id that this table is attached to
	int restaurantID = 0;
	//the real id to this table
	int mTableId = 0;
	//the alias id to this table
	int mAliasId = 0;
	//the alias name to this table
	String mName;
	//the pinyin to table name
	String mPinyin;
	//the shortcut to pinyin of table name
	String mPinyinShortcut;
	//the number of the custom to this table
	int mCustomNum = 0;
	//the status to this table
	short mStatus = TABLE_IDLE;
	//the category to this table
	short mCategory = Order.CATE_NORMAL;
	//FIXME the region to this table
	public short regionID = PRegion.REGION_1;
	
	//the service rate to this table
	int mServiceRate = 0;
	
	public void setServiceRate(Float rate){
		mServiceRate = NumericUtil.float2Int(rate);
	}
	
	public Float getServiceRate(){
		return NumericUtil.int2Float(mServiceRate);
	}
	
	public void setCustomNum(int customNum){
		this.mCustomNum = customNum;
	}
	
	public int getCustomNum(){
		return this.mCustomNum;
	}
	
	public int getRestaurantId(){
		return restaurantID;
	}
	
	public void setRestaurantId(int restaurantId){
		this.restaurantID = restaurantId;
	}
	
	/**
	 * The value of minimum cost to this table, ranges from 99999.99 through 0.00
	 * Since the 8100 doesn't support float, we instead to use 0 through 9999999.
	 * So the real price should be divided 100 at last. 
	 */
	int mMinimumCost = 0;
	
	public void setMinimumCost(Float cost){
		mMinimumCost = NumericUtil.float2Int(cost);
	}
	
	public Float getMinimumCost(){
		return NumericUtil.int2Float(mMinimumCost);
	}
	
	public void setTableId(int tableId){
		this.mTableId = tableId;
	}
	
	public int getTableId(){
		return this.mTableId;
	}
	
	public void setAliasId(int aliasId){
		this.mAliasId = aliasId;
	}
	
	public int getAliasId(){
		return this.mAliasId;
	}
	
	public void setName(String name){
		this.mName = name;
	}
	
	public String getName(){
		if(this.mName == null){
			this.mName = "";
		}
		return this.mName;
	}
	
	public String getPinyin(){
		return mPinyin;
	}
	
	public void setStatus(short status){
		this.mStatus = status;
	}
	
	public short getStatus(){
		return mStatus;
	}
	
	public boolean isIdle(){
		return mStatus == TABLE_IDLE;
	}
	
	public boolean isBusy(){
		return mStatus == TABLE_BUSY;
	}
	
	public void setPinyin(String pinyin){
		this.mPinyin = pinyin;
	}
	
	public String getPinyinShortcut(){
		return mPinyinShortcut;
	}
	
	public void setPinyinShortcut(String pinyinShortcut){
		this.mPinyinShortcut = pinyinShortcut;
	}
	
	public void setCategory(short category){
		this.mCategory = category;
	}
	
	public short getCategory(){
		return mCategory;
	}
	
	public short getRegionId() {
		return regionID;
	}

	public void setRegionID(short regionID) {
		this.regionID = regionID;
	}

	public boolean isNormal(){
		return mCategory == TABLE_NORMAL;
	}
	
	public boolean isMerged(){
		return mCategory == TABLE_MERGER || mCategory == TABLE_MERGER_CHILD;
	}	
	
	public int hashCode(){
		return restaurantID + mAliasId;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PTable)){
			return false;
		}else{
			return restaurantID == ((PTable)obj).restaurantID && mAliasId == ((PTable)obj).mAliasId;
		}
	}
	
	public String toString(){
		return "table(alias_id = " + mAliasId + ", restaurant_id = " + restaurantID + ")";
	}
	
	public PTable(){
		
	}
	
	public PTable(int tableId, int aliasId, int restaurantId){
		this.mTableId = tableId;
		this.mAliasId = aliasId;
		this.restaurantID = restaurantId;
	}
	
	public PTable(PTable src){
		this.restaurantID = src.restaurantID;
		this.mTableId = src.mTableId;
		this.mAliasId = src.mAliasId;
		this.mName = src.mName;
		this.mCustomNum = src.mCustomNum;
		this.mStatus = src.mStatus;
		this.mCategory = src.mCategory;
		this.regionID = src.regionID;
		this.mServiceRate = src.mServiceRate;
		this.mMinimumCost = src.mMinimumCost;
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TABLE_PARCELABLE_SIMPLE){
			dest.writeShort(this.mAliasId);
		}else if(flag == TABLE_PARCELABLE_COMPLEX){
			//TODO
			dest.writeShort(this.mAliasId);
			dest.writeString(this.mName);
			dest.writeByte(this.regionID);
			dest.writeShort(this.mServiceRate);
			dest.writeInt(this.mMinimumCost);
			dest.writeByte(this.mStatus);
			dest.writeByte(this.mCategory);
			dest.writeShort(this.mCustomNum);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TABLE_PARCELABLE_SIMPLE){
			this.mAliasId = source.readShort();
		}else if(flag == TABLE_PARCELABLE_COMPLEX){
			//TODO
			this.mAliasId = source.readShort();
			this.mName = source.readString();
			this.regionID = source.readByte();
			this.mServiceRate = source.readShort();
			this.mMinimumCost = source.readInt();
			this.mStatus = source.readByte();
			this.mCategory = source.readByte();
			this.mCustomNum = source.readShort();
		}
	}
		
	public final static Parcelable.Creator TABLE_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PTable[size];
		}
		
		public Parcelable newInstance() {
			return new PTable();
		}
	};
}
