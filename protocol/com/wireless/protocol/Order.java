package com.wireless.protocol;public class Order{	public int id = 0;				//the id to this order	public Food[] foods = null;		//the order foods	public short tableID = 0;		//the table alias id	public int customNum = 0;		//the custom number	/**	 * Here we use an integer to represent the total price of the order.	 * In Java, an integer has 4-byte long.	 * And we use 4-byte to represent the value, as below	 * AA AA AA BB	 * AA AA AA - 3-byte indicates the fixed-point, range from 0 through 1118481	 * BB - 1-byte indicates the float-point, range from 0 through 99	 */	private int totalPrice = 0;	//the total price of this order		public int getTotalPrice() {		return totalPrice;	}		public void setTotalPrice(int _price){		totalPrice = _price;	}		public void setTotalPrice(Float _price) {		//split the total price into 4-byte value		//get the float point from the price		String floatPart = _price.toString();		int pos = floatPart.indexOf(".");		if(pos != -1){			byte decimal = Byte.parseByte(floatPart.substring(pos + 1));			int integer = (int)_price.floatValue();			totalPrice = ((integer & 0x00FFFFFF) << 8) | (decimal & 0x000000FF);					}else{			throw new NumberFormatException();		}	}		public Order(){			}		public Order(Food[] _foods, short _tableID, int _customNum){		foods = _foods;		tableID = _tableID;		customNum = _customNum;	}			public Order(int _orderID, Food[] _foods, short _tableID, int _customNum){		id = _orderID;		foods = _foods;		tableID = _tableID;		customNum = _customNum;	}		public Order(Food[] _foods, short _tableID, int _customNum, Float _price){		foods = _foods;		tableID = _tableID;		customNum = _customNum;		setTotalPrice(_price);	}		public Order(int _orderID, Food[] _foods, short _tableID, int _customNum, Float _price){		id = _orderID;		foods = _foods;		tableID = _tableID;		customNum = _customNum;		setTotalPrice(_price);	}		/**	 * Convert the total price of the order to string.	 * Note that the value accurate to two decimal parts 	 * and add the "￥" character in front of the converted string.<br>	 * For example as below.<br>	 * "1" shown as "￥1".<br>	 * "1.1" shown as "￥1.10".<br>	 * "1.23" shown as "￥1.23".<br>	 * @return the converted string	 */	public String price2String(){		String integer = new Integer((totalPrice & 0xFFFFFF00) >> 8).toString();		String decimal = new Byte((byte)(totalPrice & 0x000000FF)).toString();		if((totalPrice & 0x000000FF) < 10){			return "￥" + integer + ".0" + decimal;					}else{			return "￥" + integer + "." + decimal;					}			}		/**	 * Sum all the foods price and assign to total price,	 * then convert the total price to String,	 * add the "￥" character in front of the converted string.	 * For example as below.<br>	 * "1" shown as "￥1".<br>	 * "1.1" shown as "￥1.10".<br>	 * "1.23" shown as "￥1.23".<br>	 * @return the converted string	 */	public String price2StringEx(){		totalPrice = 0;		int total = 0;		for(int i = 0; i < foods.length; i++){			int unitPrice = ((foods[i].price & 0xFFFFFF00) >> 8) * 100 + (foods[i].price & 0x000000FF);			int cnt = ((foods[i].count & 0x0000FF00) >> 8) * 100 + (foods[i].count & 0x000000FF);			total += ((unitPrice * cnt) / 100);		}		totalPrice += ((((total / 100) << 8) & 0xFFFFFF00) | ((total % 100) & 0x000000FF));		return price2String();	}		/**	 * Convert the order's total price to float object 	 * @return the float object indicates the total price	 */	public Float price2Float(){		return new Float(((totalPrice & 0xFFFFFF00) >> 8) + ((totalPrice & 0x000000FF) * 0.01));	}		/**	 * Calculate the total price as below	 * unit = food_price	 * price = unit * count	 * total = price1 + price2 + ...	 * @return the total price	 */	public int totalPrice(){		int total = 0;		for(int i = 0; i < foods.length; i++){			int unitPrice = ((foods[i].price & 0xFFFFFF00) >> 8) * 100 + (foods[i].price & 0x000000FF);			int cnt = ((foods[i].count & 0x0000FF00) >> 8) * 100 + (foods[i].count & 0x000000FF);			total += ((unitPrice * cnt) / 100);		}		return total;	}		/**	 * Calculate the total price as below	 * unit = food_price + taste_price	 * price = unit * count	 * total = price1 + price2 + ...	 * @return the total price	 */	public int totalPrice2(){		int total = 0;		for(int i = 0; i < foods.length; i++){			int unitPrice = ((foods[i].unitPrice2() & 0xFFFFFF00) >> 8) * 100 + (foods[i].price & 0x000000FF);			int cnt = ((foods[i].count & 0x0000FF00) >> 8) * 100 + (foods[i].count & 0x000000FF);			total += ((unitPrice * cnt) / 100);		}		return total;	}		/**	 * Convert the total price of the order to string.	 * Note that the value accurate to two decimal parts 	 * and add the "￥" character in front of the converted string.<br>	 * For example as below.<br>	 * "1" shown as "￥1".<br>	 * "1.1" shown as "￥1.10".<br>	 * "1.23" shown as "￥1.23".<br>	 * 	 * @param priceInt the price represented as an integer	 * @return the converted string	 */	public String price2String(int priceInt){		String integer = new Integer((priceInt & 0xFFFFFF00) >> 8).toString();		String decimal = new Byte((byte)(priceInt & 0x000000FF)).toString();		if((totalPrice & 0x000000FF) < 10){			return "￥" + integer + ".0" + decimal;					}else{			return "￥" + integer + "." + decimal;					}	}		/**	 * Convert price to Float object 	 * @param priceInt the price represented as an integer	 * @return the Float object	 */	public Float price2Float(int priceInt){		return new Float(((priceInt & 0xFFFFFF00) >> 8) + ((priceInt & 0x000000FF) * 0.01));	}	}