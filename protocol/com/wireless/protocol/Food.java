package com.wireless.protocol;public final class Food{	//public long real_id = 0;			//the food's real id (combined with the restaurant id)	public int alias_id = 0;			//the food's alias id	public short kitchen = Kitchen.KITCHEN_NULL;	//the kitchen which the food belong to	public String name = "";	//the food's name		public Taste taste = new Taste();	//the taste whose default is no taste	/**	 * Here we use an integer to represent the amount of ordered food.	 */	public int count = 0;		//the number of the food to be ordered	/**	 * Here we use an integer to represent the unit price of the food.	 */	int price = 0;		//the unit price of the food		/**	 * The value of discount ranges from 0.00 through 1.00	 * Since the 8100 doesn't support float, we instead to use 0 through 100.	 * So the real price should be divided 100 at last. 	 */	public byte discount = 100;	//the discount to this food 		/**	 * The simple Chinese pinyin to this food	 */	public String pinyin = null;		/**	 * The status of the food.	 * It can be the combination of values below.	 */	public final static byte SPECIAL = 0x01;	public final static byte RECOMMEND = 0x02;	public final static byte SELL_OUT = 0x04;	public short status = 0;		public boolean isSpecial(){		return ((status & SPECIAL) != 0);	}		public boolean isRecommend(){		return ((status & RECOMMEND) != 0);	}		public boolean isSellOut(){		return ((status & SELL_OUT) != 0);	}		/**	 * Two foods are the same if both food and taste id is matched.	 */	public boolean equals(Object obj){		return alias_id == ((Food)obj).alias_id &&			   taste.alias_id == ((Food)obj).taste.alias_id;	}		public Food(){			}		public Food(int alias_id, String _name){		this.alias_id = alias_id;		name = _name;	}		public Food(int alias_id, String _name, Float _price){		this(alias_id, _name);		//split the price into 3-byte value		//get the float point from the price		setPrice(_price);	}	public Food(int alias_id, String _name, Float _price, short _kitchen, short _status, String _pinyin){		this(alias_id, _name, _price);		kitchen = _kitchen;		status = _status;		pinyin = _pinyin;	}	public void setCount(Float _count){		count = Util.float2Int(_count);	}		public Float getCount(){		return Util.int2Float(count);	}		/**	 * Convert the number of ordered food to string.	 * Note that the value accurate to two decimal parts.<br>	 * For example as below.<br>	 * "1" shown as "1".<br>	 * "1.1" shown as "1.1".<br>	 * "1.23" shown as "1.23".<br>	 * @return the converted string	 *///	public String count2String(){//		return Util.int2String2(count);//	}		/**	 * Convert the count value to float object.	 * @return the Float object indicates the count value	 */	public Float count2Float(){		return Util.int2Float(count);	}		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		price = Util.float2Int(_price);	}		public Float getPrice(){		return Util.int2Float(price);	}		/**	 * The 2nd unit price to food is as below.	 * unit_price = food_price * discount + taste_price	 * @return the unit price represented as Float 	 */	public Float getPrice2(){		return Util.int2Float(price2());	}		/**	 * The 2nd unit price to food is as below.	 * unit_price = food_price * discount + taste_price	 * @return the unit price represented as an integer	 */	int price2(){		return price * discount / 100 + taste.price;	}		/**	 * Calculate the total price to this food as below.	 * <br>price = food_price * discount * count 	 * @return the total price to this food	 */	public Float totalPrice(){		return Util.int2Float((price * discount * count) / 10000);	}		/**	 * Calculate the total price to this food as below.	 * <br>price = (food_price * discount + taste_price) * count 	 * @return the total price to this food	 */	public Float totalPrice2(){		return Util.int2Float(price2() * count / 100);	}}