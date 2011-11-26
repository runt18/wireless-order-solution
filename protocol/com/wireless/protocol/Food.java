package com.wireless.protocol;public class Food{	public long food_id;								//the food's id	public int alias_id = 0;						//the food's alias id	public short kitchen = Kitchen.KITCHEN_NULL;	//the kitchen which the food belong to	public String name = "";						//the food's name		public String pinyin = null;					//the simple Chinese pinyin to this food		int price = 0;		//the unit price of the food		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		price = Util.float2Int(_price);	}		public Float getPrice(){		return Util.int2Float(price);	}		/**	 * The status of the food.	 * It can be the combination of values below.	 */	public final static byte SPECIAL = 0x01;	/* 特价 */	public final static byte RECOMMEND = 0x02;	/* 推荐 */ 	public final static byte SELL_OUT = 0x04;	/* 售完 */	public final static byte GIFT = 0x08;		/* 赠送 */	public short status = 0;		public boolean isSpecial(){		return ((status & SPECIAL) != 0);	}		public boolean isRecommend(){		return ((status & RECOMMEND) != 0);	}		public boolean isSellOut(){		return ((status & SELL_OUT) != 0);	}		public boolean isGift(){		return ((status & GIFT) != 0);		}			public Food(){	}		public Food(int alias_id, String _name){		this();		this.alias_id = alias_id;		name = _name.trim();	}		public Food(int alias_id, String _name, Float _price){		this(alias_id, _name);		//split the price into 3-byte value		//get the float point from the price		setPrice(_price);	}	public Food(int alias_id, String _name, Float _price, short _kitchen, short _status, String _pinyin){		this(alias_id, _name, _price);		kitchen = _kitchen;		status = _status;		pinyin = _pinyin;	}	}