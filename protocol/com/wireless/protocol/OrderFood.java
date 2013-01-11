package com.wireless.protocol;

import com.wireless.excep.BusinessException;

public class OrderFood extends Food {
	public long orderDate;
	public String waiter;
	//public int payManner = Order.MANNER_CASH;
	
	//the hang status to the food
	public short hangStatus = FOOD_NORMAL;			
	public static final int FOOD_NORMAL = 0;		/* 普通 */
	public static final int FOOD_HANG_UP = 1;		/* 叫起 */
	public static final int FOOD_IMMEDIATE = 2;		/* 即起 */
	
	//the taste group to this order food
	TasteGroup mTasteGroup;							
	
	//the cancel reason to this order food
	CancelReason mCancelReason;
	
	//the table this order food belongs to
	//public Table table = new Table();				
	
	//indicates whether the food is temporary
	public boolean isTemporary = false;				
	
	//indicates whether the food is repaid.
	boolean isRepaid = false;
	
	public void setRepaid(boolean isRepaid){
		this.isRepaid = isRepaid; 
	}
	
	public boolean isRepaid(){
		return isRepaid;
	}
	
	//the discount to this food represent as integer
	private int mDiscount = 100;	 
	
	/**
	 * Set the discount for internal.
	 * @param discount the discount to set
	 */
	void setDiscountInternal(int discount){
		//The discount remains as before in case of gift or special
		if(isGift() || isSpecial()){
			//this.discount = 100;
		}else{
			this.mDiscount = discount;
		}
	}
	
	/**
	 * Get the discount used for internal.
	 * @return the discount represented as integer
	 */
	int getDiscountInternal(){
		return mDiscount;
	}
	
	/**
	 * Set the discount to this order food.
	 * @param discount the discount to set
	 */
	public void setDiscount(Float discount){
		setDiscountInternal(Util.float2Int(discount));
	}
	
	/**
	 * Get the discount to this order food.
	 * @return the discount to this order food
	 */
	public Float getDiscount(){
		return Util.int2Float(getDiscountInternal());
	}

	final static int MAX_ORDER_AMOUNT = 255 * 100;

	//the current order amount to this order food
	private int mCurCnt;		
	
	//the last order amount to this order food
	private int mLastCnt;	

	
	/**
	 * Add the order amount to order food.
	 * @param countToAdd the count to add
	 * @throws BusinessException
	 * 			throws if the count to add exceeds {@link MAX_ORDER_AMOUNT}
	 */
	public void addCount(Float countToAdd) throws BusinessException{
		if(countToAdd.floatValue() >= 0){
			addCountInternal(Util.float2Int(countToAdd));
		}else{
			throw new IllegalArgumentException("The count(" + countToAdd.floatValue() + ") to add should be positive.");
		}
	}
	
	void addCountInternal(int countToAdd) throws BusinessException{
		if(countToAdd >= 0){
			int amount = mCurCnt + countToAdd; 
			if(amount <= MAX_ORDER_AMOUNT){
				mLastCnt = mCurCnt;
				mCurCnt = amount;
			}else{
				throw new BusinessException("对不起，\"" + mName + "\"每次最多只能点" + MAX_ORDER_AMOUNT / 100 + "份");
			}
		}else{
			throw new IllegalArgumentException("The count(" + countToAdd / 100 + ") to add should be positive.");			
		}
	}
	
	/**
	 * Remove the order amount to order food.
	 * @param countToRemove 
	 * 			the count to remove
	 * @throws BusinessException
	 * 			throws if the count to remove is greater than original count
	 */
	public void removeCount(Float countToRemove) throws BusinessException{
		if(countToRemove.floatValue() >= 0){
			removeCountInternal(Util.float2Int(countToRemove));
		}else{
			throw new IllegalArgumentException("The count(" + countToRemove.floatValue() + ") to remove should be positive.");
		}
	}
	
	/**
	 * Remove the order amount to order food for internal.
	 * @param countToRemove 
	 * 			the count to remove
	 * @throws BusinessException
	 * 			throws if the count to remove is greater than original count
	 */
	void removeCountInternal(int countToRemove) throws BusinessException{
		if(countToRemove >= 0){
			if(countToRemove <= getCountInternal()){
				mLastCnt = mCurCnt;
				mCurCnt -= countToRemove;
			}else{
				throw new BusinessException("输入的删除数量大于已点数量, 请重新输入");
			}
		}else{
			throw new IllegalArgumentException("The count(" + countToRemove / 100 + ") to remove should be positive.");
		}
	}
	
	/**
	 * Get the delta to order count.
	 * @return the offset to order count
	 */
	public Float getDelta(){
		return new Float((float)getDeltaInternal() / 100);
	}
	
	/**
	 * Get the delta(used for internal) to this order food
	 * @return the offset to order count
	 */
	int getDeltaInternal(){
		return mLastCnt - mCurCnt;
	}
	
	/**
	 * Set the current count and reset the offset to zero.
	 * The current count would set to {@link MAX_ORDER_AMOUNT} if the parameter exceeds it.
	 * @param count
	 * 			the order amount to set
	 */
	public void setCount(Float count){
		setCountInternal(Util.float2Int(count));			
	}
	
	/**
	 * Set the current count(used for internal) and reset the offset to zero.
	 * The current count would set to {@link MAX_ORDER_AMOUNT} if the parameter exceeds it.
	 * @param count
	 * 			the order amount to set represent as integer
	 */
	void setCountInternal(int count){
		mLastCnt = mCurCnt;
		mCurCnt = (count <= MAX_ORDER_AMOUNT ? count : MAX_ORDER_AMOUNT);			
	}
	
	/**
	 * Get the current count to this order food.
	 * @return the current count to this order food
	 */
	public Float getCount(){
		return Util.int2Float(getCountInternal());
	}
	
	/**
	 * Get the current count(used for internal) to this order food.
	 * @return the current count represented as integer to this order food
	 */
	int getCountInternal(){
		return mCurCnt;
	}
	
	/**
	 * Get the original count to this order food.
	 * @return the original count to this order food
	 */
	public Float getOriCount(){
		return Util.int2Float(getLastCountInternal());
	}
	
	/**
	 * Get the last count(used for internal) to this order food.
	 * @return the original count represented as integer to this order food
	 */
	int getLastCountInternal(){
		return mLastCnt;
	}
	
	/**
	 * Indicates the food is hurried
	 */
	public boolean isHurried = false;
	
	/**
	 * Comparing two foods without the tastes
	 * @param food
	 * @return
	 */
	public boolean equalsIgnoreTaste(OrderFood food){
		if(isTemporary != food.isTemporary){
			return false;
		}else if(isTemporary && food.isTemporary){
			return mName.equals(food.mName) && (mUnitPrice == food.mUnitPrice);
		}else{
			return mAliasId == food.mAliasId && hangStatus == food.hangStatus;
		}
	}
	
	/**
	 * Comparing two foods without the hang status.
	 * @param food
	 * @return
	 */
	public boolean equalsIgnoreHangStauts(OrderFood food){
		if(isTemporary != food.isTemporary){
			return false;
		}else if(isTemporary && food.isTemporary){
			return mName.equals(food.mName) && (mUnitPrice == food.mUnitPrice);
		}else{
			return restaurantID == food.restaurantID && 
				   mAliasId == food.mAliasId && 
				   equalsByTasteGroup(food);
		}
	}
	
	/**
	 * Check to see whether the taste group to two foods is the same.
	 * @param food the food to compared
	 * @return true if the taste group is the same, otherwise false
	 */
	boolean equalsByTasteGroup(OrderFood food){
		if(hasTaste() && food.hasTaste()){
			return mTasteGroup.equals(food.mTasteGroup);
			
		}else if(!hasTaste() && !food.hasTaste()){
			return true;
			
		}else{
			return false;
		}
	}
	
	/**
	 * There are three ways to determine whether two foods is the same as each other.
	 * 1 - If one food is temporary while the other NOT, means they are NOT the same.
	 * 2 - If both of foods are temporary, check to see whether their names and price are the same.
	 *     They are the same if both name and price is matched.
	 * 3 - If both of foods are NOT temporary, check to see their food, all tastes id and hang status.
	 *     They are the same if all of the things above are matched.
	 */
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof OrderFood)){
			return false;
			
		}else{
			OrderFood food = (OrderFood)obj;
			if(isTemporary != food.isTemporary){
				return false;
				
			}else if(hangStatus != food.hangStatus){
				return false;
				
			}else{
				return equalsIgnoreHangStauts(food);
			}
			
		}
	}

	/**
	 * Generate the hash code according to the equals method.
	 */
	public int hashCode(){
		if(isTemporary){
			return mName.hashCode() ^ mUnitPrice ^ hangStatus;
		}else{
			return new Integer(mAliasId).hashCode() ^ 
				   (mTasteGroup != null ? mTasteGroup.hashCode() : 0)^
				   new Short(hangStatus).hashCode();
		}
	}
	
	/**
	 * Check to see if the order food has taste(either normal taste or temporary taste).
	 * @return true if the order food has taste, otherwise false
	 */
	public boolean hasTaste(){
		return mTasteGroup == null ? false : mTasteGroup.hasTaste();
	}
	
	/**
	 * Check to see if the order food has normal taste.
	 * @return true if the order food has normal taste, otherwise false.
	 */
	public boolean hasNormalTaste(){
		return mTasteGroup == null ? false : mTasteGroup.hasNormalTaste();
	}
	
	/**
	 * Check to see if the order food has temporary taste.
	 * @return true if the order food has temporary taste, otherwise false
	 */
	public boolean hasTmpTaste(){
		return mTasteGroup == null ? false : mTasteGroup.hasTmpTaste();
	}
	
	/**
	 * The unit price with taste before discount to a specific food.
	 * @return The unit price represented as integer.
	 */
//	int getUnitPriceBeforeDiscountInternal(){
//		return (mUnitPrice + (mTasteGroup == null ? 0 : mTasteGroup.getTastePriceInternal()));
//		return mUnitPrice + (!hasTaste() || isWeigh() ? 0 : mTasteGroup.getTastePriceInternal());
//	}
	
	/**
	 * Calculate the price with taste before discount to a specific food.
	 * @return The price represented as integer.
	 */
	int calcPriceBeforeDiscountInternal(){
		if(isWeigh()){
			return getUnitPriceWithTasteInternal() * getCountInternal() / 100 + (hasTaste() ? mTasteGroup.getTastePriceInternal() : 0);			
		}else{
			return getUnitPriceWithTasteInternal() * getCountInternal() / 100;
		}
	}
	
	/**
	 * Calculate the price with taste before discount to a specific food.
	 * @return The price represented as float.
	 */	
	Float calcPriceBeforeDiscount(){
		return Util.int2Float(calcPriceBeforeDiscountInternal());
	}
	
	/**
	 * The unit price with taste to a specific food is as below.
	 * <p>unit_price = (food_price + taste_price + tmp_taste_price) * discount</p>
	 * If taste price is calculated by rate, then
	 * taste_price = food_price * taste_rate
	 * @return the unit price represented as integer
	 */
	int getUnitPriceWithTasteInternal(){
		return mUnitPrice + (!hasTaste() || isWeigh() ? 0 : mTasteGroup.getTastePriceInternal());
	}	
	
	/**
	 * The unit price with taste to a specific food is as below.
	 * unit_price = food_price * discount + taste_price + tmp_taste_price
	 * If taste price is calculated by rate, then
	 * taste_price = food_price * taste_rate
	 * @return the unit price represented as a Float
	 */
	public Float getUnitPriceWithTaste(){
		return Util.int2Float(getUnitPriceWithTasteInternal());
	}
	
	/**
	 * Calculate the pure total price to this food without taste as below.
	 * <br>price = food_price * discount * count 
	 * @return the total price to this food
	 */
//	public Float calcPurePrice(){
//		return Util.int2Float((mUnitPrice * mDiscount * getCountInternal()) / 10000);
//	}	

	/**
	 * Calculate the total price to this food along with taste as below<br>.
	 * price = ((food_price + taste_price) * discount) * count 
	 * @return the total price to this food represented as integer
	 */
	int calcPriceWithTasteInternal(){
		if(isWeigh()){
			return (getUnitPriceWithTasteInternal() * getCountInternal() / 100 + (hasTaste() ? mTasteGroup.getTastePriceInternal() : 0)) * mDiscount / 100;			
		}else{
			return getUnitPriceWithTasteInternal() * getCountInternal() / 100 * mDiscount / 100;	
		}
	}
	
	/**
	 * Calculate the total price to this food along with taste as below<br>.
	 * price = ((food_price + taste_price) * discount) * count 
	 * @return the total price to this food represented as float
	 */
	public Float calcPriceWithTaste(){
		return Util.int2Float(calcPriceWithTasteInternal());
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * price = unit_price * (1 - discount)
	 * @return the discount price to this food represented as an integer
	 */
	int calcDiscountPriceInternal(){
		if(mDiscount != 100){
			return (mUnitPrice + (mTasteGroup == null ? 0 : mTasteGroup.getTastePriceInternal())) * getCountInternal() * (100 - mDiscount) / 10000;
		}else{
			return 0;
		}
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * price = unit_price * (1 - discount)
	 * @return the discount price to this food represented as an float
	 */
	public Float calcDiscountPrice(){
		return Util.int2Float(calcDiscountPriceInternal());
	}	
	
	/**
	 * Override the same method to super.
	 * Get the alias id according to name and price in case of temporary,
	 * otherwise return its own alias.
	 * @return the alias id to this order food
	 */
	public int getAliasId(){
		if(isTemporary){
			return Math.abs((mName.hashCode() + mUnitPrice) % 65535);
		}else{
			return this.mAliasId;
		}
	}
	
	public OrderFood(){

	}

	public OrderFood(Food food){
		super(food.restaurantID,
			  food.mFoodId,
			  food.mAliasId,
			  food.mName,
			  food.getPrice(),
			  food.statistics,
			  food.mStatus,
			  food.mPinyin,
			  food.getPinyinShortcut(),
			  food.tasteRefType,
			  food.desc,
			  food.image,
			  food.kitchen);
		popTastes = food.popTastes;
		childFoods = food.childFoods;
	}

	public OrderFood(OrderFood src){
		this((Food)src);
		this.orderDate = src.orderDate;
		this.waiter = src.waiter;
		this.hangStatus = src.hangStatus;
		this.isTemporary = src.isTemporary;
		this.mDiscount = src.mDiscount;
		this.mLastCnt = src.mLastCnt;
		this.mCurCnt = src.mCurCnt;
		this.isHurried = src.isHurried;
		this.isRepaid = src.isRepaid;
		//this.payManner = src.payManner;
//		if(src.table != null){
//			this.table = new Table(src.table);
//		}
		mTasteGroup = src.mTasteGroup;
	}
	
	public TasteGroup makeTasteGroup(){
		mTasteGroup = new TasteGroup(this, null, null);
		return mTasteGroup;
	}
	
	public TasteGroup makeTasetGroup(Taste[] normal, Taste tmp){
		mTasteGroup = new TasteGroup(this, normal, tmp);
		return mTasteGroup;
	}
	
	public TasteGroup makeTasteGroup(int groupID, Taste normal, Taste tmp){
		mTasteGroup = new TasteGroup(groupID, normal, tmp);
		return mTasteGroup;
	}
	
	public void clearTasetGroup(){
		mTasteGroup = null;
	}
	
	public TasteGroup getTasteGroup(){
		return mTasteGroup;
	}
	
	public void setTasteGroup(TasteGroup tg){
		if(tg != null){
			mTasteGroup = tg;
			mTasteGroup.setAttachedFood(this);
		}
	}
	
	public void setCancelReason(CancelReason cancelReason){
		this.mCancelReason = cancelReason;
	}
	
	public CancelReason getCancelReason(){
		return mCancelReason;
	}
	
	public boolean hasCancelReason(){
		return mCancelReason == null ? false : mCancelReason.hasReason();
	}
	
	/**
	 * Return the order food string.
	 * The string format is as below.
	 * name-taste1,taste2,taste3
	 */
	public String toString(){
		return mName + (hasTaste() ? ("-" + mTasteGroup.getTastePref()) : "");
	}
}
