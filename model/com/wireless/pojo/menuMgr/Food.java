package com.wireless.pojo.menuMgr;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.LinkedHashMap;import java.util.List;import java.util.Map;import com.wireless.json.Jsonable;import com.wireless.parcel.Parcel;import com.wireless.parcel.Parcelable;import com.wireless.pojo.tasteMgr.Taste;import com.wireless.pojo.util.NumericUtil;public class Food implements Parcelable, Comparable<Food>, Jsonable{		/**	 * The taste reference type as below	 * 1 - smart(智能关联)	 * 2 - manual(人工关联)	 */	public static enum TasteRef{		SMART(1, "智能关联"),		MANUAL(2, "人工关联");				private final int val;		private final String desc;				TasteRef(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){			return "taste reference(val = " + val + ",desc = " + desc + ")";		}				public static TasteRef valueOf(int val){			for(TasteRef ref : values()){				if(ref.val == val){					return ref;				}			}			throw new IllegalArgumentException("The taste reference type(val = " + val + ") is invalid.");		}				public int getVal(){			return val;		}				public String getDesc(){			return desc;		}	}		/**	 * The stock status as below.	 * 1 - None(无管理)	 * 2 - Good(商品出库)	 * 3 - Material(原料出库)	 */	public static enum StockStatus{		NONE(1, "无管理"),		GOOD(2, "商品出库"),		MATERIAL(3, "原料出库");				private final int val;		private final String desc;				StockStatus(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){			return "stock status(val = " + val + ",desc = " + desc + ")";		}				public static StockStatus valueOf(int val){			for(StockStatus status : values()){				if(status.val == val){					return status;				}			}			throw new IllegalArgumentException("The stock status(val = " + val + ") is invalid.");		}				public int getVal(){			return this.val;		}				public String getDesc(){			return this.desc;		}	}		//the status of the food as below	public static final short SPECIAL = 1 << 0;			/* 特价 */	public static final short RECOMMEND = 1 << 1;		/* 推荐 */ 	public static final short SELL_OUT = 1 << 2;		/* 售完 */	public static final short GIFT = 1 << 3;			/* 赠送 */	public static final short CUR_PRICE = 1 << 4;		/* 时价 */	public static final short COMBO = 1 << 5;			/* 套菜 */	public static final short HOT = 1 << 6;				/* 热销 */	public static final short WEIGHT = 1 << 7;			/* 称重 */	public static final short COMMISSION = 1 << 8;		/* 提成 */	public static final short TEMP = 1 << 9;			/* 临时 */		//the flag to different kinds of parcel	public static final byte FOOD_PARCELABLE_COMPLEX = 0;	public static final byte FOOD_PARCELABLE_SIMPLE = 1;		public static class ComboBuilder{		private final int parentId;		private final List<Food> childFoods = new ArrayList<Food>();				public ComboBuilder(int parentId){			this.parentId = parentId;		}				public ComboBuilder addChild(int childFoodId, int amount){			Food child = new Food(childFoodId);			child.setAmount(amount);			if(!childFoods.contains(child)){				childFoods.add(child);			}			return this;		}				public Food build(){			return new Food(this);		}	}		public static class UpdateBuilder{		private final int foodId;		private String name;		private float price = -1;		private Kitchen kitchen;		private int aliasId;		private String desc;		private int status;		private int statusChangedFlag;		private float commission;		private StockStatus stock;		private String image = IMG_MODIFIED_FLAG;		private final static String IMG_MODIFIED_FLAG = "img_modified_flag";				public UpdateBuilder(int foodId){			this.foodId = foodId;		}				public UpdateBuilder setAliasId(int aliasId){			this.aliasId = aliasId;			return this;		}				public boolean isAliasChanged(){			return this.aliasId != 0;		}				public UpdateBuilder setName(String name){			if(name.length() == 0){				throw new IllegalArgumentException("菜名不能为空");			}else{				this.name = name;			}			return this;		}				public boolean isNameChanged(){			return this.name != null;		}				public UpdateBuilder setKitchen(Kitchen kitchen){			this.kitchen = kitchen;			return this;		}				public boolean isKitchenChanged(){			return this.kitchen != null;		}				public UpdateBuilder setPrice(float price){			if(price < 0){				throw new IllegalArgumentException("价钱不能是负数");			}else{				this.price = price;			}			return this;		}				public boolean isPriceChanged(){			return this.price >= 0;		}				public UpdateBuilder setDesc(String desc){			this.desc = desc;			return this;		}				public boolean isDescChanged(){			return this.desc != null;		}				public UpdateBuilder setStockStatus(StockStatus stock){			this.stock = stock;			return this;		}				public boolean isStockChanged(){			return this.stock != null;		}				public UpdateBuilder setCurPrice(boolean onOff){			if(onOff){				status |= CUR_PRICE;			}else{				status &= ~CUR_PRICE;			}			statusChangedFlag |= CUR_PRICE;			return this;		}				public boolean isCurPrice(){			return (status & CUR_PRICE) != 0;		}				public boolean isCurPriceChanged(){			return (statusChangedFlag & CUR_PRICE) != 0;		}				public UpdateBuilder setGift(boolean onOff){			if(onOff){				status |= GIFT;			}else{				status &= ~GIFT;			}			statusChangedFlag |= GIFT;			return this;		}				public boolean isGift(){			return (status & GIFT) != 0;		}				public boolean isGiftChanged(){			return (statusChangedFlag & GIFT) != 0;		}				public UpdateBuilder setSellOut(boolean onOff){			if(onOff){				status |= SELL_OUT;			}else{				status &= ~SELL_OUT;			}			statusChangedFlag |= SELL_OUT;			return this;		}				public boolean isSellout(){			return (status & SELL_OUT) != 0;		}				public boolean isSelloutChanged(){			return (statusChangedFlag & SELL_OUT) != 0;		}				public UpdateBuilder setRecommend(boolean onOff){			if(onOff){				status |= RECOMMEND;			}else{				status &= ~RECOMMEND;			}			statusChangedFlag |= RECOMMEND;			return this;		}				public boolean isRecommend(){			return (status & RECOMMEND) != 0;		}				public boolean isRecommendChanged(){			return (statusChangedFlag & RECOMMEND) != 0;		}				public UpdateBuilder setSpecial(boolean onOff){			if(onOff){				status |= SPECIAL;			}else{				status &= ~SPECIAL;			}			statusChangedFlag |= SPECIAL;			return this;		}				public boolean isSpecial(){			return (status & SPECIAL) != 0;		}				public boolean isSpecialChanged(){			return (statusChangedFlag & SPECIAL) != 0;		}				public UpdateBuilder setHot(boolean onOff){			if(onOff){				status |= HOT;			}else{				status &= ~HOT;			}			statusChangedFlag |= HOT;			return this;		}				public boolean isHot(){			return (status & HOT) != 0;		}				public boolean isHotChanged(){			return (statusChangedFlag & HOT) != 0;		}				public UpdateBuilder setWeigh(boolean onOff){			if(onOff){				status |= WEIGHT;			}else{				status &= ~WEIGHT;			}			statusChangedFlag |= WEIGHT;			return this;		}				public boolean isWeight(){			return (status & WEIGHT) != 0;		}				public boolean isWeightChanged(){			return (statusChangedFlag & WEIGHT) != 0;		}				public UpdateBuilder setCommission(float commission){			status |= COMMISSION;			this.commission = commission;			statusChangedFlag |= COMMISSION;			return this;		}				public boolean isCommission(){			return (status & COMMISSION) != 0;		}				public boolean isCommissionChanged(){			return (statusChangedFlag & COMMISSION) != 0;		}				public UpdateBuilder setCombo(boolean onOff){			if(onOff){				status |= COMBO;			}else{				status &= ~COMBO;			}			statusChangedFlag |= COMBO;			return this;		}				public boolean isCombo(){			return (status & COMBO) != 0;		}				public boolean isComboChanged(){			return (statusChangedFlag & COMBO) != 0;		}				public UpdateBuilder setImage(String image){			this.image = image;			return this;		}				public boolean isImageChanged(){			return !IMG_MODIFIED_FLAG.equals(image);		}				public Food build(){			return new Food(this);		}	}		public static class InsertBuilder{		private final String name;		private final float price;		private final Kitchen kitchen;				private int aliasId;		private String desc;		private int status;		private float commission;		private StockStatus stock = StockStatus.NONE;				public InsertBuilder(String name, float price, Kitchen kitchen){			if(name.length() == 0){				throw new IllegalArgumentException("菜名不能为空");			}else{				this.name = name;			}			if(price < 0){				throw new IllegalArgumentException("价钱不能是负数");			}else{				this.price = price;			}			if(kitchen == null){				throw new IllegalArgumentException("厨房不能是空");			}else{				this.kitchen = kitchen;			}		}				public InsertBuilder setAliasId(int aliasId){			if(aliasId <= 0){				throw new IllegalArgumentException("菜谱编号必须大于0");			}			this.aliasId = aliasId;			return this;		}				public boolean isAliasChanged(){			return this.aliasId != 0;		}				public InsertBuilder setDesc(String desc){			this.desc = desc;			return this;		}				public InsertBuilder setStockStatus(StockStatus stock){			this.stock = stock;			return this;		}				public InsertBuilder setCurPrice(boolean onOff){			if(onOff){				status |= CUR_PRICE;			}else{				status &= ~CUR_PRICE;			}			return this;		}						public InsertBuilder setGift(boolean onOff){			if(onOff){				status |= GIFT;			}else{				status &= ~GIFT;			}			return this;		}				public InsertBuilder setSellOut(boolean onOff){			if(onOff){				status |= SELL_OUT;			}else{				status &= ~SELL_OUT;			}			return this;		}				public InsertBuilder setRecommend(boolean onOff){			if(onOff){				status |= RECOMMEND;			}else{				status &= ~RECOMMEND;			}			return this;		}				public InsertBuilder setSpecial(boolean onOff){			if(onOff){				status |= SPECIAL;			}else{				status &= ~SPECIAL;			}			return this;		}				public InsertBuilder setHot(boolean onOff){			if(onOff){				status |= HOT;			}else{				status &= ~HOT;			}			return this;		}				public InsertBuilder setWeigh(boolean onOff){			if(onOff){				status |= WEIGHT;			}else{				status &= ~WEIGHT;			}			return this;		}				public InsertBuilder setCommission(float commission){			status |= COMMISSION;			this.commission = commission;			return this;		}				public InsertBuilder setTemp(boolean onOff){			if(onOff){				status |= TEMP;			}else{				status &= ~TEMP;			}			return this;					}				public Food build(){			return new Food(this);		}	}		private int mFoodId;												//the food's id	private int mAliasId;												//the food's alias id	private int mRestaurantId;											//the restaurant id that the food belongs to	private int mStatus;												//the food status	private float mUnitPrice;											//the unit price of the food	private float mCommission;											//the commission to the food	private final Kitchen mKitchen = new Kitchen(0);					//the kitchen which the food belongs to	private String mName;												//the food's name		private String mPinyin;												//the simple Chinese pinyin to this food	private String mPinyinShortcut;										//the short cut to Chinese pinyin	private StockStatus mStockStatus = StockStatus.NONE;				//the stock status 	private String mDesc;												//the description to this food	private String mImage;												//the image file name	private TasteRef mTasteRefType = TasteRef.SMART;					//the type to taste reference	private final List<Taste> mPopTastes = new ArrayList<Taste>();		//the most popular taste to this food	private final List<Food> mAssociatedFoods = new ArrayList<Food>();	//the associated foods 	private int mAmount;												//the amount to this food, only available as the sub food to a combo	private final List<Food> mChildFoods = new ArrayList<Food>();		//the child foods, only available as the food belongs combo	public FoodStatistics statistics;									//the food statistics			public final static Comparator<Food> BY_SALES = new Comparator<Food>(){			@Override		public int compare(Food f1, Food f2) {			if(f1.statistics.getOrderCnt() > f2.statistics.getOrderCnt()){				return -1;			}else if(f1.statistics.getOrderCnt() < f2.statistics.getOrderCnt()){				return 1;			}else{				return 0;			}		}			};	public final static Comparator<Food> BY_KITCHEN = new Comparator<Food>(){			@Override		public int compare(Food f1, Food f2) {			if (f1.getKitchen().getDisplayId() > f2.getKitchen().getDisplayId()) {				return 1;			} else if (f1.getKitchen().getDisplayId() < f2.getKitchen().getDisplayId()) {				return -1;			} else {				return 0;			}		}			};		private Food(ComboBuilder builder){		setFoodId(builder.parentId);		for(Food childFood : builder.childFoods){			this.mChildFoods.add(childFood);		}	}		private Food(UpdateBuilder builder){		setFoodId(builder.foodId);		setName(builder.name);		setPrice(builder.price);		setKitchen(builder.kitchen);		setAliasId(builder.aliasId);		setDesc(builder.desc);		setStatus(builder.status);		setCommission(builder.commission);		setStockStatus(builder.stock);		setImage(builder.image);	}		private Food(InsertBuilder builder){		setName(builder.name);		setPrice(builder.price);		setKitchen(builder.kitchen);		setAliasId(builder.aliasId);		setDesc(builder.desc);		setStatus(builder.status);		setCommission(builder.commission);		setStockStatus(builder.stock);	}		public Food(int foodId){		this.mFoodId = foodId;	}		public Food(int foodId, int aliasId, int restaurantId){		this.mFoodId = foodId;		this.mAliasId = aliasId;		this.mRestaurantId = restaurantId;	}		//Copy constructor	public Food(Food src){		copyFrom(src);	}		public void copyFrom(Food src){		if(src != null){			this.mAliasId = src.mAliasId;			this.mName = src.mName;			this.mUnitPrice = src.mUnitPrice;			this.mCommission = src.mCommission;			this.mRestaurantId = src.mRestaurantId;			this.mFoodId = src.mFoodId;			this.mStatus = src.mStatus;			this.mPinyin = src.mPinyin;			this.mPinyinShortcut = src.mPinyinShortcut;			this.mTasteRefType = src.mTasteRefType;			this.mDesc = src.mDesc;			this.mImage = src.mImage;			this.mKitchen.copyFrom(src.mKitchen);			this.statistics = src.statistics;			setPopTastes(src.mPopTastes);			setChildFoods(src.mChildFoods);			this.mStockStatus = src.mStockStatus;		}	}		public void setTasteRefType(TasteRef refType){		this.mTasteRefType = refType;	}		public void setTasteRefType(int type){		this.mTasteRefType = TasteRef.valueOf(type);	}		public TasteRef getTasteRefType(){		return this.mTasteRefType;	}		public boolean isTasteSmartRef(){		return this.mTasteRefType == TasteRef.SMART;	}		public boolean isTasteManualRef(){		return this.mTasteRefType == TasteRef.MANUAL;	}		public boolean hasImage(){		return mImage != null ? mImage.length() != 0 : false;	}		public String getImage(){		if(mImage == null){			return "";		}		return mImage;	}		public void setImage(String image){		this.mImage = image;	}		public List<Taste> getPopTastes(){		return mPopTastes;	}		public void addPopTaste(Taste popTaste){		this.mPopTastes.add(popTaste);	}		public void setPopTastes(List<Taste> popTastes){		if(popTastes != null){			this.mPopTastes.clear();			this.mPopTastes.addAll(popTastes);		}	}		public boolean hasPopTastes(){		return !mPopTastes.isEmpty();	}		public List<Food> getChildFoods(){		return mChildFoods;	}		public void addChildFood(Food childFood){		if(childFood != null){			this.mChildFoods.add(childFood);		}	}		public void setChildFoods(List<Food> childFoods){		if(childFoods != null){			this.mChildFoods.clear();			this.mChildFoods.addAll(childFoods);		}	}		public boolean hasChildFoods(){		return !mChildFoods.isEmpty();	}		public int getAmount(){		return mAmount;	}		public void setAmount(int amount){		this.mAmount = amount;	}		public Kitchen getKitchen(){		return mKitchen;	}		public void setKitchen(Kitchen kitchen){		if(kitchen != null){			this.mKitchen.copyFrom(kitchen);		}	}		public int getRestaurantId(){		return mRestaurantId;	}		public void setRestaurantId(int restaurantId){		this.mRestaurantId = restaurantId;	}		public void setAssocatedFoods(List<Food> associatedFoods){		if(associatedFoods != null){			this.mAssociatedFoods.clear();			this.mAssociatedFoods.addAll(associatedFoods);		}	}		public List<Food> getAssociatedFoods(){		return Collections.unmodifiableList(mAssociatedFoods);	}		public boolean hasAssociatedFoods(){		return !mAssociatedFoods.isEmpty();	}		public int getFoodId(){		return this.mFoodId;	}		public void setFoodId(int foodId){		this.mFoodId = foodId;	}		public String getName(){		if(this.mName == null){			return "";		}		return this.mName;	}		public void setName(String name){		if(name != null){			this.mName = name;		}	}		public int getAliasId(){		return mAliasId;	}		public void setAliasId(int aliasId){		this.mAliasId = aliasId;	}		public String getPinyinShortcut(){		if(mPinyinShortcut == null){			return "";		}		return mPinyinShortcut;	}		public void setPinyinShortcut(String pinyinShortcut){		mPinyinShortcut = pinyinShortcut;	}		public String getPinyin(){		if(mPinyin == null){			return "";		}		return mPinyin;	}		public void setPinyin(String pinyin){		this.mPinyin = pinyin;	}		public StockStatus getStockStatus() {		return mStockStatus;	}	public void setStockStatus(StockStatus stockStatus) {		this.mStockStatus = stockStatus;	}	public void setStockStatus(int statusVal){		this.mStockStatus = StockStatus.valueOf(statusVal);	}		public void setCommission(float commission){		this.mCommission = commission;	}		public float getCommission(){		return this.mCommission;	}		public void setPrice(float price){		mUnitPrice = price;	}		public float getPrice(){		return NumericUtil.roundFloat(mUnitPrice);	}		public int getStatus(){		return mStatus;	}		public void setStatus(int status){		this.mStatus = status;	}		/**	 * Check to see whether the food is special.	 * @return true if the food is special, otherwise false	 */	public boolean isSpecial(){		return ((mStatus & SPECIAL) != 0);	}		/**	 * Set the food to special or not.	 * @param onOff 	 * 			the switch to set the food to special or not	 */	public void setSpecial(boolean onOff){		if(onOff){			mStatus |= SPECIAL;		}else{			mStatus &= ~SPECIAL;		}	}		/**	 * Check to see whether the food is recommended.	 * @return true if the food is recommended, other false	 */	public boolean isRecommend(){		return ((mStatus & RECOMMEND) != 0);	}		/**	 * Check to see whether the food is weigh.	 * @return true if the food is weigh, other false	 */	public boolean isWeigh(){		return ((mStatus & WEIGHT) != 0);	}		/**	 * Set the food to recommended or not.	 * @param onOff 	 * 			the switch to set the food to recommended or not	 */	public void setRecommend(boolean onOff){		if(onOff){			mStatus |= RECOMMEND;		}else{			mStatus &= ~RECOMMEND;		}	}		/**	 * Check to see whether the food is sell out.	 * @return true if the food is sell out, other false	 */	public boolean isSellOut(){		return ((mStatus & SELL_OUT) != 0);	}		/**	 * Set the food to sell out or not.	 * @param onOff 	 * 			the switch to set the food to sell out or not	 */	public void setSellOut(boolean onOff){		if(onOff){			mStatus |= SELL_OUT;		}else{			mStatus &= ~SELL_OUT;		}	}		/**	 * Check to see whether the food is gifted.	 * @return true if the food is gifted, other false	 */	public boolean isGift(){		return ((mStatus & GIFT) != 0);		}		/**	 * Set the food to gift or not.	 * @param onOff 	 * 			the switch to set the food to gift or not	 */	public void setGift(boolean onOff){		if(onOff){			mStatus |= GIFT;		}else{			mStatus &= ~GIFT;		}	}		/**	 * Check to see whether the food is current price.	 * @return true if the food is current price, other false	 */	public boolean isCurPrice(){		return ((mStatus & CUR_PRICE) != 0);	}		/**	 * Set the food to current price or not.	 * @param onOff 	 * 			the switch to set the food to current price or not	 */	public void setCurPrice(boolean onOff){		if(onOff){			mStatus |= CUR_PRICE;		}else{			mStatus &= ~CUR_PRICE;		}	}		/**	 * Check to see whether the food is combo.	 * @return true if the food is combo, other false	 */	public boolean isCombo(){		return ((mStatus & COMBO) != 0);	}		/**	 * Set the food to combo or not.	 * @param onOff 	 * 			the switch to set the food to combo or not	 */	public void setCombo(boolean onOff){		if(onOff){			mStatus |= COMBO;		}else{			mStatus &= ~COMBO;		}	}		/**	 * Set the food to weigh or not.	 * @param onOff 	 * 			the switch to set the food to weigh or not	 */	public void setWeigh(boolean onOff){		if(onOff){			mStatus |= WEIGHT;		}else{			mStatus &= ~WEIGHT;		}	}		/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isHot(){		return ((mStatus & HOT) != 0);	}		/**	 * Set the food to hot or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setHot(boolean onOff){		if(onOff){			mStatus |= HOT;		}else{			mStatus &= ~HOT;		}	}	/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isCommission(){		return ((mStatus & COMMISSION) != 0);	}	/**	 * Set the food to commission or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setCommission(boolean onOff){		if(onOff){			mStatus |= COMMISSION;		}else{			mStatus &= ~COMMISSION;		}	}		public boolean isTemp(){		return ((mStatus & TEMP) != 0);	}		public void setTemp(boolean onOff){		if(onOff){			mStatus |= TEMP;		}else{			mStatus &= ~TEMP;		}	}		public FoodStatistics getStatistics() {		return statistics;	}	public void setStatistics(FoodStatistics statistics) {		this.statistics = statistics;	}	public boolean hasDesc(){		return getDesc().trim().length() != 0;	}		public String getDesc() {		if(mDesc == null){			mDesc = "";		}		return mDesc;	}	public void setDesc(String desc) {		this.mDesc = desc;	}	@Override	public boolean equals(Object obj){		if(obj == null || !(obj instanceof Food)){			return false;		}else{			return mFoodId == ((Food)obj).mFoodId;		}	}		@Override	public int hashCode(){		return 17 * 31 + mFoodId;	}		@Override	public String toString(){		return getName() + "(food_id = " + getFoodId() + ", restaurant_id = " + getRestaurantId() + ")";	}		@Override	public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == FOOD_PARCELABLE_COMPLEX){			dest.writeInt(this.getFoodId());			dest.writeShort(this.getAliasId());			dest.writeFloat(this.getPrice());			dest.writeParcel(this.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);			dest.writeShort(this.getStatus());			dest.writeParcel(this.statistics, 0);			dest.writeString(this.getName());			dest.writeString(this.getPinyin());			dest.writeString(this.getPinyinShortcut());			dest.writeString(this.getImage());			dest.writeString(this.getDesc());			dest.writeParcelList(this.mPopTastes, Taste.TASTE_PARCELABLE_SIMPLE);			dest.writeParcelList(this.mChildFoods, Food.FOOD_PARCELABLE_SIMPLE);					}else if(flag == FOOD_PARCELABLE_SIMPLE){			dest.writeInt(this.getFoodId());			dest.writeShort(this.getAliasId());			dest.writeString(this.getName());		}			}	@Override	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == FOOD_PARCELABLE_COMPLEX){			this.setFoodId(source.readInt());			this.setAliasId(source.readShort());			this.setPrice(source.readFloat());			this.setKitchen(source.readParcel(Kitchen.CREATOR));			this.setStatus(source.readShort());			this.statistics = source.readParcel(FoodStatistics.CREATOR);			this.setName(source.readString());			this.setPinyin(source.readString());			this.setPinyinShortcut(source.readString());			this.setImage(source.readString());			this.setDesc(source.readString());			this.setPopTastes(source.readParcelList(Taste.CREATOR));			this.setChildFoods(source.readParcelList(Food.CREATOR));					}else if(flag == FOOD_PARCELABLE_SIMPLE){			this.setFoodId(source.readInt());			this.setAliasId(source.readShort());			this.setName(source.readString());		}	}	public final static Parcelable.Creator<Food> CREATOR = new Parcelable.Creator<Food>(){		@Override		public Food newInstance() {			return new Food(0);		}				@Override		public Food[] newInstance(int size){			return new Food[size];		}			};	@Override	public int compareTo(Food o) {		if(mFoodId > o.mFoodId){			return 1;		}else if(mFoodId < o.mFoodId){			return -1;		}else{			return 0;		}	}		protected Map<String, Object> toJsonMap(){		Map<String, Object> jm = new LinkedHashMap<String, Object>();		jm.put("id", this.mFoodId);		jm.put("alias", this.mAliasId == 0 ? "----" : this.mAliasId);		jm.put("name", this.mName);		jm.put("amount", this.mAmount);		jm.put("unitPrice", this.mUnitPrice);		jm.put("commission", this.mCommission);		jm.put("restaurantId", this.mRestaurantId);		jm.put("pinyin", this.mPinyinShortcut);		jm.put("desc", this.mDesc);		jm.put("img", this.mImage);		jm.put("status", this.mStatus);		jm.put("stockStatusValue", this.mStockStatus.getVal());		jm.put("stockStatusText", this.mStockStatus.getDesc());		jm.put("tasteRefType", this.mTasteRefType.getVal());		if(this.mKitchen != null)			jm.put("kitchen", this.mKitchen.toJsonMap(0));		return jm;	}		@Override	public Map<String, Object> toJsonMap(int flag) {		return Collections.unmodifiableMap(toJsonMap());	}	@Override	public List<Object> toJsonList(int flag) {		return null;	}}