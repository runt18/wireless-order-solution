package com.wireless.protocol;import java.util.ArrayList;import java.util.Collections;import java.util.LinkedHashMap;import java.util.List;import java.util.Map;import com.wireless.json.Jsonable;import com.wireless.pojo.menuMgr.Kitchen;import com.wireless.pojo.tasteMgr.Taste;import com.wireless.pojo.util.NumericUtil;import com.wireless.protocol.parcel.Parcel;import com.wireless.protocol.parcel.Parcelable;public class Food implements Parcelable, Comparable<Food>, Jsonable{		/**	 * The taste reference type as below	 * 1 - smart(智能关联)	 * 2 - manual(人工关联)	 */	public static enum TasteRef{		SMART(1, "智能关联"),		MANUAL(2, "人工关联");				private final int val;		private final String desc;				TasteRef(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){			return "taste reference(val = " + val + ",desc = " + desc + ")";		}				public static TasteRef valueOf(int val){			for(TasteRef ref : values()){				if(ref.val == val){					return ref;				}			}			throw new IllegalArgumentException("The taste reference type(val = " + val + ") is invalid.");		}				public int getVal(){			return val;		}				public String getDesc(){			return desc;		}	}		/**	 * The stock status as below.	 * 1 - None(无管理)	 * 2 - Good(商品出库)	 * 3 - Material(原料出库)	 */	public static enum StockStatus{		NONE(1, "无管理"),		GOOD(2, "商品出库"),		MATERIAL(3, "原料出库");				private final int val;		private final String desc;				StockStatus(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){			return "stock status(val = " + val + ",desc = " + desc + ")";		}				public static StockStatus valueOf(int val){			for(StockStatus status : values()){				if(status.val == val){					return status;				}			}			throw new IllegalArgumentException("The stock status(val = " + val + ") is invalid.");		}				public int getVal(){			return this.val;		}				public String getDesc(){			return this.desc;		}	}		//the status of the food as below	public static final short SPECIAL = 1 << 0;			/* 特价 */	public static final short RECOMMEND = 1 << 1;		/* 推荐 */ 	public static final short SELL_OUT = 1 << 2;		/* 售完 */	public static final short GIFT = 1 << 3;			/* 赠送 */	public static final short CUR_PRICE = 1 << 4;		/* 时价 */	public static final short COMBO = 1 << 5;			/* 套菜 */	public static final short HOT = 1 << 6;				/* 热销 */	public static final short WEIGHT = 1 << 7;			/* 称重 */		//the flag to different kinds of parcel	public static final byte FOOD_PARCELABLE_COMPLEX = 0;	public static final byte FOOD_PARCELABLE_SIMPLE = 1;		private long mFoodId;												//the food's id	private int mAliasId;												//the food's alias id	private int mRestaurantId;											//the restaurant id that the food belongs to	private int mStatus;												//the food status	private float mUnitPrice;											//the unit price of the food	private Kitchen mKitchen;											//the kitchen which the food belongs to	private String mName;												//the food's name		private String mPinyin;												//the simple Chinese pinyin to this food	private String mPinyinShortcut;										//the short cut to Chinese pinyin	private StockStatus stockStatus = StockStatus.NONE;			//the stock status 	private String desc;										//the description to this food	private String image;										//the image file name	private TasteRef mTasteRefType = TasteRef.SMART;			//the type to taste reference	private List<Taste> mPopTastes = new ArrayList<Taste>();	//the most popular taste to this food	private List<Food> mAssociatedFoods = new ArrayList<Food>();//the associated foods 	private int mAmount;										//the amount to this food, only available as the sub food to a combo	private List<Food> mChildFoods = new ArrayList<Food>();		//the child foods, only available as the food belongs combo	public FoodStatistics statistics;							//the food statistics					public Food(){			}		public Food(long foodId, int aliasId, int restaurantId){		this();		this.mFoodId = foodId;		this.mAliasId = aliasId;		this.mRestaurantId = restaurantId;	}		public Food(int restaurantId, long foodId, int foodAlias, 				String name, float price, FoodStatistics statistics,				int status, 				String pinyin, String pinyinShortcut, 				short tasteRefType, 				String desc, String image, Kitchen kitchen,StockStatus stockStatus)	{				copyFrom(restaurantId, foodId, foodAlias, 				 name, price, statistics,				 status,				 pinyin, pinyinShortcut, 				 tasteRefType,				 desc, image, kitchen,				 null, null, stockStatus);	}	//Copy constructor	public Food(Food src){		copyFrom(src);	}		public void copyFrom(Food src){		if(src != null){			copyFrom(src.mRestaurantId, src.mFoodId, src.mAliasId,					 src.mName, src.mUnitPrice, src.statistics,					 src.mStatus, src.mPinyin, src.mPinyinShortcut,					 src.mTasteRefType.getVal(), 					 src.getDesc(), src.getImage(), src.mKitchen,					 src.mPopTastes, src.mChildFoods, src.getStockStatus());		}	}		private void copyFrom(int restaurantID, long foodID, int foodAlias, 						  String name, float unitPrice, FoodStatistics statistics,						  int status, String pinyin, String pinyinShortcut, 						  int tasteRefType,						  String desc, String image, Kitchen kitchen,						  List<Taste> popTastes, List<Food> childFoods, StockStatus stockStatus){		this.mAliasId = foodAlias;		this.mName = name;		this.mUnitPrice = unitPrice;		this.mRestaurantId = restaurantID;		this.mFoodId = foodID;		this.mStatus = status;		this.mPinyin = pinyin;		this.mPinyinShortcut = pinyinShortcut;		this.mTasteRefType = TasteRef.valueOf(tasteRefType);		this.setDesc(desc);		this.setImage(image);		this.mKitchen = kitchen;		this.statistics = statistics;		this.mPopTastes = popTastes;		this.mChildFoods = childFoods;		this.stockStatus = stockStatus;	}		public void setTasteRefType(TasteRef refType){		this.mTasteRefType = refType;	}		public void setTasteRefType(int type){		this.mTasteRefType = TasteRef.valueOf(type);	}		public TasteRef getTasteRefType(){		return this.mTasteRefType;	}		public boolean isTasteSmartRef(){		return this.mTasteRefType == TasteRef.SMART;	}		public boolean isTasteManualRef(){		return this.mTasteRefType == TasteRef.MANUAL;	}		public boolean hasImage(){		return image != null;	}		public String getImage(){		return image;	}		public void setImage(String image){		this.image = image;	}		public List<Taste> getPopTastes(){		return mPopTastes;	}		public void addPopTaste(Taste popTaste){		this.mPopTastes.add(popTaste);	}	public void setPopTastes(List<Taste> popTastes){		if(popTastes != null){			this.mPopTastes = popTastes;		}	}		public boolean hasPopTastes(){		return !mPopTastes.isEmpty();	}		public List<Food> getChildFoods(){		return mChildFoods;	}		public void setChildFoods(List<Food> childFoods){		if(childFoods != null){			this.mChildFoods = childFoods;		}	}		public boolean hasChildFoods(){		return !mChildFoods.isEmpty();	}		public int getAmount(){		return mAmount;	}		public void setAmount(int amount){		this.mAmount = amount;	}		public Kitchen getKitchen(){		if(mKitchen == null){			setKitchen(new Kitchen());		}		return mKitchen;	}		public void setKitchen(Kitchen kitchen){		this.mKitchen = kitchen;	}		public int getRestaurantId(){		return mRestaurantId;	}		public void setRestaurantId(int restaurantId){		this.mRestaurantId = restaurantId;	}		public void setAssocatedFoods(List<Food> associatedFoods){		if(associatedFoods != null){			this.mAssociatedFoods = associatedFoods;		}	}		public List<Food> getAssociatedFoods(){		return mAssociatedFoods;	}		public boolean hasAssociatedFoods(){		return !mAssociatedFoods.isEmpty();	}		public long getFoodId(){		return this.mFoodId;	}		public void setFoodId(long foodId){		this.mFoodId = foodId;	}		public String getName(){		if(this.mName == null){			this.mName = "";		}		return this.mName;	}		public void setName(String name){		this.mName = name;	}		public int getAliasId(){		return mAliasId;	}		public void setAliasId(int aliasId){		this.mAliasId = aliasId;	}		public String getPinyinShortcut(){		if(mPinyinShortcut == null){			mPinyinShortcut = "";		}		return mPinyinShortcut;	}		public void setPinyinShortcut(String pinyinShortcut){		mPinyinShortcut = pinyinShortcut;	}		public String getPinyin(){		if(mPinyin == null){			mPinyin = "";		}		return mPinyin;	}		public void setPinyin(String pinyin){		this.mPinyin = pinyin;	}		public StockStatus getStockStatus() {		return stockStatus;	}	public void setStockStatus(StockStatus stockStatus) {		this.stockStatus = stockStatus;	}	public void setStockStatus(int statusVal){		this.stockStatus = StockStatus.valueOf(statusVal);	}		public void setPrice(float price){		mUnitPrice = price;	}		public float getPrice(){		return NumericUtil.roundFloat(mUnitPrice);	}		public int getStatus(){		return mStatus;	}		public void setStatus(int status){		this.mStatus = status;	}		/**	 * Check to see whether the food is special.	 * @return true if the food is special, otherwise false	 */	public boolean isSpecial(){		return ((mStatus & SPECIAL) != 0);	}		/**	 * Set the food to special or not.	 * @param onOff 	 * 			the switch to set the food to special or not	 */	public void setSpecial(boolean onOff){		if(onOff){			mStatus |= SPECIAL;		}else{			mStatus &= ~SPECIAL;		}	}		/**	 * Check to see whether the food is recommended.	 * @return true if the food is recommended, other false	 */	public boolean isRecommend(){		return ((mStatus & RECOMMEND) != 0);	}		/**	 * Check to see whether the food is weigh.	 * @return true if the food is weigh, other false	 */	public boolean isWeigh(){		return ((mStatus & WEIGHT) != 0);	}		/**	 * Set the food to recommended or not.	 * @param onOff 	 * 			the switch to set the food to recommended or not	 */	public void setRecommend(boolean onOff){		if(onOff){			mStatus |= RECOMMEND;		}else{			mStatus &= ~RECOMMEND;		}	}		/**	 * Check to see whether the food is sell out.	 * @return true if the food is sell out, other false	 */	public boolean isSellOut(){		return ((mStatus & SELL_OUT) != 0);	}		/**	 * Set the food to sell out or not.	 * @param onOff 	 * 			the switch to set the food to sell out or not	 */	public void setSellOut(boolean onOff){		if(onOff){			mStatus |= SELL_OUT;		}else{			mStatus &= ~SELL_OUT;		}	}		/**	 * Check to see whether the food is gifted.	 * @return true if the food is gifted, other false	 */	public boolean isGift(){		return ((mStatus & GIFT) != 0);		}		/**	 * Set the food to gift or not.	 * @param onOff 	 * 			the switch to set the food to gift or not	 */	public void setGift(boolean onOff){		if(onOff){			mStatus |= GIFT;		}else{			mStatus &= ~GIFT;		}	}		/**	 * Check to see whether the food is current price.	 * @return true if the food is current price, other false	 */	public boolean isCurPrice(){		return ((mStatus & CUR_PRICE) != 0);	}		/**	 * Set the food to current price or not.	 * @param onOff 	 * 			the switch to set the food to current price or not	 */	public void setCurPrice(boolean onOff){		if(onOff){			mStatus |= CUR_PRICE;		}else{			mStatus &= ~CUR_PRICE;		}	}		/**	 * Check to see whether the food is combo.	 * @return true if the food is combo, other false	 */	public boolean isCombo(){		return ((mStatus & COMBO) != 0);	}		/**	 * Set the food to combo or not.	 * @param onOff 	 * 			the switch to set the food to combo or not	 */	public void setCombo(boolean onOff){		if(onOff){			mStatus |= COMBO;		}else{			mStatus &= ~COMBO;		}	}		/**	 * Set the food to weigh or not.	 * @param onOff 	 * 			the switch to set the food to weigh or not	 */	public void setWeigh(boolean onOff){		if(onOff){			mStatus |= WEIGHT;		}else{			mStatus &= ~WEIGHT;		}	}		/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isHot(){		return ((mStatus & HOT) != 0);	}		/**	 * Set the food to hot or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setHot(boolean onOff){		if(onOff){			mStatus |= HOT;		}else{			mStatus &= ~HOT;		}	}		public FoodStatistics getStatistics() {		return statistics;	}	public void setStatistics(FoodStatistics statistics) {		this.statistics = statistics;	}	public String getDesc() {		if(desc == null){			desc = "";		}		return desc;	}	public void setDesc(String desc) {		this.desc = desc;	}	@Override	public boolean equals(Object obj){		if(obj == null || !(obj instanceof Food)){			return false;		}else{			return mRestaurantId == ((Food)obj).mRestaurantId && mAliasId == ((Food)obj).mAliasId;		}	}		@Override	public int hashCode(){		int result = 17;		result = result * 31 + mRestaurantId;		result = result * 31 + mAliasId;		return result;	}		@Override	public String toString(){		return getName() + "(alias_id = " + getAliasId() + ", restaurant_id = " + getRestaurantId() + ")";	}		@Override	public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == FOOD_PARCELABLE_COMPLEX){			dest.writeShort(this.mAliasId);			dest.writeFloat(this.mUnitPrice);			dest.writeParcel(this.mKitchen, Kitchen.KITCHEN_PARCELABLE_SIMPLE);			dest.writeShort(this.mStatus);			dest.writeParcel(this.statistics, 0);			dest.writeString(this.mName);			dest.writeString(this.getImage());			dest.writeParcelList(this.mPopTastes, Taste.TASTE_PARCELABLE_SIMPLE);			dest.writeParcelList(this.mChildFoods, Food.FOOD_PARCELABLE_SIMPLE);					}else if(flag == FOOD_PARCELABLE_SIMPLE){			dest.writeShort(this.mAliasId);		}			}	@Override	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == FOOD_PARCELABLE_COMPLEX){			this.mAliasId = source.readShort();			this.mUnitPrice = source.readFloat();			this.mKitchen = source.readParcel(Kitchen.KITCHEN_CREATOR);			this.mStatus = source.readShort();			this.statistics = source.readParcel(FoodStatistics.FS_CREATOR);			this.mName = source.readString();			this.setImage(source.readString());						this.mPopTastes = source.readParcelList(Taste.TASTE_CREATOR);						this.mChildFoods = source.readParcelList(Food.FOOD_CREATOR);					}else if(flag == FOOD_PARCELABLE_SIMPLE){			this.mAliasId = source.readShort();		}	}	public final static Parcelable.Creator<Food> FOOD_CREATOR = new Parcelable.Creator<Food>(){		public Food newInstance() {			return new Food();		}				public Food[] newInstance(int size){			return new Food[size];		}			};	@Override	public int compareTo(Food o) {		if(mAliasId > o.mAliasId){			return 1;		}else if(mAliasId < o.mAliasId){			return -1;		}else{			return 0;		}	}	@Override	public Map<String, Object> toJsonMap(int flag) {		Map<String, Object> jm = new LinkedHashMap<String, Object>();		jm.put("id", this.mFoodId);		jm.put("alias", this.mAliasId);		jm.put("name", this.mName);		jm.put("unitPrice", this.mUnitPrice);		jm.put("restaurantId", this.mRestaurantId);		jm.put("pinyin", this.mPinyin);		jm.put("desc", this.desc);		jm.put("img", this.image);		jm.put("status", this.mStatus);		jm.put("stockStatusValue", this.stockStatus.getVal());		jm.put("stockStatusText", this.stockStatus.getDesc());		if(this.mKitchen != null)			jm.put("kitchen", this.mKitchen.toJsonMap(0));				jm.put("isCombo", this.isCombo());		jm.put("isCurPrice", this.isCurPrice());		jm.put("isGift", this.isGift());		jm.put("isHot", this.isHot());		jm.put("isRecommend", this.isRecommend());		jm.put("isStop", this.isSellOut());		jm.put("isSpecial", this.isSpecial());		jm.put("isWeigh", this.isWeigh());				return Collections.unmodifiableMap(jm);	}	@Override	public List<Object> toJsonList(int flag) {		return null;	}}