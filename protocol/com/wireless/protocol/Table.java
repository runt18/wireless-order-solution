package com.wireless.protocol;

public class Table {
	
	public static final byte TABLE_IDLE = 0;
	public static final byte TABLE_BUSY = 1;

	//the restaurant id that this table is attached to
	public int restaurantID = 0;
	//the real id to this table
	public int tableID = 0;
	//the alias id to this table
	public int aliasID = 0;
	//the alias name to this table
	public String name = "";
	//the number of the custom to this table
	public short customNum = 0;
	//the status to this table
	public short status = TABLE_IDLE;
	//the category to this table
	public short category = Order.CATE_NORMAL;
	//the region to this table
	public short regionID = Region.REGION_1;
	
	//the service rate to this table
	int serviceRate = 0;
	
	public void setServiceRate(Float rate){
		serviceRate = Util.float2Int(rate);
	}
	
	public Float getServiceRate(){
		return Util.int2Float(serviceRate);
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
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Table)){
			return false;
		}else{
			return restaurantID == ((Table)obj).restaurantID && aliasID == ((Table)obj).aliasID;
		}
	}
	
	public int hashCode(){
		return new Integer(restaurantID).hashCode() ^ new Integer(aliasID).hashCode();
	}
	
	public Table(){
		
	}
	
	public Table(Table src){
		this.restaurantID = src.restaurantID;
		this.tableID = src.tableID;
		this.aliasID = src.aliasID;
		this.name = src.name;
		this.customNum = src.customNum;
		this.status = src.status;
		this.category = src.category;
		this.regionID = src.regionID;
		this.serviceRate = src.serviceRate;
		this.minimumCost = src.minimumCost;
	}
	
}
