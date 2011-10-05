package com.wireless.protocol;

public final class Kitchen {
	
	public final static short KITCHEN_NULL = 255;
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
		
	/**
	 * The discount to this kitchen ranges from 0.00 through 1.00
	 * Since the 8100 doesn't support float, we instead to use 0 through 100.
	 * So the real price should be divided 100 at last. 
	 */
	public byte discount = 100;	
	public byte discount_2 = 100;
	public byte discount_3 = 100;
	public byte member_discount_1 = 100;
	public byte member_discount_2 = 100;
	public byte member_discount_3 = 100;
	
	public String name;
	public short alias_id = KITCHEN_NULL;
	public short skitchen_id = SKitchen.S_KITCHEN_1;
	
	
	public Kitchen(String kName, short id, short sKitchenID){
		name = kName;
		alias_id = id;
		skitchen_id = sKitchenID;
	}
	
	public Kitchen(String kName, short id, short sKitchenID, byte dist1, byte dist2, byte dist3, byte memDist1, byte memDist2, byte memDist3){
		this(kName, id, sKitchenID);
		discount = dist1;
		discount_2 = dist2;
		discount_3 = dist3;
		member_discount_1 = memDist1;
		member_discount_2 = memDist2;
		member_discount_3 = memDist3;
	}
}
