package com.wireless.protocol;public class Food{	public long foodID = 0;							//the food's id	public int aliasID = 0;							//the food's alias id	public int restaurantID = 0;					//the restaurant id that the food belongs to	public Kitchen kitchen = new Kitchen();			//the kitchen which the food belongs to	public String name = "";						//the food's name		public String pinyin = "";						//the simple Chinese pinyin to this food		int price = 0;		//the unit price of the food		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		price = Util.float2Int(_price);	}		public Float getPrice(){		return Util.int2Float(price);	}		/**	 * The status of the food.	 * It can be the combination of values below.	 */	public final static byte SPECIAL = 0x01;	/* 特价 */	public final static byte RECOMMEND = 0x02;	/* 推荐 */ 	public final static byte SELL_OUT = 0x04;	/* 售完 */	public final static byte GIFT = 0x08;		/* 赠送 */	public final static byte CUR_PRICE = 0x10;	/* 时价 */	public short status = 0;		public boolean isSpecial(){		return ((status & SPECIAL) != 0);	}		public boolean isRecommend(){		return ((status & RECOMMEND) != 0);	}		public boolean isSellOut(){		return ((status & SELL_OUT) != 0);	}		public boolean isGift(){		return ((status & GIFT) != 0);		}			public boolean isCurPrice(){		return ((status & CUR_PRICE) != 0);	}		public boolean equals(Object obj){		if(obj == null || !(obj instanceof OrderFood)){			return false;		}else{			return restaurantID == ((Food)obj).restaurantID && aliasID == ((Food)obj).aliasID;		}	}		public int hashCode(){		return restaurantID + aliasID;	}		public Food(){	}		public Food(int alias_id, String name){		this.aliasID = alias_id;		this.name = name.trim();	}		public Food(int alias_id, String name, Float price){		this(alias_id, name);		//split the price into 3-byte value		//get the float point from the price		setPrice(price);	}	public Food(int restaurantID, long foodID, int alias_id, String name, Float price, short _kitchenAlias, short status, String pinyin){		this(alias_id, name, price);		this.restaurantID = restaurantID;		this.foodID = foodID;		this.kitchen.aliasID = _kitchenAlias;		this.status = status;		this.pinyin = pinyin;	}	}