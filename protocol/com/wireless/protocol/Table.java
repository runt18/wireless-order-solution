package com.wireless.protocol;

public class Table {
	
	public static final byte TABLE_IDLE = 0;
	public static final byte TABLE_BUSY = 1;

	public static final byte TABLE_NORMAL = Order.CATE_NORMAL;
	public static final byte TABLE_MERGER = Order.CATE_MERGER_TABLE;
	public static final byte TABLE_MERGER_CHILD = Order.CATE_MERGER_CHILD;
	
	//the restaurant id that this table is attached to
	public int restaurantID = 0;
	//the real id to this table
	public int tableID = 0;
	//the alias id to this table
	public int aliasID = 0;
	//the alias name to this table
	public String name = "";
	//the pinyin to table name
	String mPinyin;
	//the shortcut to pinyin of table name
	String mPinyinShortcut;
	//the number of the custom to this table
	short mCustomNum = 0;
	//the status to this table
	short mStatus = TABLE_IDLE;
	//the category to this table
	short mCategory = Order.CATE_NORMAL;
	//the region to this table
	public short regionID = Region.REGION_1;
	
	//the service rate to this table
	int mServiceRate = 0;
	
	public void setServiceRate(Float rate){
		mServiceRate = Util.float2Int(rate);
	}
	
	public Float getServiceRate(){
		return Util.int2Float(mServiceRate);
	}
	
	public void setCustomNum(short customNum){
		this.mCustomNum = customNum;
	}
	
	public short getCustomNum(){
		return this.mCustomNum;
	}
	
	/**
	 * The value of minimum cost to this table, ranges from 99999.99 through 0.00
	 * Since the 8100 doesn't support float, we instead to use 0 through 9999999.
	 * So the real price should be divided 100 at last. 
	 */
	int minimumCost = 0;
	
	public void setMinimumCost(Float cost){
		minimumCost = Util.float2Int(cost);
	}
	
	public Float getMinimumCost(){
		return Util.int2Float(minimumCost);
	}
	
	public void setTableId(int tableId){
		this.tableID = tableId;
	}
	
	public int getTableId(){
		return this.tableID;
	}
	
	public void setAliasId(int aliasId){
		this.aliasID = aliasId;
	}
	
	public int getAliasId(){
		return this.aliasID;
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
	
	public boolean isNormal(){
		return mCategory == TABLE_NORMAL;
	}
	
	public boolean isMerged(){
		return mCategory == TABLE_MERGER || mCategory == TABLE_MERGER_CHILD;
	}	
	
	public int hashCode(){
		return restaurantID + aliasID;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Table)){
			return false;
		}else{
			return restaurantID == ((Table)obj).restaurantID && aliasID == ((Table)obj).aliasID;
		}
	}
	
	public String toString(){
		return "table(alias_id = " + aliasID + ", restaurant_id = " + restaurantID + ")";
	}
	
	public Table(){
		
	}
	
	public Table(int tableId, int aliasId, int restaurantId){
		this.tableID = tableId;
		this.aliasID = aliasId;
		this.restaurantID = restaurantId;
	}
	
	public Table(Table src){
		this.restaurantID = src.restaurantID;
		this.tableID = src.tableID;
		this.aliasID = src.aliasID;
		this.name = src.name;
		this.mCustomNum = src.mCustomNum;
		this.mStatus = src.mStatus;
		this.mCategory = src.mCategory;
		this.regionID = src.regionID;
		this.mServiceRate = src.mServiceRate;
		this.minimumCost = src.minimumCost;
	}
		
}
