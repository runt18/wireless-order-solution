package com.wireless.protocol;

public final class Kitchen {
	
	public final static short TYPE_NORMAL = 0;				/* 一般 */
	public final static short TYPE_RESERVED = 1;			/* 保留 */
	
	public final static short KITCHEN_NULL = 255;
	public final static short KITCHEN_FULL = 254;
	public final static short KITCHEN_TEMP = 253;
	public final static short KITCHEN_1 = 0;
	public final static short KITCHEN_2 = 1;
	public final static short KITCHEN_3 = 2;
	public final static short KITCHEN_4 = 3;
	public final static short KITCHEN_5 = 4;
	public final static short KITCHEN_6 = 5;
	public final static short KITCHEN_7 = 6;
	public final static short KITCHEN_8 = 7;
	public final static short KITCHEN_9 = 8;
	public final static short KITCHEN_10 = 9;
	public final static short KITCHEN_11 = 10;
	public final static short KITCHEN_12 = 11;
	public final static short KITCHEN_13 = 12;
	public final static short KITCHEN_14 = 13;
	public final static short KITCHEN_15 = 14;
	public final static short KITCHEN_16 = 15;
	public final static short KITCHEN_17 = 16;
	public final static short KITCHEN_18 = 17;
	public final static short KITCHEN_19 = 18;
	public final static short KITCHEN_20 = 19;	

	int discount_1 = 100;	
	int discount_2 = 100;
	int discount_3 = 100;
	int memberDist_1 = 100;
	int memberDist_2 = 100;
	int memberDist_3 = 100;
	
	public Float getDist1(){
		return Util.int2Float(discount_1);
	}
	
	public void setDist1(Float distVal){
		discount_1 = Util.float2Int(distVal);
	}
	
	public Float getDist2(){
		return Util.int2Float(discount_2);
	}
	
	public void setDist2(Float distVal){
		discount_2 = Util.float2Int(distVal);
	}
	
	public Float getDist3(){
		return Util.int2Float(discount_3);
	}
	
	public void setDist3(Float distVal){
		discount_3 = Util.float2Int(distVal);
	}
	
	public Float getMemDist1(){
		return Util.int2Float(memberDist_1);
	}
	
	public void setMemDist1(Float distVal){
		memberDist_1 = Util.float2Int(distVal);
	}
	
	public Float getMemDist2(){
		return Util.int2Float(memberDist_2);
	}
	
	public void setMemDist2(Float distVal){
		memberDist_2 = Util.float2Int(distVal);
	}
	
	public Float getMemDist3(){
		return Util.int2Float(memberDist_3);
	}
	
	public void setMemDist3(Float distVal){
		memberDist_3 = Util.float2Int(distVal);
	}
	//the type to this kitchen
	public short type = TYPE_NORMAL;	
	//the name to this kitchen
	public String name;
	//the restaurant id to this kitchen
	public int restaurantID;
	//the id to this kitchen
	public long kitchenID;
	//the alias id to this kitchen
	public short aliasID;
	//the department id to this kitchen
	public Department dept;
	
	public Kitchen(){
		this.dept = new Department();
		this.restaurantID = 0;
		this.kitchenID = 0;
		this.aliasID = KITCHEN_NULL;
	}
	
	public Kitchen(int restaurantID, String kitchenName, long kitchenID, short kitchenAlias, short type, Department dept){
		this.restaurantID = restaurantID;
		this.name = kitchenName;
		this.kitchenID = kitchenID;
		this.aliasID = kitchenAlias;
		this.type = type;
		this.dept = dept;
	}
	
	public boolean isNormal(){
		return type == TYPE_NORMAL;
	}
	
	public boolean isReserved(){
		return type == TYPE_RESERVED;
	}
	
	public Kitchen(int restaurantID, String kitchenName, long kitchenID, short kitchenAlias, short type,
				   Department dept, byte dist1, byte dist2, byte dist3, byte memDist1, byte memDist2, byte memDist3){
		this(restaurantID, kitchenName, kitchenID, kitchenAlias, type, dept);
		discount_1 = dist1;
		discount_2 = dist2;
		discount_3 = dist3;
		memberDist_1 = memDist1;
		memberDist_2 = memDist2;
		memberDist_3 = memDist3;
		this.type = type;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Kitchen)){
			return false;
		}else{
			return restaurantID == ((Kitchen)obj).restaurantID && aliasID == ((Kitchen)obj).aliasID;
		}
	}
	
	public int hashCode(){
		return new Integer(restaurantID).hashCode() ^
			   new Integer(aliasID).hashCode();
	}
}
