package com.wireless.pojo.menuMgr;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.HashMap;import java.util.List;import java.util.Map;import com.wireless.json.JsonMap;import com.wireless.json.Jsonable;import com.wireless.parcel.Parcel;import com.wireless.parcel.Parcelable;import com.wireless.pojo.oss.OssImage;import com.wireless.pojo.tasteMgr.Taste;import com.wireless.pojo.util.NumericUtil;public class Food implements Parcelable, Comparable<Food>, Jsonable{		//the status of the food as below	public static final short SPECIAL = 1 << 0;			/* 特价 */	public static final short RECOMMEND = 1 << 1;		/* 推荐 */ 	public static final short SELL_OUT = 1 << 2;		/* 售完 */	public static final short GIFT = 1 << 3;			/* 赠送 */	public static final short CUR_PRICE = 1 << 4;		/* 时价 */	public static final short COMBO = 1 << 5;			/* 套菜 */	public static final short HOT = 1 << 6;				/* 热销 */	public static final short WEIGHT = 1 << 7;			/* 称重 */	public static final short COMMISSION = 1 << 8;		/* 提成 */	public static final short TEMP = 1 << 9;			/* 临时 */		//the flag to different kinds of parcel	public static final byte FOOD_PARCELABLE_COMPLEX = 0;	public static final byte FOOD_PARCELABLE_SIMPLE = 1;		public static class ComboBuilder{		private final int parentId;		private final List<ComboFood> childFoods = new ArrayList<ComboFood>();				public ComboBuilder(int parentId){			this.parentId = parentId;		}				public ComboBuilder addChild(int childFoodId, int amount){			ComboFood comboFood = new ComboFood(new Food(childFoodId), amount);			if(!childFoods.contains(comboFood)){				childFoods.add(comboFood);			}			return this;		}				public Food build(){			return new Food(this);		}	}		public static class UpdateBuilder{				private static final OssImage EMPTY_FLAG = new OssImage(0);				private final int foodId;		private String name;		private float price = -1;		private Kitchen kitchen;		private int aliasId = -1;		private String desc;		private int status;		private int statusChangedFlag;		private float commission;		private OssImage image = EMPTY_FLAG;		private final Map<PricePlan, Float> plan = new HashMap<PricePlan, Float>();		private List<FoodUnit> units = new ArrayList<FoodUnit>();				public UpdateBuilder(int foodId){			this.foodId = foodId;		}				public UpdateBuilder addPrice(int planId, float price){			plan.put(new PricePlan(planId), price);			return this;		}				public UpdateBuilder addPrice(PricePlan pricePlan, float price){			plan.put(pricePlan, price);			return this;		}				public boolean isPricePlanChanged(){ 			return !this.plan.isEmpty();		}				public UpdateBuilder emptyUnit(){			units = null;			return this;		}				public UpdateBuilder addUnit(float price, String unit){			if(units == null){				units = new ArrayList<FoodUnit>();			}			units.add(new FoodUnit(price, unit));			return this;		}				public boolean isFoodUnitChanged(){			if(units == null){				return true;			}else{				return !this.units.isEmpty();			}		}				public UpdateBuilder setAliasId(int aliasId){			this.aliasId = aliasId;			return this;		}				public boolean isAliasChanged(){			return this.aliasId != -1;		}				public UpdateBuilder setName(String name){			if(name.length() == 0){				throw new IllegalArgumentException("菜名不能为空");			}else{				this.name = name;			}			return this;		}				public boolean isNameChanged(){			return this.name != null;		}				public UpdateBuilder setKitchen(Kitchen kitchen){			this.kitchen = kitchen;			return this;		}				public boolean isKitchenChanged(){			return this.kitchen != null;		}				public UpdateBuilder setPrice(float price){			if(price < 0){				throw new IllegalArgumentException("价钱不能是负数");			}else{				this.price = price;			}			return this;		}				public boolean isPriceChanged(){			return this.price >= 0;		}				public UpdateBuilder setDesc(String desc){			this.desc = desc;			return this;		}				public boolean isDescChanged(){			return this.desc != null;		}				public UpdateBuilder setCurPrice(boolean onOff){			if(onOff){				status |= CUR_PRICE;			}else{				status &= ~CUR_PRICE;			}			statusChangedFlag |= CUR_PRICE;			return this;		}				public boolean isCurPrice(){			return (status & CUR_PRICE) != 0;		}				public boolean isCurPriceChanged(){			return (statusChangedFlag & CUR_PRICE) != 0;		}				public UpdateBuilder setGift(boolean onOff){			if(onOff){				status |= GIFT;			}else{				status &= ~GIFT;			}			statusChangedFlag |= GIFT;			return this;		}				public boolean isGift(){			return (status & GIFT) != 0;		}				public boolean isGiftChanged(){			return (statusChangedFlag & GIFT) != 0;		}				public UpdateBuilder setSellOut(boolean onOff){			if(onOff){				status |= SELL_OUT;			}else{				status &= ~SELL_OUT;			}			statusChangedFlag |= SELL_OUT;			return this;		}				public boolean isSellout(){			return (status & SELL_OUT) != 0;		}				public boolean isSelloutChanged(){			return (statusChangedFlag & SELL_OUT) != 0;		}				public UpdateBuilder setRecommend(boolean onOff){			if(onOff){				status |= RECOMMEND;			}else{				status &= ~RECOMMEND;			}			statusChangedFlag |= RECOMMEND;			return this;		}				public boolean isRecommend(){			return (status & RECOMMEND) != 0;		}				public boolean isRecommendChanged(){			return (statusChangedFlag & RECOMMEND) != 0;		}				public UpdateBuilder setSpecial(boolean onOff){			if(onOff){				status |= SPECIAL;			}else{				status &= ~SPECIAL;			}			statusChangedFlag |= SPECIAL;			return this;		}				public boolean isSpecial(){			return (status & SPECIAL) != 0;		}				public boolean isSpecialChanged(){			return (statusChangedFlag & SPECIAL) != 0;		}				public UpdateBuilder setHot(boolean onOff){			if(onOff){				status |= HOT;			}else{				status &= ~HOT;			}			statusChangedFlag |= HOT;			return this;		}				public boolean isHot(){			return (status & HOT) != 0;		}				public boolean isHotChanged(){			return (statusChangedFlag & HOT) != 0;		}				public UpdateBuilder setWeigh(boolean onOff){			if(onOff){				status |= WEIGHT;			}else{				status &= ~WEIGHT;			}			statusChangedFlag |= WEIGHT;			return this;		}				public boolean isWeight(){			return (status & WEIGHT) != 0;		}				public boolean isWeightChanged(){			return (statusChangedFlag & WEIGHT) != 0;		}				public UpdateBuilder setCommission(float commission){			status |= COMMISSION;			this.commission = commission;			statusChangedFlag |= COMMISSION;			return this;		}				public UpdateBuilder setCommission(boolean onOff){			if(onOff){				status |= COMMISSION;			}else{				status &= ~COMMISSION;			}			statusChangedFlag |= COMMISSION;			return this;		}				public boolean isCommission(){			return (status & COMMISSION) != 0;		}				public boolean isCommissionChanged(){			return (statusChangedFlag & COMMISSION) != 0;		}				public UpdateBuilder setCombo(boolean onOff){			if(onOff){				status |= COMBO;			}else{				status &= ~COMBO;			}			statusChangedFlag |= COMBO;			return this;		}				public boolean isCombo(){			return (status & COMBO) != 0;		}				public boolean isComboChanged(){			return (statusChangedFlag & COMBO) != 0;		}				public UpdateBuilder setImage(int ossImageId){			this.image = new OssImage(ossImageId);			return this;		}				public UpdateBuilder setImage(OssImage image){			this.image = image;			return this;		}				public boolean isImageChanged(){			return this.image != EMPTY_FLAG;		}				public Food build(){			return new Food(this);		}	}		public static class InsertBuilder{		private final String name;		private final float price;		private final Kitchen kitchen;				private int aliasId;		private String desc;		private int status;		private float commission;		private OssImage image;		private final Map<PricePlan, Float> plan = new HashMap<PricePlan, Float>();		private final List<FoodUnit> units = new ArrayList<FoodUnit>();		public InsertBuilder(String name, float price, Kitchen kitchen){			if(name.length() == 0){				throw new IllegalArgumentException("菜名不能为空");			}else{				this.name = name;			}			if(price < 0){				throw new IllegalArgumentException("价钱不能是负数");			}else{				this.price = price;			}			if(kitchen == null){				throw new IllegalArgumentException("厨房不能是空");			}else{				this.kitchen = kitchen;			}		}				public InsertBuilder addPrice(int planId, float price){			plan.put(new PricePlan(planId), price);			return this;		}				public InsertBuilder addPrice(PricePlan pricePlan, float price){			plan.put(pricePlan, price);			return this;		}				public InsertBuilder addUnit(float price, String unit){			this.units.add(new FoodUnit(price, unit));			return this;		}				public InsertBuilder setAliasId(int aliasId){			if(aliasId <= 0){				throw new IllegalArgumentException("菜谱编号必须大于0");			}			this.aliasId = aliasId;			return this;		}				public boolean isAliasChanged(){			return this.aliasId != 0;		}				public InsertBuilder setDesc(String desc){			this.desc = desc;			return this;		}				public InsertBuilder setCurPrice(boolean onOff){			if(onOff){				status |= CUR_PRICE;			}else{				status &= ~CUR_PRICE;			}			return this;		}						public InsertBuilder setGift(boolean onOff){			if(onOff){				status |= GIFT;			}else{				status &= ~GIFT;			}			return this;		}				public InsertBuilder setSellOut(boolean onOff){			if(onOff){				status |= SELL_OUT;			}else{				status &= ~SELL_OUT;			}			return this;		}				public InsertBuilder setRecommend(boolean onOff){			if(onOff){				status |= RECOMMEND;			}else{				status &= ~RECOMMEND;			}			return this;		}				public InsertBuilder setSpecial(boolean onOff){			if(onOff){				status |= SPECIAL;			}else{				status &= ~SPECIAL;			}			return this;		}				public InsertBuilder setHot(boolean onOff){			if(onOff){				status |= HOT;			}else{				status &= ~HOT;			}			return this;		}				public InsertBuilder setWeigh(boolean onOff){			if(onOff){				status |= WEIGHT;			}else{				status &= ~WEIGHT;			}			return this;		}				public InsertBuilder setCommission(float commission){			status |= COMMISSION;			this.commission = commission;			return this;		}				public InsertBuilder setTemp(boolean onOff){			if(onOff){				status |= TEMP;			}else{				status &= ~TEMP;			}			return this;					}				public InsertBuilder setImage(int ossImageId){			this.image = new OssImage(ossImageId);			return this;		}				public InsertBuilder setImage(OssImage image){			this.image = image;			return this;		}				public Food build(){			return new Food(this);		}	}		private int mFoodId;																//the food's id	private int mAliasId;																//the food's alias id	private int mRestaurantId;															//the restaurant id that the food belongs to	private int mStatus;																//the food status	private float mUnitPrice;															//the unit price of the food	private float mCommission;															//the commission to the food	private final Kitchen mKitchen = new Kitchen(0);									//the kitchen which the food belongs to	private String mName;																//the food's name		private String mPinyin;																//the simple Chinese pinyin to this food	private String mPinyinShortcut;														//the short cut to Chinese pinyin	private String mDesc;																//the description to this food	private OssImage mImage;															//the oss image	private final List<Taste> mPopTastes = new ArrayList<Taste>();						//the most popular taste to this food	private final List<Food> mAssociatedFoods = new ArrayList<Food>();					//the associated foods 	private final List<ComboFood> mChildFoods = new ArrayList<ComboFood>();				//the child foods, only available as the food belongs combo	private final Map<PricePlan, Float> mPricePlan = new HashMap<PricePlan, Float>();	//the price plan	private final List<FoodUnit> mFoodUnits = new ArrayList<FoodUnit>();				//the food units	private FoodStatistics statistics;													//the food statistics		public final static Comparator<Food> BY_ALIAS = new Comparator<Food>(){		@Override		public int compare(Food f1, Food f2) {			if(f1.mAliasId > 0 && f2.mAliasId > 0){				if(f1.mAliasId < f2.mAliasId){					return -1;				}else if(f1.mAliasId > f2.mAliasId){					return 1;				}else{					return 0;				}							}else if(f1.mAliasId == 0 && f2.mAliasId == 0){				if(f1.mFoodId > f2.mFoodId){					return 1;				}else if(f1.mFoodId < f2.mFoodId){					return -1;				}else{					return 0;				}							}else if(f1.mAliasId == 0 && f2.mAliasId > 0){				return 1;							}else if(f1.mAliasId > 0 && f2.mAliasId == 0){				return -1;							}else{				return 0;			}		}	};		public final static Comparator<Food> BY_SALES = new Comparator<Food>(){			@Override		public int compare(Food f1, Food f2) {			if(f1.statistics.getOrderCnt() > f2.statistics.getOrderCnt()){				return -1;			}else if(f1.statistics.getOrderCnt() < f2.statistics.getOrderCnt()){				return 1;			}else{				return 0;			}		}			};	public final static Comparator<Food> BY_KITCHEN = new Comparator<Food>(){			@Override		public int compare(Food f1, Food f2) {			if (f1.getKitchen().getDisplayId() > f2.getKitchen().getDisplayId()) {				return 1;			} else if (f1.getKitchen().getDisplayId() < f2.getKitchen().getDisplayId()) {				return -1;			} else {				return f1.compareTo(f2);			}		}			};		private Food(ComboBuilder builder){		setFoodId(builder.parentId);		for(ComboFood childFood : builder.childFoods){			this.mChildFoods.add(childFood);		}	}		private Food(UpdateBuilder builder){		setFoodId(builder.foodId);		setName(builder.name);		setPrice(builder.price);		setKitchen(builder.kitchen);		setAliasId(builder.aliasId);		setDesc(builder.desc);		setStatus(builder.status);		setCommission(builder.commission);		setImage(builder.image);		setPricePlan(builder.plan);		if(builder.units != null){			setFoodUnits(builder.units);		}	}		private Food(InsertBuilder builder){		setName(builder.name);		setPrice(builder.price);		setKitchen(builder.kitchen);		setAliasId(builder.aliasId);		setDesc(builder.desc);		setStatus(builder.status);		setCommission(builder.commission);		setImage(builder.image);		setPricePlan(builder.plan);		setFoodUnits(builder.units);	}		public Food(int foodId){		this.mFoodId = foodId;	}		public Food(int foodId, int aliasId, int restaurantId){		this.mFoodId = foodId;		this.mAliasId = aliasId;		this.mRestaurantId = restaurantId;	}		//Copy constructor	public Food(Food src){		copyFrom(src);	}		public void copyFrom(Food src){		if(src != null){			this.mAliasId = src.mAliasId;			this.mName = src.mName;			this.mUnitPrice = src.mUnitPrice;			this.mCommission = src.mCommission;			this.mRestaurantId = src.mRestaurantId;			this.mFoodId = src.mFoodId;			this.mStatus = src.mStatus;			this.mPinyin = src.mPinyin;			this.mPinyinShortcut = src.mPinyinShortcut;			this.mDesc = src.mDesc;			this.mImage = src.mImage;			this.mKitchen.copyFrom(src.mKitchen);			this.statistics = src.statistics;			setPopTastes(src.mPopTastes);			setChildFoods(src.mChildFoods);			setPricePlan(src.mPricePlan);		}	}		public boolean hasImage(){		return mImage != null;	}		public OssImage getImage(){		return mImage;	}		public void setImage(OssImage image){		this.mImage = image;	}		public List<Taste> getPopTastes(){		return mPopTastes;	}		public void addPopTaste(Taste popTaste){		this.mPopTastes.add(popTaste);	}		public void setPopTastes(List<Taste> popTastes){		if(popTastes != null){			this.mPopTastes.clear();			this.mPopTastes.addAll(popTastes);		}	}		public boolean hasPopTastes(){		return !mPopTastes.isEmpty();	}		public List<ComboFood> getChildFoods(){		return Collections.unmodifiableList(mChildFoods);	}		public void addChildFood(ComboFood childFood){		if(childFood != null){			this.mChildFoods.add(childFood);		}	}		public void setChildFoods(List<ComboFood> childFoods){		if(childFoods != null){			this.mChildFoods.clear();			this.mChildFoods.addAll(childFoods);		}	}		public boolean hasChildFoods(){		return !mChildFoods.isEmpty();	}		public Kitchen getKitchen(){		return mKitchen;	}		public void setKitchen(Kitchen kitchen){		if(kitchen != null){			this.mKitchen.copyFrom(kitchen);		}	}		public int getRestaurantId(){		return mRestaurantId;	}		public void setRestaurantId(int restaurantId){		this.mRestaurantId = restaurantId;	}		public void setAssocatedFoods(List<Food> associatedFoods){		if(associatedFoods != null){			this.mAssociatedFoods.clear();			this.mAssociatedFoods.addAll(associatedFoods);		}	}		public List<Food> getAssociatedFoods(){		return Collections.unmodifiableList(mAssociatedFoods);	}		public boolean hasAssociatedFoods(){		return !mAssociatedFoods.isEmpty();	}		public int getFoodId(){		return this.mFoodId;	}		public void setFoodId(int foodId){		this.mFoodId = foodId;	}		public String getName(){		if(this.mName == null){			return "";		}		return this.mName;	}		public void setName(String name){		this.mName = name;	}		public int getAliasId(){		return mAliasId;	}		public void setAliasId(int aliasId){		this.mAliasId = aliasId;	}		public String getPinyinShortcut(){		if(mPinyinShortcut == null){			return "";		}		return mPinyinShortcut;	}		public void setPinyinShortcut(String pinyinShortcut){		mPinyinShortcut = pinyinShortcut;	}		public String getPinyin(){		if(mPinyin == null){			return "";		}		return mPinyin;	}		public void setPinyin(String pinyin){		this.mPinyin = pinyin;	}		public void setCommission(float commission){		this.mCommission = commission;	}		public float getCommission(){		return this.mCommission;	}		public void setPrice(float price){		mUnitPrice = price;	}		public float getPrice(){		return NumericUtil.roundFloat(mUnitPrice);	}		public float getPrice(PricePlan plan, FoodUnit unit){		if(plan != null){			Float price = mPricePlan.get(plan);			if(price != null){				return NumericUtil.roundFloat(price.floatValue());			}else{				return NumericUtil.roundFloat(mUnitPrice);			}		}else if(unit != null){			int index = mFoodUnits.indexOf(unit);			if(index >= 0){				return mFoodUnits.get(index).getPrice();			}else{				return getPrice();			}		}else{			return getPrice();		}	}		public void addPricePlan(PricePlan plan, float price){		mPricePlan.put(plan, price);	}		public void setPricePlan(Map<PricePlan, Float> pricePlan){		if(pricePlan != null){			mPricePlan.clear();			mPricePlan.putAll(pricePlan);		}	}		public Map<PricePlan, Float> getPricePlan(){		return Collections.unmodifiableMap(mPricePlan);	}		public void setFoodUnits(List<FoodUnit> foodUnits){		this.mFoodUnits.clear();		this.mFoodUnits.addAll(foodUnits);	}		public void addFoodUnit(FoodUnit foodUnit){		if(!mFoodUnits.contains(foodUnit)){			mFoodUnits.add(foodUnit);		}	}		public boolean hasFoodUnit(){		return !this.mFoodUnits.isEmpty();	}		public List<FoodUnit> getFoodUnits(){		return Collections.unmodifiableList(this.mFoodUnits);	}		public int getStatus(){		return mStatus;	}		public void setStatus(int status){		this.mStatus = status;	}		/**	 * Check to see whether the food is special.	 * @return true if the food is special, otherwise false	 */	public boolean isSpecial(){		return ((mStatus & SPECIAL) != 0);	}		/**	 * Set the food to special or not.	 * @param onOff 	 * 			the switch to set the food to special or not	 */	public void setSpecial(boolean onOff){		if(onOff){			mStatus |= SPECIAL;		}else{			mStatus &= ~SPECIAL;		}	}		/**	 * Check to see whether the food is recommended.	 * @return true if the food is recommended, other false	 */	public boolean isRecommend(){		return ((mStatus & RECOMMEND) != 0);	}		/**	 * Check to see whether the food is weigh.	 * @return true if the food is weigh, other false	 */	public boolean isWeigh(){		return ((mStatus & WEIGHT) != 0);	}		/**	 * Set the food to recommended or not.	 * @param onOff 	 * 			the switch to set the food to recommended or not	 */	public void setRecommend(boolean onOff){		if(onOff){			mStatus |= RECOMMEND;		}else{			mStatus &= ~RECOMMEND;		}	}		/**	 * Check to see whether the food is sell out.	 * @return true if the food is sell out, other false	 */	public boolean isSellOut(){		return ((mStatus & SELL_OUT) != 0);	}		/**	 * Set the food to sell out or not.	 * @param onOff 	 * 			the switch to set the food to sell out or not	 */	public void setSellOut(boolean onOff){		if(onOff){			mStatus |= SELL_OUT;		}else{			mStatus &= ~SELL_OUT;		}	}		/**	 * Check to see whether the food is gifted.	 * @return true if the food is gifted, other false	 */	public boolean isGift(){		return ((mStatus & GIFT) != 0);		}		/**	 * Set the food to gift or not.	 * @param onOff 	 * 			the switch to set the food to gift or not	 */	public void setGift(boolean onOff){		if(onOff){			mStatus |= GIFT;		}else{			mStatus &= ~GIFT;		}	}		/**	 * Check to see whether the food is current price.	 * @return true if the food is current price, other false	 */	public boolean isCurPrice(){		return ((mStatus & CUR_PRICE) != 0);	}		/**	 * Set the food to current price or not.	 * @param onOff 	 * 			the switch to set the food to current price or not	 */	public void setCurPrice(boolean onOff){		if(onOff){			mStatus |= CUR_PRICE;		}else{			mStatus &= ~CUR_PRICE;		}	}		/**	 * Check to see whether the food is combo.	 * @return true if the food is combo, other false	 */	public boolean isCombo(){		return ((mStatus & COMBO) != 0);	}		/**	 * Set the food to combo or not.	 * @param onOff 	 * 			the switch to set the food to combo or not	 */	public void setCombo(boolean onOff){		if(onOff){			mStatus |= COMBO;		}else{			mStatus &= ~COMBO;		}	}		/**	 * Set the food to weigh or not.	 * @param onOff 	 * 			the switch to set the food to weigh or not	 */	public void setWeigh(boolean onOff){		if(onOff){			mStatus |= WEIGHT;		}else{			mStatus &= ~WEIGHT;		}	}		/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isHot(){		return ((mStatus & HOT) != 0);	}		/**	 * Set the food to hot or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setHot(boolean onOff){		if(onOff){			mStatus |= HOT;		}else{			mStatus &= ~HOT;		}	}	/**	 * Check to see whether the food is hot.	 * @return true if the food is hot, other false	 */	public boolean isCommission(){		return ((mStatus & COMMISSION) != 0);	}	/**	 * Set the food to commission or not.	 * @param onOff 	 * 			the switch to set the food to hot or not	 */	public void setCommission(boolean onOff){		if(onOff){			mStatus |= COMMISSION;		}else{			mStatus &= ~COMMISSION;		}	}		public boolean isTemp(){		return ((mStatus & TEMP) != 0);	}		public void setTemp(boolean onOff){		if(onOff){			mStatus |= TEMP;		}else{			mStatus &= ~TEMP;		}	}		public FoodStatistics getStatistics() {		return statistics;	}	public void setStatistics(FoodStatistics statistics) {		this.statistics = statistics;	}	public boolean hasDesc(){		return getDesc().trim().length() != 0;	}		public String getDesc() {		if(mDesc == null){			mDesc = "";		}		return mDesc;	}	public void setDesc(String desc) {		this.mDesc = desc;	}	@Override	public boolean equals(Object obj){		if(obj == null || !(obj instanceof Food)){			return false;		}else{			return mFoodId == ((Food)obj).mFoodId;		}	}		@Override	public int hashCode(){		return 17 * 31 + mFoodId;	}		@Override	public String toString(){		return getName() + "(food_id = " + getFoodId() + ", restaurant_id = " + getRestaurantId() + ")";	}		@Override	public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == FOOD_PARCELABLE_COMPLEX){			dest.writeInt(this.getFoodId());			dest.writeShort(this.getAliasId());			dest.writeFloat(this.getPrice());			dest.writeParcel(this.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);			dest.writeShort(this.getStatus());			dest.writeParcel(this.getStatistics(), 0);			dest.writeString(this.getName());			dest.writeString(this.getPinyin());			dest.writeString(this.getPinyinShortcut());			dest.writeParcel(this.getImage(), 0);			dest.writeString(this.getDesc());			dest.writeParcelList(this.mPopTastes, Taste.TASTE_PARCELABLE_SIMPLE);			dest.writeParcelList(this.mChildFoods, ComboFood.COMBO_FOOD_PARCELABLE_COMPLEX);			dest.writeParcelList(this.mFoodUnits, 0);					}else if(flag == FOOD_PARCELABLE_SIMPLE){			dest.writeInt(this.getFoodId());			dest.writeShort(this.getAliasId());			dest.writeString(this.getName());		}	}	@Override	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == FOOD_PARCELABLE_COMPLEX){			this.setFoodId(source.readInt());			this.setAliasId(source.readShort());			this.setPrice(source.readFloat());			this.setKitchen(source.readParcel(Kitchen.CREATOR));			this.setStatus(source.readShort());			this.setStatistics(source.readParcel(FoodStatistics.CREATOR));			this.setName(source.readString());			this.setPinyin(source.readString());			this.setPinyinShortcut(source.readString());			this.setImage(source.readParcel(OssImage.CREATOR));			this.setDesc(source.readString());			this.setPopTastes(source.readParcelList(Taste.CREATOR));			this.setChildFoods(source.readParcelList(ComboFood.CREATOR));			this.setFoodUnits(source.readParcelList(FoodUnit.CREATOR));					}else if(flag == FOOD_PARCELABLE_SIMPLE){			this.setFoodId(source.readInt());			this.setAliasId(source.readShort());			this.setName(source.readString());		}	}	public final static Parcelable.Creator<Food> CREATOR = new Parcelable.Creator<Food>(){		@Override		public Food newInstance() {			return new Food(0);		}				@Override		public Food[] newInstance(int size){			return new Food[size];		}			};	@Override	public int compareTo(Food f) {		if(mFoodId > f.mFoodId){			return 1;		}else if(mFoodId < f.mFoodId){			return -1;		}else{			return 0;		}	}		public static enum Key4Json{		FOOD_ID("id", "菜品Id"),		FOOD_ALIAS("alias", "助记码"),		FOOD_NAME("name", "名称"),		COMBO_AMOUNT("amount", "套菜(子菜)数量"),		FOOD_PRICE("unitPrice", "单价"),		COMMISSION("commission", "提成价"),		RESTAURANT_ID("restaurantId", "餐厅编号"),		FOOD_PINYIN("pinyin", "拼音"),		FOOD_DESC("desc", "描述"),		FOOD_IMG("img", "图片名"),		FOOD_STATUS("status", "菜品状态"),		//STOCK_STATUS("stockStatusValue", "库存状态"),		//STOCK_STATUS_TEXT("stockStatusText", "库存状态描述"),		ASSOCIATED_KITCHEN("kitchen", "所属厨房");				public final String key;		public final String desc;				Key4Json(String key, String desc){			this.key = key;			this.desc = desc;		}				@Override		public String toString(){			return "key = " + key + ",desc = " + desc;		}	}		public final static int FOOD_JSONABLE_COMPLEX = 0;	public final static int FOOD_JSONABLE_SIMPLE = 1;		@Override	public JsonMap toJsonMap(int flag) {		JsonMap jm = new JsonMap();		jm.putInt(Key4Json.FOOD_ID.key, this.mFoodId);		jm.putString(Key4Json.FOOD_ALIAS.key, this.mAliasId == 0 ? "----" : Integer.toString(this.mAliasId));		jm.putString(Key4Json.FOOD_NAME.key, this.mName);		//jm.putInt(Key4Json.COMBO_AMOUNT.key, this.mAmount);		jm.putFloat(Key4Json.FOOD_PRICE.key, this.mUnitPrice);		jm.putFloat(Key4Json.COMMISSION.key, this.mCommission);		jm.putInt(Key4Json.RESTAURANT_ID.key, this.mRestaurantId);		jm.putString(Key4Json.FOOD_PINYIN.key, this.mPinyinShortcut);		jm.putString(Key4Json.FOOD_DESC.key, this.mDesc);		jm.putInt("foodCnt", this.getStatistics() != null?this.getStatistics().getOrderCnt():0);		//jm.putString(Key4Json.FOOD_IMG.key, this.mImage);		jm.putJsonable(Key4Json.FOOD_IMG.key, this.mImage, 0);		jm.putInt(Key4Json.FOOD_STATUS.key, this.mStatus);		//jm.putInt(Key4Json.STOCK_STATUS.key, this.mStockStatus.getVal());		//jm.putString(Key4Json.STOCK_STATUS_TEXT.key, this.mStockStatus.getDesc());		if(flag == FOOD_JSONABLE_SIMPLE){			jm.putJsonable(Key4Json.ASSOCIATED_KITCHEN.key, this.mKitchen, Kitchen.KITCHEN_JSONABLE_SIMPLE);		}else{			jm.putJsonable(Key4Json.ASSOCIATED_KITCHEN.key, this.mKitchen, Kitchen.KITCHEN_JSONABLE_COMPLEX);		}				return jm;	}	@Override	public void fromJsonMap(JsonMap jsonMap, int flag) {			}}