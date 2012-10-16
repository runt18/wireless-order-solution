package com.wireless.protocol;import com.wireless.excep.BusinessException;public class Order{		/**	 * The payment type is as below	 */	public final static int PAY_NORMAL = 1;	public final static int PAY_MEMBER = 2;	public int pay_type = PAY_NORMAL;		//the payment type	/**	 * The pay manner is as below	 */	public final static int MANNER_CASH = 1;		//现金	public final static int MANNER_CREDIT_CARD = 2;	//刷卡	public final static int MANNER_MEMBER = 3;		//会员卡	public final static int MANNER_SIGN = 4;		//签单	public final static int MANNER_HANG = 5;		//挂账	public int pay_manner = MANNER_CASH;	//the payment manner	/**	 * The category is as below. 		 */	public final static short CATE_NORMAL = 1;			//一般	public final static short CATE_TAKE_OUT = 2;		//外卖	public final static short CATE_JOIN_TABLE = 3;		//并台	public final static short CATE_MERGER_TABLE = 4;	//拼台	public short category = CATE_NORMAL;	//the category to this order		/**	 * The service rate to this order	 */	int serviceRate = 0;		public Float getServiceRate(){		return Util.int2Float(serviceRate);	}		public void setServiceRate(Float _rate){		serviceRate = Util.float2Int(_rate);	}	//the id to this order	public int id;			//the sequence id to this order	public int seqID;	//the restaurant id this order belong to	public int restaurantID;		//the order date time	public long orderDate;	//the order foods	public OrderFood[] foods;		//the discount to this order	Discount discount;		//the region info to this order	public Region region = new Region();		//the destination table to this order	public Table destTbl = new Table();				//the 2nd table info to this order(used for table merger)	public Table destTbl2 = new Table();		//the source table to this order which is used for table transfer	public Table srcTbl = new Table();	//the custom number	public int customNum;				//the id to member	public String memberID;			//the comment to this order	public String comment;		//the print type to this order	public int print_type = Reserved.DEFAULT_CONF;	//indicates the order has been paid before	public boolean isPaid = false;		/**	 * The value of minimum cost to this order, ranges from 99999.99 through 0.00	 * Since the 8100 doesn't support float, we instead to use 0 through 9999999.	 * So the real price should be divided 100 at last. 	 */	int minCost = 0;		public void setMinimumCost(Float cost){		minCost = Util.float2Int(cost);	}		public Float getMinimumCost(){		return Util.int2Float(minCost);	}		/**	 * Here we use an integer to represent the cash income to this order.	 * The cash income only takes effect while the pay in cash.	 */	int cashIncome = 0;				//the cash income to this order		public void setCashIncome(Float _price) {		cashIncome = Util.float2Int(_price);	}		public Float getCashIncome(){		return Util.int2Float(cashIncome);	}		/**	 * Here we use an integer to represent the total price of the order.	 * If total price is the minimum integer, means the order is unpaid.	 * Otherwise means the order is paid. 	 */	int totalPrice = -1;	//the total price of this order		public void setTotalPrice(Float _price){		totalPrice = Util.float2Int(_price);	}		public Float getTotalPrice(){		return Util.int2Float(totalPrice);	}		/**	 * Here we use an integer to represent the actual price of the order.	 * 如果客户没有设定尾数的处理方式，实收金额会等于合计金额,	 * 如果客户设定了尾数的处理方式，比如“抹零”、“四舍五入”等，实收金额就会根据不同处理方式变化	 */	int actualPrice = 0;		public void setActualPrice(Float _price){		actualPrice = Util.float2Int(_price);	}		public Float getActualPrice(){		return Util.int2Float(actualPrice);	}		public Order(){		this.foods = new OrderFood[0];		this.discount = new Discount();			}		public Order(OrderFood[] foods){		this.discount = new Discount();		this.foods = foods;	}		public Order(OrderFood[] foods, int tableAlias, int customNum){		this(foods);		this.destTbl.aliasID = tableAlias;		this.customNum = customNum;	}			public Order(int orderID, OrderFood[] foods, int tableAlias, int customNum){		this(foods, tableAlias, customNum);		id = orderID;	}		public Order(OrderFood[] foods, short tableAlias, int customNum, Float price){		this(foods, tableAlias, customNum);		setCashIncome(price);	}		public Order(int orderID, OrderFood[] foods, short tableAlias, int customNum, Float price){		this(orderID, foods, tableAlias, customNum);		setCashIncome(price);	}		/**	 * Set the discount to this order.	 * The discount to each food is also be set. 	 * @param discount The discount to be set.	 */	public void setDiscount(Discount discount){		this.discount = discount;		for(int i = 0; i < foods.length; i++){			boolean isFound = false;			for(int j = 0; j < discount.plans.length; j++){				if(foods[i].kitchen.equals(discount.plans[j].kitchen)){					foods[i].setDiscountInternal(discount.plans[j].rate);					isFound = true;					break;				}			}			if(!isFound){				foods[i].setDiscountInternal(100);			}		}	}		/**	 * Get the discount to this order.	 * @return The discount to this order.	 */	public Discount getDiscount(){		return discount;	}		/**	 * Calculate the total price without taste price(included the gifted food) as below	 * unit = food_price	 * price[n] = unit * count	 * total = price[1] + price[2] + ... price[n]	 * @return the total price	 */	public Float calcPrice(){		int total = 0;		for(int i = 0; i < foods.length; i++){			total += ((foods[i].price * foods[i].count) / 100);		}		return Util.int2Float(total);	}		/**	 * Calculate the total price with taste price (exclude the gifted food) as below<br>	 * unit = food_price + taste_price<br>	 * price[n] = unit * count<br>	 * total = price[1] + price[2] + ... price[n]	 * @return the total price	 */	public Float calcPriceWithTaste(){		int totalPrice = 0;		for(int i = 0; i < foods.length; i++){			if(!foods[i].isGift()){				totalPrice += foods[i].calcPriceWithTasteInternal();			}		}		return Util.int2Float(totalPrice);	}		/**	 * Calculate the total price of gifted foods.	 * @return the total price of gifted foods	 */	public Float calcGiftPrice(){		int totalGifted = 0;		for(int i = 0; i < foods.length; i++){			if(foods[i].isGift()){				totalGifted += foods[i].calcPriceWithTasteInternal();			}		}		return Util.int2Float(totalGifted);	}		/**	 * Calculate the total discount price.	 * @return the total discount	 */	public Float calcDiscountPrice(){		int totalDiscount = 0;		for(int i = 0; i < foods.length; i++){			totalDiscount += foods[i].calcDiscountPriceInternal();		}		return Util.int2Float(totalDiscount);	}	/**	 * Calculate the total price before discount to this order	 * @return The total price before discount to this order.	 */	public Float calcPriceBeforeDiscount(){		int totalPrice = 0;		for(int i = 0; i < foods.length; i++){			if(!foods[i].isGift()){				totalPrice += foods[i].calcPriceBeforeDiscountInternal();			}		}		return Util.int2Float(totalPrice);	}		private final static int MAX_ORDER_AMOUNT = 255 * 100;		/**	 * Add the food to the list.	 * @param foodToAdd The food to add	 * @throws BusinessException	 * 			Throws if the order amount of the added food exceed MAX_ORDER_AMOUNT	 */	public void addFood(OrderFood foodToAdd) throws BusinessException{		if(foodToAdd.count > MAX_ORDER_AMOUNT){			throw new BusinessException("对不起，\"" + foodToAdd.toString() + "\"每次最多只能点255份");					}else{			//如果新添加的菜品在原来菜品List中已经存在相同的菜品，则累加数量			boolean isExist = false;			for(int i = 0; i < foods.length; i++){				if(foodToAdd.equals(foods[i])){					int count = foods[i].count + foodToAdd.count;					if(count > MAX_ORDER_AMOUNT){						throw new BusinessException("对不起，\"" + foodToAdd.toString() + "\"每次最多只能点255份");					}else{						foods[i].count = count;					}					isExist = true;					break;				}			}			//如果新添加的菜品在原来菜品List中没有相同的菜品，则添加到菜品列表			if(!isExist){				OrderFood[] newFoods = new OrderFood[foods.length + 1];				System.arraycopy(foods, 0, newFoods, 0, foods.length);				newFoods[foods.length] = foodToAdd;				foods = newFoods;			}					}			}		/**	 * Add the foods to list.	 * @param foodsToAdd The foods to add	 */	public void addFoods(OrderFood[] foodsToAdd){		//遍历新添加的菜品List中，如果与原来菜品相同的，则累加数量		int nExist = 0;		for(int i = 0; i < foodsToAdd.length; i++){			for(int j = 0; j < foods.length; j++){				if(foodsToAdd[i].equals(foods[j])){					foods[j].count = foods[j].count + foodsToAdd[i].count;					if(foods[j].count > MAX_ORDER_AMOUNT * 2){						foods[j].count = MAX_ORDER_AMOUNT * 2;					}					foodsToAdd[i] = null;					nExist++;					break;				}			}		}				//新添加菜品List中，如果与原来菜品不相同的，则添加到菜品列表		OrderFood[] newFoods = new OrderFood[foods.length + foodsToAdd.length - nExist];		System.arraycopy(foods, 0, newFoods, 0, foods.length);				int availPos = foods.length; 		for(int i = 0; i < foodsToAdd.length; i++){			if(foodsToAdd[i] != null){				newFoods[availPos++] = foodsToAdd[i];			}		}		foods = newFoods;	}		/**	 * Remove the food.	 * @param foodToRemove The food to remove.	 * @return true if the food to remove is found, otherwise false.	 */	public boolean remove(OrderFood foodToRemove){		boolean isExist = false;		for(int i = 0; i < foods.length; i++){			if(foods[i].equals(foodToRemove)){				foods[i] = null;				isExist = true;				break;			}		}				if(isExist){			OrderFood[] newFoods = new OrderFood[foods.length - 1];			int pos = 0;			for(int i = 0; i < foods.length; i++){				if(foods[i] != null){					newFoods[pos++] = foods[i];				}			}		}				return isExist;			}	}