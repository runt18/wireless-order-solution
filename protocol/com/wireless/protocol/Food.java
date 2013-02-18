package com.wireless.protocol;import com.wireless.protocol.parcel.Parcel;import com.wireless.protocol.parcel.Parcelable;import com.wireless.util.NumericUtil; public class Food implements Parcelable{		public final static byte FOOD_PARCELABLE_COMPLEX = 0;	public final static byte FOOD_PARCELABLE_SIMPLE = 1;		public final static short TASTE_SMART_REF = 1;	//taste smart reference	public final static short TASTE_MANUAL_REF = 2;	//taste manual reference		long mFoodId;									//the food's id	int mAliasId;									//the food's alias id	int mRestaurantID;								//the restaurant id that the food belongs to	Kitchen mKitchen;								//the kitchen which the food belongs to	String mName;									//the food's name		String mPinyin;									//the simple Chinese pinyin to this food	String mPinyinShortcut;							//the short cut to Chinese pinyin	public String desc;								//the description to this food	public String image;							//the image file name	public short tasteRefType = TASTE_SMART_REF;	//the type to taste reference	Taste[] mPopTastes;								//the most popular taste to this food	int mAmount;									//the amount to this food, only available as the sub food to a combo	Food[] mAssociatedFoods;						//the associated foods 	Food[] mChildFoods;								//the child foods, only available as the food belongs combo	int mUnitPrice = 0;								//the unit price of the food	public FoodStatistics statistics;				//the food statistics					public String getImage(){		return image;	}		public void setImage(String image){		this.image = image;	}		public Taste[] getPopTastes(){		return mPopTastes;	}		public void setPopTastes(Taste[] popTastes){		this.mPopTastes = popTastes;	}		public boolean hasPopTastes(){		return mPopTastes == null ? false : mPopTastes.length != 0;	}		public Food[] getChildFoods(){		return mChildFoods;	}		public void setChildFoods(Food[] childFoods){		this.mChildFoods = childFoods;	}		public boolean hasChildFoods(){		return mChildFoods == null ? false : mChildFoods.length != 0;	}		public int getAmount(){		return mAmount;	}		public void setAmount(int amount){		this.mAmount = amount;	}		public Kitchen getKitchen(){		if(mKitchen == null){			setKitchen(new Kitchen());		}		return mKitchen;	}		public void setKitchen(Kitchen kitchen){		this.mKitchen = kitchen;	}		public int getRestaurantId(){		return mRestaurantID;	}		public void setRestaurantId(int restaurantId){		this.mRestaurantID = restaurantId;	}		public void setAssocatedFoods(Food[] associatedFoods){		this.mAssociatedFoods = associatedFoods;	}		public Food[] getAssociatedFoods(){		return mAssociatedFoods;	}		public boolean hasAssociatedFoods(){		return mAssociatedFoods == null ? false : mAssociatedFoods.length != 0;	}		public long getFoodId(){		return this.mFoodId;	}		public void setFoodId(long foodId){		this.mFoodId = foodId;	}		public String getName(){		return this.mName;	}		public void setName(String name){		this.mName = name;	}		public int getAliasId(){		return mAliasId;	}		public void setAliasId(int aliasId){		this.mAliasId = aliasId;	}		public String getPinyinShortcut(){		return mPinyinShortcut;	}		public void setPinyinShortcut(String pinyinShortcut){		mPinyinShortcut = pinyinShortcut;	}		public String getPinyin(){		return mPinyin;	}		public void setPinyin(String pinyin){		this.mPinyin = pinyin;	}		/**	 * Since the price is represented as an integer,	 * and float data type is NOT supported under BlackBerry OS 4.5	 * We use class Float instead of the primitive float type.	 * @param _price the price to taste preference represented by Float	 */	public void setPrice(Float _price){		mUnitPrice = NumericUtil.float2Int(_price);	}		public Float getPrice(){		return NumericUtil.int2Float(mUnitPrice);	}		/**	 * The status of the food.	 * It can be the combination of values below.	 */	public final static short SPECIAL = 0x01;		/* 特价 */	public final static short RECOMMEND = 0x02;		/* 推荐 */ 	public final static short SELL_OUT = 0x04;		/* 售完 */	public final static short GIFT = 0x08;			/* 赠送 */	public final static short CUR_PRICE = 0x10;		/* 时价 */	public final static short COMBO = 0x20;			/* 套菜 */	public final static short HOT = 0x40;			/* 热销 */	public final static short WEIGHT = 0x80;		/* 称重 */		int mStatus = 0;		public int getStatus(){		return mStatus;	}		public void setStatus(int status){		this.mStatus = status;	}		/**	 * Check to see whether the food is special.	 * @return true if the food is special, otherwise false	 */	public boolean isSpecial(){		return ((mStatus & SPECIAL) != 0);	}		/**	 * Set the food to special or not.	 * @param onOff 	 * 			the switch to set the food to special or not	 */	public void setSpecial(boolean onOff){		if(onOff){			mStatus |= SPECIAL;		}else{			mStatus &= ~SPECIAL;		}	}		/**	 * Check to see whether the food is recommended.	 * @return true if the food is recommended, other false	 */	public boolean isRecommend(){		return ((mStatus & RECOMMEND) != 0);	}		/**	 * Check to see whether the food is weigh.	 * @return true if the food is weigh, other false	 */	public boolean isWeigh(){		return ((mStatus & WEIGHT) != 0);	}		/**	 * Set the food to recommended or not.	 * @param onOff 	 * 			the switch to set the food to recommended or not	 */	public void setRecommend(boolean onOff){		if(onOff){			mStatus |= RECOMMEND;		}else{			mStatus &= ~RECOMMEND;		}	}		/**	 * Check to see whether the food is sell out.	 * @return true if the food is sell out, other false	 */	public boolean isSellOut(){		return ((mStatus & SELL_OUT) != 0);	}		/**	 * Set the food to sell out or not.	 * @param onOff 	 * 			the switch to set the food to sell out or not	 */	public void setSellOut(boolean onOff){		if(onOff){			mStatus |= SELL_OUT;		}else{			mStatus &= ~SELL_OUT;		}	}		/**	 * Check to see whether the food is gifted.	 * @return true if the food is gifted, other false	 */	public boolean isGift(){		return ((mStatus & GIFT) != 0);		}		/**	 * Set the food to gift or not.	 * @param onOff 	 * 			the switch to set the food to gift or not	 */	public void setGift(boolean onOff){		if(onOff){			mStatus |= GIFT;		}else{			mStatus &= ~GIFT;		}	}		/**	 * Check to see whether the food is current price.	 * @return true if the food is current price, other false	 */	public boolean isCurPrice(){		return ((mStatus & CUR_PRICE) != 0);	}		/**	 * Set the food to current price or not.	 * @param onOff 	 * 			the switch to set the food to current price or not	 */	public void setCurPrice(boolean onOff){		if(onOff){			mStatus |= CUR_PRICE;		}else{			mStatus &= ~CUR_PRICE;		}	}		/**	 * Check to see whether the food is combo.	 * @return true if the food is combo, other false	 */	public boolean isCombo(){		return ((mStatus & COMBO) != 0);	}		/**	 * Set the food to combo or not.	 * @param onOff 	 * 			the switch to set the food to combo or not	 */	public void setCombo(boolean onOff){		if(onOff){			mStatus |= COMBO;		}else{			mStatus &= ~COMBO;		}	}		/**	 * Set the food to weigh or not.	 * @param onOff 	 * 			the switch to set the food to weigh or not	 */	public void setWeigh(boolean onOff){		if(onOff){			mStatus |= WEIGHT;		}else{			mStatus &= ~WEIGHT;		}	}		/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isHot(){		return ((mStatus & HOT) != 0);	}		/**	 * Set the food to hot or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setHot(boolean onOff){		if(onOff){			mStatus |= HOT;		}else{			mStatus &= ~HOT;		}	}		public FoodStatistics getStatistics() {		return statistics;	}	public void setStatistics(FoodStatistics statistics) {		this.statistics = statistics;	}	public boolean equals(Object obj){		if(obj == null || !(obj instanceof Food)){			return false;		}else{			return mRestaurantID == ((Food)obj).mRestaurantID && mAliasId == ((Food)obj).mAliasId;		}	}		public int hashCode(){		return new Integer(mRestaurantID).hashCode() ^			   new Integer(mAliasId).hashCode();	}		public Food(){//		this.mKitchen = new Kitchen();	}		public Food(long foodId, int aliasId, int restaurantId){		this();		this.mFoodId = foodId;		this.mAliasId = aliasId;		this.mRestaurantID = restaurantId;	}		public Food(int aliasID, String name){		this();		this.mAliasId = aliasID;		this.mName = name.trim();	}		public Food(int aliasId, String name, Float price){		this(aliasId, name);		//split the price into 3-byte value		//get the float point from the price		setPrice(price);	}	public Food(int restaurantID, long foodID, int foodAlias, String name, Float price, FoodStatistics statistics,				int status, String pinyin, String pinyinShortcut, short tasteRefType, String desc, String image, Kitchen kitchen){		this.mAliasId = foodAlias;		this.mName = name.trim();		setPrice(price);		this.mRestaurantID = restaurantID;		this.mFoodId = foodID;		this.mStatus = status;		this.mPinyin = pinyin;		this.mPinyinShortcut = pinyinShortcut;		this.tasteRefType = tasteRefType;		this.desc = desc;		this.image = image;		this.mKitchen = kitchen;		this.statistics = statistics;	}	public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == FOOD_PARCELABLE_COMPLEX){			dest.writeShort(this.mAliasId);			dest.writeInt(this.mUnitPrice);			dest.writeParcel(this.mKitchen, Kitchen.KITCHEN_PARCELABLE_SIMPLE);			dest.writeShort(this.mStatus);			dest.writeParcel(this.statistics, (short)0);			dest.writeString(this.mName);			dest.writeString(this.image);			dest.writeParcelArray(this.mPopTastes, Taste.TASTE_PARCELABLE_SIMPLE);			dest.writeParcelArray(this.mChildFoods, Food.FOOD_PARCELABLE_SIMPLE);					}else if(flag == FOOD_PARCELABLE_SIMPLE){			dest.writeShort(this.mAliasId);		}			}	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == FOOD_PARCELABLE_COMPLEX){			this.mAliasId = source.readShort();			this.mUnitPrice = source.readInt();			this.mKitchen = (Kitchen)source.readParcel(Kitchen.KITCHEN_CREATOR);			this.mStatus = source.readShort();			this.statistics = (FoodStatistics)source.readParcel(FoodStatistics.FS_CREATOR);			this.mName = source.readString();			this.image = source.readString();						Parcelable[] popTastes = source.readParcelArray(Taste.TASTE_CREATOR);			if(popTastes != null){				mPopTastes = new Taste[popTastes.length];				for(int i = 0; i < popTastes.length; i++){					mPopTastes[i] = (Taste)popTastes[i];				}			}						Parcelable[] childFoods = source.readParcelArray(Food.FOOD_CREATOR);			if(childFoods != null){				mChildFoods = new Food[childFoods.length];				for(int i = 0; i < childFoods.length; i++){					mChildFoods[i] = (Food)childFoods[i];				}			}					}else if(flag == FOOD_PARCELABLE_SIMPLE){			this.mAliasId = source.readShort();		}	}	public final static Parcelable.Creator FOOD_CREATOR = new Parcelable.Creator(){		public Parcelable newInstance() {			return new Food();		}				public Parcelable[] newInstance(int size){			return new Food[size];		}			};}