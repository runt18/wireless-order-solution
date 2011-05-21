package com.wireless.protocol;public class Order{		/**	 * The payment type is as below	 */	public final static int PAY_NORMAL = 1;	public final static int PAY_MEMBER = 2;		/**	 * The discount type is as below	 */	public final static int DISCOUNT_1 = 1;	public final static int DISCOUNT_2 = 2;	public final static int DISCOUNT_3 = 3;		/**	 * The pay manner is as below	 */	public final static int MANNER_CASH = 1;	public final static int MANNER_CREDIT_CARD = 2;	public final static int MANNER_MEMBER = 3;	public final static int MANNER_SIGN = 4;	public final static int MANNER_HANG = 5;		public int id = 0;						//the id to this order	public int restaurant_id = 0;			//the restaurant id this order belong to	public Food[] foods = null;				//the order foods	public short table_id = 0;				//the table alias id this order order belong to	public short originalTableID = 0;		//the original table id used for table transferred	public int custom_num = 0;				//the custom number	public int pay_type = PAY_NORMAL;		//the payment type	public int discount_type = DISCOUNT_1;	//the discount type 	public int pay_manner = MANNER_CASH;	//the payment manner	public String member_id = null;			//the id to member	/**	 * Here we use an integer to represent the actual total price of the order.	 * In Java, an integer has 4-byte long.	 * And we use 4-byte to represent the value, as below	 * AA AA AA BB	 * AA AA AA - 3-byte indicates the fixed-point, range from 0 through 1118481	 * BB - 1-byte indicates the float-point, range from 0 through 99	 */	public int actualPrice = 0;	//the actual price of this order		public void setActualPrice(Float _price) {		actualPrice = convert2Int(_price);	}		/**	 * Here we use an integer to represent the total price of the order.	 * In Java, an integer has 4-byte long.	 * And we use 4-byte to represent the value, as below	 * AA AA AA BB	 * AA AA AA - 3-byte indicates the fixed-point, range from 0 through 1118481	 * BB - 1-byte indicates the float-point, range from 0 through 99	 */	public int totalPrice = 0;	//the total price of this order		public void setTotalPrice(Float _price){		totalPrice = convert2Int(_price);	}		/**	 * Convert the float to int defined by digi-e terminal	 * @param _price the price in the form of Float	 * @return the integer corresponding to the price	 */	private int convert2Int(Float _price){		//split the total price into 4-byte value		//get the float point from the price		String floatPart = _price.toString();		int pos = floatPart.indexOf(".");		if(pos != -1){			byte decimal = Byte.parseByte(floatPart.substring(pos + 1));			int integer = (int)_price.floatValue();			return ((integer & 0x00FFFFFF) << 8) | (decimal & 0x000000FF);					}else{			throw new NumberFormatException();		}	}		public Order(){			}		public Order(Food[] _foods, short _tableID, int _customNum){		foods = _foods;		table_id = _tableID;		custom_num = _customNum;	}			public Order(int _orderID, Food[] _foods, short _tableID, int _customNum){		this(_foods, _tableID, _customNum);		id = _orderID;	}		public Order(Food[] _foods, short _tableID, int _customNum, Float _price){		this(_foods, _tableID, _customNum);		setActualPrice(_price);	}		public Order(int _orderID, Food[] _foods, short _tableID, int _customNum, Float _price){		this(_orderID, _foods, _tableID, _customNum);		setActualPrice(_price);	}		/**	 * Calculate the total price as below	 * unit = food_price	 * price[n] = unit * count	 * total = price[1] + price[2] + ... price[n]	 * @return the total price	 */	public int totalPrice(){		int total = 0;		for(int i = 0; i < foods.length; i++){			int unitPrice = ((foods[i].price & 0xFFFFFF00) >> 8) * 100 + (foods[i].price & 0x000000FF);			int cnt = ((foods[i].count & 0x0000FF00) >> 8) * 100 + (foods[i].count & 0x000000FF);			total += ((unitPrice * cnt) / 100);		}		return (((total / 100) << 8) & 0xFFFFFF00) | ((total % 100) & 0x000000FF);	}		/**	 * Calculate the total price as below<br>	 * unit = food_price + taste_price<br>	 * price[n] = unit * count<br>	 * total = price[1] + price[2] + ... price[n]	 * @return the total price	 */	public int totalPrice2(){		int total = 0;		for(int i = 0; i < foods.length; i++){			int unitPrice = ((foods[i].price2() & 0xFFFFFF00) >> 8) * 100 + (foods[i].price2() & 0x000000FF);			int cnt = ((foods[i].count & 0x0000FF00) >> 8) * 100 + (foods[i].count & 0x000000FF);			total += ((unitPrice * cnt) / 100);		}		return (((total / 100) << 8) & 0xFFFFFF00) | ((total % 100) & 0x000000FF);	}	}