package com.wireless.protocol; public class Food{		public final static short TASTE_SMART_REF = 1;	//taste smart reference	public final static short TASTE_MANUAL_REF = 2;	//taste manual reference		public long foodID;								//the food's id	protected int mAliasId;							//the food's alias id	public int restaurantID;						//the restaurant id that the food belongs to	public Kitchen kitchen;							//the kitchen which the food belongs to	public String name;								//the food's name		String pinyin;									//the simple Chinese pinyin to this food	String mPinyinShortcut;							//the short cut to Chinese pinyin	public String desc;								//the description to this food	public String image;							//the image file name	public short tasteRefType = TASTE_SMART_REF;	//the type to taste reference	public Taste[] popTastes;						//the most popular taste to this food	public int amount;								//the amount to this food, only available as the sub food to a combo	Food[] mAssociatedFoods;						//the associated foods 	public Food[] childFoods;						//the child foods, only available as the food belongs combo	public FoodStatistics statistics;				//the food statistics				int mUnitPrice = 0;								//the unit price of the food		public void setAssocatedFoods(Food[] associatedFoods){		this.mAssociatedFoods = associatedFoods;	}		public Food[] getAssociatedFoods(){		return mAssociatedFoods;	}		public boolean hasAssociatedFoods(){		return mAssociatedFoods == null ? false : mAssociatedFoods.length != 0;	}		public int getAliasId(){		return mAliasId;	}		public void setAliasId(int aliasId){		this.mAliasId = aliasId;	}		public String getPinyinShortcut(){		return mPinyinShortcut;	}		public void setPinyinShortcut(String pinyinShortcut){		mPinyinShortcut = pinyinShortcut;	}		public String getPinyin(){		return pinyin;	}		public void setPinyin(String pinyin){		this.pinyin = pinyin;	}		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		mUnitPrice = Util.float2Int(_price);	}		public Float getPrice(){		return Util.int2Float(mUnitPrice);	}		/**	 * The status of the food.	 * It can be the combination of values below.	 */	public final static short SPECIAL = 0x01;		/* 特价 */	public final static short RECOMMEND = 0x02;		/* 推荐 */ 	public final static short SELL_OUT = 0x04;		/* 售完 */	public final static short GIFT = 0x08;			/* 赠送 */	public final static short CUR_PRICE = 0x10;		/* 时价 */	public final static short COMBO = 0x20;			/* 套菜 */	public final static short HOT = 0x40;			/* 热销 */	public final static short WEIGHT = 0x80;		/* 称重 */		short mStatus = 0;		public short getStatus(){		return mStatus;	}		public void setStatus(short status){		this.mStatus = status;	}		/**	 * Check to see whether the food is special.	 * @return true if the food is special, otherwise false	 */	public boolean isSpecial(){		return ((mStatus & SPECIAL) != 0);	}		/**	 * Set the food to special or not.	 * @param onOff 	 * 			the switch to set the food to special or not	 */	public void setSpecial(boolean onOff){		if(onOff){			mStatus |= SPECIAL;		}else{			mStatus &= ~SPECIAL;		}	}		/**	 * Check to see whether the food is recommended.	 * @return true if the food is recommended, other false	 */	public boolean isRecommend(){		return ((mStatus & RECOMMEND) != 0);	}		/**	 * Check to see whether the food is weigh.	 * @return true if the food is weigh, other false	 */	public boolean isWeigh(){		return ((mStatus & WEIGHT) != 0);	}		/**	 * Set the food to recommended or not.	 * @param onOff 	 * 			the switch to set the food to recommended or not	 */	public void setRecommend(boolean onOff){		if(onOff){			mStatus |= RECOMMEND;		}else{			mStatus &= ~RECOMMEND;		}	}		/**	 * Check to see whether the food is sell out.	 * @return true if the food is sell out, other false	 */	public boolean isSellOut(){		return ((mStatus & SELL_OUT) != 0);	}		/**	 * Set the food to sell out or not.	 * @param onOff 	 * 			the switch to set the food to sell out or not	 */	public void setSellOut(boolean onOff){		if(onOff){			mStatus |= SELL_OUT;		}else{			mStatus &= ~SELL_OUT;		}	}		/**	 * Check to see whether the food is gifted.	 * @return true if the food is gifted, other false	 */	public boolean isGift(){		return ((mStatus & GIFT) != 0);		}		/**	 * Set the food to gift or not.	 * @param onOff 	 * 			the switch to set the food to gift or not	 */	public void setGift(boolean onOff){		if(onOff){			mStatus |= GIFT;		}else{			mStatus &= ~GIFT;		}	}		/**	 * Check to see whether the food is current price.	 * @return true if the food is current price, other false	 */	public boolean isCurPrice(){		return ((mStatus & CUR_PRICE) != 0);	}		/**	 * Set the food to current price or not.	 * @param onOff 	 * 			the switch to set the food to current price or not	 */	public void setCurPrice(boolean onOff){		if(onOff){			mStatus |= CUR_PRICE;		}else{			mStatus &= ~CUR_PRICE;		}	}		/**	 * Check to see whether the food is combo.	 * @return true if the food is combo, other false	 */	public boolean isCombo(){		return ((mStatus & COMBO) != 0);	}		/**	 * Set the food to combo or not.	 * @param onOff 	 * 			the switch to set the food to combo or not	 */	public void setCombo(boolean onOff){		if(onOff){			mStatus |= COMBO;		}else{			mStatus &= ~COMBO;		}	}		/**	 * Set the food to weigh or not.	 * @param onOff 	 * 			the switch to set the food to weigh or not	 */	public void setWeigh(boolean onOff){		if(onOff){			mStatus |= WEIGHT;		}else{			mStatus &= ~WEIGHT;		}	}		/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isHot(){		return ((mStatus & HOT) != 0);	}		/**	 * Set the food to hot or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setHot(boolean onOff){		if(onOff){			mStatus |= HOT;		}else{			mStatus &= ~HOT;		}	}		public boolean equals(Object obj){		if(obj == null || !(obj instanceof Food)){			return false;		}else{			return restaurantID == ((Food)obj).restaurantID && mAliasId == ((Food)obj).mAliasId;		}	}		public int hashCode(){		return new Integer(restaurantID).hashCode() ^			   new Integer(mAliasId).hashCode();	}		public Food(){		this.kitchen = new Kitchen();	}		public Food(long foodID, int aliasID, int restaurantID){		this();		this.foodID = foodID;		this.mAliasId = aliasID;		this.restaurantID = restaurantID;	}		public Food(int aliasID, String name){		this();		this.mAliasId = aliasID;		this.name = name.trim();	}		public Food(int aliasID, String name, Float price){		this(aliasID, name);		//split the price into 3-byte value		//get the float point from the price		setPrice(price);	}	public Food(int restaurantID, long foodID, int foodAlias, String name, Float price, FoodStatistics statistics,				short status, String pinyin, String pinyinShortcut, short tasteRefType, String desc, String image, Kitchen kitchen){		this.mAliasId = foodAlias;		this.name = name.trim();		setPrice(price);		this.restaurantID = restaurantID;		this.foodID = foodID;		this.mStatus = status;		this.pinyin = pinyin;		this.mPinyinShortcut = pinyinShortcut;		this.tasteRefType = tasteRefType;		this.desc = desc;		this.image = image;		this.kitchen = kitchen;		this.statistics = statistics;	}	}