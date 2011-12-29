package com.wireless.protocol;public class Order{		/**	 * The payment type is as below	 */	public final static int PAY_NORMAL = 1;	public final static int PAY_MEMBER = 2;	public int pay_type = PAY_NORMAL;		//the payment type	/**	 * The discount type is as below	 */	public final static int DISCOUNT_1 = 1;	public final static int DISCOUNT_2 = 2;	public final static int DISCOUNT_3 = 3;	public int discount_type = DISCOUNT_1;	//the discount type 	/**	 * The pay manner is as below	 */	public final static int MANNER_CASH = 1;		//现金	public final static int MANNER_CREDIT_CARD = 2;	//刷卡	public final static int MANNER_MEMBER = 3;		//会员卡	public final static int MANNER_SIGN = 4;		//签单	public final static int MANNER_HANG = 5;		//挂账	public int pay_manner = MANNER_CASH;	//the payment manner	/**	 * The category is as below. 		 */	public final static short CATE_NORMAL = 1;			//一般	public final static short CATE_TAKE_OUT = 2;		//外卖	public final static short CATE_JOIN_TABLE = 3;		//并台	public final static short CATE_MERGER_TABLE = 4;	//拼台	public short category = CATE_NORMAL;	//the category to this order		/**	 * The type to handle the tail of total price	 */	public final static short TAIL_NO_ACTION = 0;			//小数部分不处理	public final static short TAIL_DECIMAL_CUT = 1;			//小数抹零	public final static short TAIL_DECIMAL_ROUND = 2;		//小数四舍五入	public short price_tail = TAIL_NO_ACTION;	//the action to price tail	/**	 * The value of service rate to this order, ranges from 0.00 through 1.00	 * Since the 8100 doesn't support float, we instead to use 0 through 100.	 * So the real price should be divided 100 at last. 	 */	public byte service_rate = 0;		public int id = 0;						//the id to this order	public int restaurant_id = 0;			//the restaurant id this order belong to	public OrderFood[] foods = null;				//the order foods	public int table_id = 0;				//the table alias id this order belong to	public String table_name = null;		//the table alias name this order belong to	public int table2_id = 0;				//the 2nd table alias id this order belong to (used for table merger)	public String table2_name = null;		//the 2nd table alias name this order belong to (used for table merger)	public int originalTableID = 0;			//the original table id used for table transferred	public int custom_num = 0;				//the custom number	public String member_id = null;			//the id to member	public String comment = null;			//the comment to this order	public int print_type = Reserved.DEFAULT_CONF; //the print type to this order		/**	 * The value of minimum cost to this order, ranges from 99999.99 through 0.00	 * Since the 8100 doesn't support float, we instead to use 0 through 9999999.	 * So the real price should be divided 100 at last. 	 */	int minCost = 0;		public void setMinimumCost(Float cost){		minCost = Util.float2Int(cost);	}		public Float getMinimumCost(){		return Util.int2Float(minCost);	}		/**	 * The gift price to this order	 */	int giftPrice = 0;		public void setGiftPrice(Float gift){		giftPrice = Util.float2Int(gift);	}		public Float getGiftPrice(){		return Util.int2Float(giftPrice);	}		/**	 * Here we use an integer to represent the cash income to this order.	 * The cash income only takes effect while the pay in cash.	 */	int cashIncome = 0;				//the cash income to this order		public void setCashIncome(Float _price) {		cashIncome = Util.float2Int(_price);	}		public Float getCashIncome(){		return Util.int2Float(cashIncome);	}		/**	 * Here we use an integer to represent the total price of the order.	 * If total price is the minimum integer, means the order is unpaid.	 * Otherwise means the order is paid. 	 */	int totalPrice = -1;	//the total price of this order		public void setTotalPrice(Float _price){		totalPrice = Util.float2Int(_price);	}		public Float getTotalPrice(){		return Util.int2Float(totalPrice);	}		/**	 * Here we use an integer to represent the actual price of the order.	 * 如果客户没有设定尾数的处理方式，实收金额会等于合计金额,	 * 如果客户设定了尾数的处理方式，比如“抹零”、“四舍五入”等，实收金额就会根据不同处理方式变化	 */	int actualPrice = 0;		public void setActualPrice(Float _price){		actualPrice = Util.float2Int(_price);	}		public Float getActualPrice(){		return Util.int2Float(actualPrice);	}		public Order(){			}		public Order(OrderFood[] _foods){		foods = _foods;	}		public Order(OrderFood[] _foods, int _tableID, int _customNum){		foods = _foods;		table_id = _tableID;		custom_num = _customNum;	}			public Order(int _orderID, OrderFood[] _foods, int _tableID, int _customNum){		this(_foods, _tableID, _customNum);		id = _orderID;	}		public Order(OrderFood[] _foods, short _tableID, int _customNum, Float _price){		this(_foods, _tableID, _customNum);		setCashIncome(_price);	}		public Order(int _orderID, OrderFood[] _foods, short _tableID, int _customNum, Float _price){		this(_orderID, _foods, _tableID, _customNum);		setCashIncome(_price);	}		/**	 * Calculate the total price as below	 * unit = food_price	 * price[n] = unit * count	 * total = price[1] + price[2] + ... price[n]	 * @return the total price	 */	public Float calcPrice(){		int total = 0;		for(int i = 0; i < foods.length; i++){			total += ((foods[i].price * foods[i].count) / 100);		}		return Util.int2Float(total);	}		/**	 * Calculate the total price (exclude the gifted food) as below<br>	 * unit = food_price + taste_price<br>	 * price[n] = unit * count<br>	 * total = price[1] + price[2] + ... price[n]	 * @return the total price	 */	public Float calcPrice2(){		int total = 0;		for(int i = 0; i < foods.length; i++){			if(!foods[i].isGift()){				total += ((foods[i].price2() * foods[i].count) / 100);			}		}		return Util.int2Float(total);	}		/**	 * Calculate the total price of gifted foods.	 * @return the total price of gifted foods	 */	public Float calcGiftPrice(){		int total = 0;		for(int i = 0; i < foods.length; i++){			if(foods[i].isGift()){				total += ((foods[i].price2() * foods[i].count) / 100);			}		}		return Util.int2Float(total);	}		/**	 * Calculate the total discount price.	 * @return the total discount	 */	public Float calcDiscountPrice(){		int total = 0;		for(int i = 0; i < foods.length; i++){			if(foods[i].discount != 100){				total += foods[i].price * foods[i].count * (100 - foods[i].discount) / 10000;			}		}		return Util.int2Float(total);	}	}