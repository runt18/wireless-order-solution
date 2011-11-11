package com.wireless.protocol;public class Food{	public int alias_id = 0;						//the food's alias id	public short kitchen = Kitchen.KITCHEN_NULL;	//the kitchen which the food belong to	public String name = "";						//the food's name		public Taste[] tastes = new Taste[3];			//three tastes the food can consist of		public static final int FOOD_NORMAL = 0;		/* 正常 */	public static final int FOOD_HANG_UP = 1;		/* 叫起 */	public static final int FOOD_IMMEDIATE = 2;		/* 即起 */	public short hangStatus = FOOD_NORMAL;			//indicates whether the food is hang up		public boolean isTemporary = false;					//indicates whether the food is temporary		/**	 * Since a food can consist of three tastes at most,	 * combine the taste into a signal string.	 */	public String tastePref = Taste.NO_PREFERENCE;		/**	 * Add a taste to the food.	 * @param taste the taste to be add	 * @return a negative number if no available taste can be set,	 * 		   a positive number if add taste successfully.	 * 	 */	public int addTaste(Taste taste){		/**		 * Enumerate to check whether an available taste can be added		 */		int tastePos = 0;		for(; tastePos < tastes.length; tastePos++){			if(tastes[tastePos].alias_id == Taste.NO_TASTE){				break;			}		}				if(tastePos < tastes.length){			/**			 * Add the taste to one of the three available tastes 			 */			try{				//assign the taste id 				tastes[tastePos].alias_id = taste.alias_id;				//assign the taste preference 				tastes[tastePos].preference = taste.preference;				//assign the taste category				tastes[tastePos].category = taste.category;				//assign the calculate type				tastes[tastePos].calc = taste.calc;				//assign the taste price rate				tastes[tastePos].setRate(taste.getRate());				//assign the taste price				tastes[tastePos].setPrice(taste.getPrice());			}catch(ArrayIndexOutOfBoundsException e){}							//sort the tastes			for(int i = 0; i < tastes.length; i++){				for(int j = i + 1; j < tastes.length; j++){					if(tastes[i].compare(tastes[j]) > 0){						Taste tmpTaste = tastes[i];						tastes[i] = tastes[j];						tastes[j] = tmpTaste;					}				}			}						/**			 * Calculate the taste price and preference			 */			tastePref = Util.genTastePref(tastes);			setTastePrice(Util.genTastePrice(tastes, getPrice()));						return tastePos;					}else{			return -1;		}	}		/**	 * Remove the specific taste from the food's taste list	 * @param taste the taste to be deleted	 * @return a negative number if the taste to be deleted is NOT exist,	 * 		   a positive number if remove taste successfully.	 */	public int removeTaste(Taste taste){		/**		 * Enumerate to check whether the taste to delete is exist		 */		int tastePos = 0;		for(; tastePos < tastes.length; tastePos++){			if(taste.alias_id == tastes[tastePos].alias_id){				break;			}		}				if(tastePos < tastes.length){			tastes[tastePos] = new Taste();			//sort the tastes			for(int i = 0; i < tastes.length; i++){				for(int j = i + 1; j < tastes.length; j++){					if(tastes[i].compare(tastes[j]) > 0){						Taste tmpTaste = tastes[i];						tastes[i] = tastes[j];						tastes[j] = tmpTaste;					}				}			}			/**			 * Calculate the taste price and preference			 */			tastePref = Util.genTastePref(tastes);			setTastePrice(Util.genTastePrice(tastes, getPrice()));			return tastePos;		}else{			return -1;		}	}		/**	 * Here we use an integer to represent the total price to the tastes.	 */	int tastePrice = 0;		public void setTastePrice(Float price){		tastePrice = Util.float2Int(price);	}		public Float getTastePrice(){		return Util.int2Float(tastePrice);	}	/**	 * Here we use an integer to represent the amount of ordered food.	 */	int count = 0;		//the number of the food to be ordered		public void setCount(Float _count){		count = Util.float2Int(_count);	}		public Float getCount(){		return Util.int2Float(count);	}		/**	 * Here we use an integer to represent the unit price of the food.	 */	int price = 0;		//the unit price of the food		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		price = Util.float2Int(_price);	}		public Float getPrice(){		return Util.int2Float(price);	}		/**	 * The value of discount ranges from 0.00 through 1.00	 * Since the 8100 doesn't support float, we instead to use 0 through 100.	 * So the real price should be divided 100 at last. 	 */	byte discount = 100;	//the discount to this food 		public void setDiscount(Float _discount){		discount = (byte)Util.float2Int(_discount);	}		public Float getDiscount(){		return Util.int2Float(discount);	}		/**	 * The simple Chinese pinyin to this food	 */	public String pinyin = null;		/**	 * Indicates the food is hurried	 */	public boolean isHurried = false;		/**	 * The status of the food.	 * It can be the combination of values below.	 */	public final static byte SPECIAL = 0x01;	/* 特价 */	public final static byte RECOMMEND = 0x02;	/* 推荐 */ 	public final static byte SELL_OUT = 0x04;	/* 售完 */	public final static byte GIFT = 0x08;		/* 赠送 */	public short status = 0;		public boolean isSpecial(){		return ((status & SPECIAL) != 0);	}		public boolean isRecommend(){		return ((status & RECOMMEND) != 0);	}		public boolean isSellOut(){		return ((status & SELL_OUT) != 0);	}		public boolean isGift(){		return ((status & GIFT) != 0);	}		/**	 * There are three ways to determine whether two foods is the same as each other.	 * 1 - If one food is temporary while the other NOT, means they are NOT the same.	 * 2 - If both of foods are temporary, check to see whether their names and price are the same.	 *     They are the same if both name and price is matched.	 * 3 - If both of foods are NOT temporary, check to see their food, all tastes id and hang status.	 *     They are the same if all of the things above are matched.	 */	public boolean equals(Object obj){		Food food = (Food)obj;		if(isTemporary != food.isTemporary){			return false;		}else if(isTemporary && food.isTemporary){			return name.equals(food.name) && (price == food.price) && (hangStatus == food.hangStatus);		}else{			return alias_id == food.alias_id &&				   tastes[0].alias_id == food.tastes[0].alias_id &&				   tastes[1].alias_id == food.tastes[1].alias_id &&				   tastes[2].alias_id == food.tastes[2].alias_id &&				   hangStatus == food.hangStatus;		}	}		/**	 * Two foods are the same if both food and taste id is matched	 * @param food	 * @return	 */	public boolean equals2(Food food){		return alias_id == food.alias_id &&		   tastes[0].alias_id == food.tastes[0].alias_id &&		   tastes[1].alias_id == food.tastes[1].alias_id &&		   tastes[2].alias_id == food.tastes[2].alias_id ;	}		public Food(){		for(int i = 0; i < tastes.length; i++){			tastes[i] = new Taste();		}	}		public Food(int alias_id, String _name){		this.alias_id = alias_id;		name = _name;		for(int i = 0; i < tastes.length; i++){			tastes[i] = new Taste();		}	}		public Food(int alias_id, String _name, Float _price){		this(alias_id, _name);		//split the price into 3-byte value		//get the float point from the price		setPrice(_price);	}	public Food(int alias_id, String _name, Float _price, short _kitchen, short _status, String _pinyin){		this(alias_id, _name, _price);		kitchen = _kitchen;		status = _status;		pinyin = _pinyin;	}		/**	 * The 2nd unit price to food is as below.	 * unit_price = food_price * discount + taste_price	 * @return the unit price represented as Float 	 */	public Float getPrice2(){		return Util.int2Float(price2());	}		/**	 * The 2nd unit price to food is as below.	 * unit_price = food_price * discount + taste_price	 * If taste price is calculated by rate, then	 * taste_price = food_price * taste_rate	 * @return the unit price represented as an integer	 */	int price2(){		return price * discount / 100 + tastePrice;	}		/**	 * Calculate the total price to this food as below.	 * <br>price = food_price * discount * count 	 * @return the total price to this food	 */	public Float totalPrice(){		return Util.int2Float((price * discount * count) / 10000);	}		/**	 * Calculate the total price to this food as below.	 * <br>price = (food_price * discount + taste_price) * count 	 * @return the total price to this food	 */	public Float totalPrice2(){		return Util.int2Float(price2() * count / 100);	}}