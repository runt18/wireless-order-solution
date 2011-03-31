package com.wireless.protocol;public class Food{	public long real_id = 0;			//the food's real id (combined with the restaurant id)	public int alias_id = 0;			//the food's alias id	public short kitchen = Kitchen.KITCHEN_NULL;	//the kitchen which the food belong to	public String name = "";	//the food's name		public Taste taste = new Taste();	//the taste whose default is no taste	/**	 * Here we use an integer to represent the amount of ordered food.	 * In Java, an integer is 4-byte long.	 * And we use 2-byte to represent the value, as below	 * 00 00 AA BB	 * AA - 1-byte indicates the fixed-point, range from 0 through 255	 * BB - 1-byte indicates the float-point, range from 0 through 99	 */	public int count = 0;		//the number of the food to be ordered	/**	 * Here we use an integer to represent the unit price of the food.	 * In Java, an integer is 4-byte long.	 * And we use 3-byte to represent the value, as below	 * 00 BB BB CC	 * BB BB - 2-byte indicates the fixed-point, range from 0 through 65535	 * CC - 1-byte indicates the float-point, range from 0 through 99	 */	public int price = 0;		//the unit price of the food		/**	 * Two foods are the same if both food and taste id is matched.	 */	public boolean equals(Object obj){		return alias_id == ((Food)obj).alias_id &&			   taste.alias_id == ((Food)obj).taste.alias_id;	}		public Food(){			}		public Food(int alias_id, String _name){		this.alias_id = alias_id;		name = _name;	}		public Food(int alias_id, String _name, Float _price){		this(alias_id, _name);		//split the price into 3-byte value		//get the float point from the price		setPrice(_price);	}		public Food(int alias_id, String _name, Float _count, Float _price){		this(alias_id, _name, _count);		//split the price into 3-byte value		//get the float point from the price		setPrice(_price);	}			public void setCount(Float _count){		String floatPoint = _count.toString();		floatPoint = floatPoint.substring(floatPoint.indexOf(".") + 1);		//make sure the count reserved two decimals 		if(floatPoint.length() == 1){			//in the case only the tenth digit exist,			//append the "0" to the end 			floatPoint = floatPoint + "0";					}else if(floatPoint.length() == 2){			//in the case the tenth digit is "0"			//cut this tenth digit			if(floatPoint.charAt(0) == '0'){				floatPoint = floatPoint.substring(1);			}					}else{			throw new NumberFormatException();		}				byte decimal = Byte.parseByte(floatPoint);		int fixedPoint = (int)_count.floatValue();		count = 0x0000FFFF & (((fixedPoint & 0x000000FF) << 8) | (decimal & 0x000000FF));	}		/**	 * Convert the number of ordered food to string.	 * Note that the value accurate to two decimal parts.<br>	 * For example as below.<br>	 * "1" shown as "1".<br>	 * "1.1" shown as "1.1".<br>	 * "1.23" shown as "1.23".<br>	 * @return the converted string	 */	public String count2String(){		String integer = new Integer((count & 0x0000FF00) >> 8).toString();		String decimal = new Byte((byte)(count & 0x000000FF)).toString();		if((count & 0x000000FF) == 0){			return integer;					}else if((count & 0x000000FF) > 0 && (count & 0x000000FF) < 10){			return integer + ".0" + decimal;					}else if((count & 0x000000FF) % 10 == 0){			return integer + "." + decimal.substring(0, 1);					}else{			return integer + "." + decimal;		}	}		/**	 * Convert the count value to float object.	 * @return the Float object indicates the count value	 */	public Float count2Float(){		return new Float(((count & 0x0000FF00) >> 8) + ((count & 0x000000FF) * 0.01));	}		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		//split the price into 3-byte value		//get the float point from the price		String floatPoint = _price.toString();		floatPoint = floatPoint.substring(floatPoint.indexOf(".") + 1);		//make sure the count reserved two decimals 		if(floatPoint.length() == 1){			//in the case only the tenth digit exist,			//append the "0" to the end 			floatPoint = floatPoint + "0";					}else if(floatPoint.length() == 2){			//in the case the tenth digit is "0"			//cut this tenth digit			if(floatPoint.charAt(0) == '0'){				floatPoint = floatPoint.substring(1);			}					}else{			throw new NumberFormatException();		}				byte decimal = Byte.parseByte(floatPoint);		int integer = (int)_price.floatValue();		price = 0x00FFFFFF & (((integer & 0x0000FFFF) << 8) | (decimal & 0x000000FF));	}		/**	 * Convert the unit price of the ordered food to Float object	 * @return the Float object indicates the unit price of the ordered food	 */	public Float price2Float(){		return new Float(((price & 0x00FFFF00) >> 8) + ((price & 0x000000FF) * 0.01));	}		/**	 * Convert the unit price of ordered food to string.	 * Note that the value accurate to two decimal parts 	 * and add the "￥" character in front of the converted string.<br>	 * For example as below.<br>	 * "1" shown as "￥1".<br>	 * "1.1" shown as "￥1.10".<br>	 * "1.23" shown as "￥1.23".<br>	 * @return the converted string	 *///	public String price2String(){//		String integer = new Integer((price & 0x00FFFF00) >> 8).toString();//		String decimal = new Byte((byte)(price & 0x000000FF)).toString();//		if((price & 0x000000FF) < 10){//			return "￥" + integer + ".0" + decimal;			//		}else{//			return "￥" + integer + "." + decimal;			//		}//	}		/**	 * Convert the price of the ordered food to String,	 * <br>price = unit * count<br>	 * Note that the value accurate to two decimal parts 	 * and add the "￥" character in front of the converted string.<br>	 * For example as below.<br>	 * "1" shown as "￥1".<br>	 * "1.1" shown as "￥1.10".<br>	 * "1.23" shown as "￥1.23".<br>	 * @return the converted string of the ordered food's price	 *///	public String price2StringEx(){//		int unitPrice = ((price & 0xFFFFFF00) >> 8) * 100 + (price & 0x000000FF);//		int cnt = ((count & 0x0000FF00) >> 8) * 100 + (count & 0x000000FF);//		int price2 = (unitPrice * cnt) / 100;//		String integer = new Integer(price2 / 100).toString();//		byte decVal = (byte)(price2 % 100);//		String decimal = new Byte(decVal).toString();//		if(decVal < 10){//			return "￥" + integer + ".0" + decimal;	//		}else{//			return "￥" + integer + "." + decimal;	//		}//	}		/**	 * Convert the price (unit * count) of the ordered food to Float object.	 * @return the Float object indicates the ordered food's price	 *///	public Float price2FloatEx(){//		int unitPrice = ((price & 0xFFFFFF00) >> 8) * 100 + (price & 0x000000FF);//		int cnt = ((count & 0x0000FF00) >> 8) * 100 + (count & 0x000000FF);//		int price2 = (unitPrice * cnt) / 100;//		return new Float((price2 / 100) + (price2 % 100) * 0.01);//		//	}		/**	 * Convert the price to Float object	 * @param priceInt the price represented as an integer	 * @return	 *///	public Float price2Float(int priceInt){//		return new Float(((priceInt & 0x00FFFF00) >> 8) + ((priceInt & 0x000000FF) * 0.01));//	}		/**	 * Convert the price to string.	 * Note that the value accurate to two decimal parts 	 * and add the "￥" character in front of the converted string.<br>	 * For example as below.<br>	 * "1" shown as "￥1".<br>	 * "1.1" shown as "￥1.10".<br>	 * "1.23" shown as "￥1.23".<br>	 * @param priceInt the price represented as an integer	 * @return the converted string	 *///	public String price2String(int priceInt){//		String integer = new Integer((priceInt & 0x00FFFF00) >> 8).toString();//		String decimal = new Byte((byte)(priceInt & 0x000000FF)).toString();//		if((priceInt & 0x000000FF) < 10){//			return "￥" + integer + ".0" + decimal;			//		}else{//			return "￥" + integer + "." + decimal;			//		}//	}		/**	 * The 2nd unit price to food is as below.	 * unit_price = food_price + taste_price	 * @return the unit price represented as an integer	 */	public int price2(){		int foodPrice = ((price & 0xFFFFFF00) >> 8) * 100 + (price & 0x000000FF);		int tastePrice = ((taste.price & 0xFFFFFF00) >> 8) * 100 + (taste.price & 0x000000FF);		int price = foodPrice + tastePrice;		return (((price / 100)  << 8) & 0xFFFFFF00) | ((price % 100) & 0x000000FF); 	}		/**	 * Calculate the total price to this food as below.	 * <br>price = food_price * count 	 * @return the total price to this food	 */	public int totalPrice(){		int foodPrice = ((price & 0xFFFFFF00) >> 8) * 100 + (price & 0x000000FF);		int cnt = ((count & 0x0000FF00) >> 8) * 100 + (count & 0x000000FF);		int price = (foodPrice * cnt) / 100;		return (((price / 100)  << 8) & 0xFFFFFF00) | ((price % 100) & 0x000000FF);	}		/**	 * Calculate the total price to this food as below.	 * <br>price = (food_price + taste_price) * count 	 * @return the total price to this food	 */	public int totalPrice2(){		int unit = ((price2() & 0xFFFFFF00) >> 8) * 100 + (price2() & 0x000000FF);		int cnt = ((count & 0x0000FF00) >> 8) * 100 + (count & 0x000000FF);		int price = unit * cnt / 100;		return (((price / 100)  << 8) & 0xFFFFFF00) | ((price % 100) & 0x000000FF); 	}}