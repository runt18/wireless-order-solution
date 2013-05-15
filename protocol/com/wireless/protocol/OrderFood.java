package com.wireless.protocol;

import java.util.List;

import com.wireless.excep.ProtocolException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class OrderFood extends Food {
	
	public final static byte OF_PARCELABLE_4_COMMIT = 0;
	public final static byte OF_PARCELABLE_4_QUERY = 1;
	
	//the order id associated with this order food
	int mOrderId;
	
	//the order date to this order food
	long mOrderDate;

	//the waiter to this order food
	String mWaiter;
	
	//the taste group to this order food
	TasteGroup mTasteGroup;							
	
	//the cancel reason to this order food
	CancelReason mCancelReason;
	
	//indicates whether the food is temporary
	boolean isTemporary = false;				
	
	//indicates whether the food is repaid.
	boolean isRepaid = false;
	
	//indicates the order food is need to be hurried
	boolean isHurried = false;
	
	//indicates the order food is need to be hang up
	boolean isHangup = false;
	
	//the discount to this food represent as integer
	private int mDiscount = 100;	 
	
	final static int MAX_ORDER_AMOUNT = 255 * 100;

	//the current order amount to this order food
	private int mCurCnt;		
	
	//the last order amount to this order food
	private int mLastCnt;	

	public Food asFood(){
		return this;
	}
	
	public void toggleHangup(){
		this.isHangup = !this.isHangup;
	}
	
	public void setHangup(boolean isHangup){
		this.isHangup = isHangup;
	}
	
	public boolean isHangup(){
		return this.isHangup;
	}
	
	public void setHurried(boolean isHurried){
		this.isHurried = isHurried;
	}
	
	public boolean isHurried(){
		return this.isHurried;
	}
	
	/**
	 * Set this order food to be temporary or NOT.
	 * @param onOff
	 */
	public void setTemp(boolean onOff){
		this.isTemporary = onOff;
	}
	
	/**
	 * Indicates this order food is temporary or NOT.
	 * @return true if temporary, otherwise false
	 */
	public boolean isTemp(){
		return this.isTemporary;
	}
	
	/**
	 * Add the order amount to order food.
	 * @param countToAdd the count to add
	 * @throws ProtocolException
	 * 			throws if the count to add exceeds {@link MAX_ORDER_AMOUNT}
	 */
	public void addCount(Float countToAdd) throws ProtocolException{
		if(countToAdd.floatValue() >= 0){
			addCountInternal(NumericUtil.float2Int(countToAdd));
		}else{
			throw new IllegalArgumentException("The count(" + countToAdd.floatValue() + ") to add should be positive.");
		}
	}
	
	void addCountInternal(int countToAdd) throws ProtocolException{
		if(countToAdd >= 0){
			int amount = mCurCnt + countToAdd; 
			if(amount <= MAX_ORDER_AMOUNT){
				mLastCnt = mCurCnt;
				mCurCnt = amount;
			}else{
				throw new ProtocolException("对不起，\"" + this.getName() + "\"每次最多只能点" + MAX_ORDER_AMOUNT / 100 + "份");
			}
		}else{
			throw new IllegalArgumentException("The count(" + countToAdd / 100 + ") to add should be positive.");			
		}
	}
	
	/**
	 * Remove the order amount to order food.
	 * @param countToRemove 
	 * 			the count to remove
	 * @throws ProtocolException
	 * 			throws if the count to remove is greater than original count
	 */
	public void removeCount(Float countToRemove) throws ProtocolException{
		if(countToRemove.floatValue() >= 0){
			removeCountInternal(NumericUtil.float2Int(countToRemove));
		}else{
			throw new IllegalArgumentException("The count(" + countToRemove.floatValue() + ") to remove should be positive.");
		}
	}
	
	/**
	 * Remove the order amount to order food for internal.
	 * @param countToRemove 
	 * 			the count to remove
	 * @throws ProtocolException
	 * 			throws if the count to remove is greater than original count
	 */
	void removeCountInternal(int countToRemove) throws ProtocolException{
		if(countToRemove >= 0){
			if(countToRemove <= getCountInternal()){
				mLastCnt = mCurCnt;
				mCurCnt -= countToRemove;
			}else{
				throw new ProtocolException("输入的删除数量大于已点数量, 请重新输入");
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
		setCountInternal(NumericUtil.float2Int(count));			
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
		return NumericUtil.int2Float(getCountInternal());
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
		return NumericUtil.int2Float(getLastCountInternal());
	}
	
	/**
	 * Get the last count(used for internal) to this order food.
	 * @return the original count represented as integer to this order food
	 */
	int getLastCountInternal(){
		return mLastCnt;
	}
	
	/**
	 * Comparing two foods without the tastes
	 * @param food
	 * @return
	 */
	public boolean equalsIgnoreTaste(OrderFood food){
		if(isTemporary != food.isTemporary){
			return false;
		}else if(isTemporary && food.isTemporary){
			return this.getName().equals(food.getName()) && (this.getPrice() == food.getPrice());
		}else{
			return this.getAliasId() == food.getAliasId();
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
	 * 3 - If both of foods is NOT temporary, check to see the food along with its associated tastes.
	 */
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof OrderFood)){
			return false;
			
		}else{
			OrderFood food = (OrderFood)obj;
			if(isTemporary != food.isTemporary){
				return false;
				
			}else if(isTemporary && food.isTemporary){
				return this.getName().equals(food.getName()) && (this.getPrice() == food.getPrice());
				
			}else{
				return this.getRestaurantId() == food.getRestaurantId() && 
					   this.getAliasId() == food.getAliasId() && 
					   equalsByTasteGroup(food);
			}
		}
	}

	/**
	 * Generate the hash code according to the equals method.
	 */
	public int hashCode(){
		int result = 17;
		if(isTemporary){
			result = 31 * result + this.getName().hashCode();
			result = 31 * result + Math.round(this.getPrice());
		}else{
			result = 31 * result + this.getAliasId();
			result = 31 * (mTasteGroup != null ? mTasteGroup.hashCode() : 0);
		}
		return result;
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
	 * Calculate the price with taste before discount to a specific food.
	 * @return The price represented as float.
	 */	
	public float calcPriceBeforeDiscount(){
		if(isWeigh()){
			return NumericUtil.roundFloat(getUnitPriceWithTaste() * getCount()  + (hasTaste() ? mTasteGroup.getTastePrice() : 0));			
		}else{
			return NumericUtil.roundFloat(getUnitPriceWithTaste() * getCount());
		}
	}
	
	/**
	 * The unit price with taste to a specific food is as below.
	 * unit_price = food_price * discount + taste_price + tmp_taste_price
	 * If taste price is calculated by rate, then
	 * taste_price = food_price * taste_rate
	 * @return the unit price represented as a Float
	 */
	public float getUnitPriceWithTaste(){
		return NumericUtil.roundFloat(getPrice() + (!hasTaste() || isWeigh() ? 0 : mTasteGroup.getTastePrice()));
	}
	
	/**
	 * Calculate the total price to this food along with taste as below<br>.
	 * price = ((food_price + taste_price) * discount) * count 
	 * @return the total price to this food represented as float
	 */
	public float calcPriceWithTaste(){
		if(isWeigh()){
			return NumericUtil.roundFloat((getUnitPriceWithTaste() * getCount() + (hasTaste() ? mTasteGroup.getTastePrice() : 0)) * getDiscount());			
		}else{
			return NumericUtil.roundFloat(getUnitPriceWithTaste() * getCount()  * getDiscount());	
		}
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * price = unit_price * (1 - discount)
	 * @return the discount price to this food represented as an float
	 */
	public float calcDiscountPrice(){
		if(getDiscount() != 1){
			return NumericUtil.roundFloat((getPrice() + (mTasteGroup == null ? 0 : mTasteGroup.getTastePrice())) * getCount() * (1 - getDiscount()));
		}else{
			return 0;
		}
	}	
	
	/**
	 * Override the same method to super.
	 * Get the alias id according to name and price in case of temporary,
	 * otherwise return its own alias.
	 * @return the alias id to this order food
	 */
	public int getAliasId(){
		if(isTemporary){
			return Math.abs((this.getName().hashCode() + Math.round(this.getPrice())) % 65535);
		}else{
			return super.getAliasId();
		}
	}
	
	public OrderFood(){

	}

	public OrderFood(Food src){
		super.copyFrom(src);
	}

	public OrderFood(OrderFood src){
		copyFrom(src);
		this.mOrderDate = src.mOrderDate;
		this.mOrderId = src.mOrderId;
		this.mWaiter = src.mWaiter;
		this.isHangup = src.isHangup;
		this.isTemporary = src.isTemporary;
		this.mDiscount = src.mDiscount;
		this.mLastCnt = src.mLastCnt;
		this.mCurCnt = src.mCurCnt;
		this.isHurried = src.isHurried;
		this.isRepaid = src.isRepaid;
		this.mTasteGroup = src.mTasteGroup;
	}
	
	public TasteGroup makeTasteGroup(){
		mTasteGroup = new TasteGroup();
		mTasteGroup.setAttachedFood(this);
		return mTasteGroup;
	}
	
	public TasteGroup makeTasteGroup(List<Taste> normal, Taste tmp){
		mTasteGroup = new TasteGroup(this, normal, tmp);
		return mTasteGroup;
	}
	
	public TasteGroup makeTasteGroup(Taste[] normal, Taste tmp){
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
		if(mTasteGroup == null){
			makeTasteGroup();
		}
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
	
	public int getOrderId(){
		return mOrderId;
	}
	
	public void setOrderId(int orderId){
		this.mOrderId = orderId;
	}
	
	public long getOrderDate() {
		return mOrderDate;
	}

	public void setOrderDate(long orderDate) {
		this.mOrderDate = orderDate;
	}

	public String getWaiter() {
		return mWaiter;
	}

	public void setWaiter(String waiter) {
		this.mWaiter = waiter;
	}

	public void setRepaid(boolean isRepaid){
		this.isRepaid = isRepaid; 
	}
	
	public boolean isRepaid(){
		return isRepaid;
	}
	
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
		setDiscountInternal(NumericUtil.float2Int(discount));
	}
	
	/**
	 * Get the discount to this order food.
	 * @return the discount to this order food
	 */
	public Float getDiscount(){
		return NumericUtil.int2Float(getDiscountInternal());
	}
	
	/**
	 * Return the order food string.
	 * The string format is as below.
	 * name-taste1,taste2,taste3
	 */
	public String toString(){
		return this.getName() + (hasTaste() ? ("-" + mTasteGroup.toString()) : "");
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		dest.writeBoolean(this.isTemporary);
		if(flag == OF_PARCELABLE_4_QUERY){
			
			if(this.isTemporary){
				dest.writeString(this.getName());
				dest.writeFloat(this.getPrice());
				dest.writeParcel(this.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);
			}else{
				dest.writeShort(this.getStatus());
				dest.writeParcel(this.mTasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
			}
			
			dest.writeShort(this.getAliasId());
			dest.writeInt(this.mCurCnt);
			dest.writeBoolean(this.isHangup);
			dest.writeLong(this.mOrderDate);
			dest.writeString(this.mWaiter);

		}else if(flag == OF_PARCELABLE_4_COMMIT){
			if(this.isTemporary){
				dest.writeString(this.getName());
				dest.writeFloat(this.getPrice());
				dest.writeParcel(this.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);
			}else{
				dest.writeShort(this.getStatus());
				dest.writeParcel(this.mTasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
			}
			
			dest.writeShort(this.getAliasId());
			dest.writeInt(this.mCurCnt);
			dest.writeBoolean(this.isHangup);
			dest.writeLong(this.mOrderDate);
			dest.writeString(this.mWaiter);
			dest.writeBoolean(this.isHurried);
			dest.writeParcel(this.mCancelReason, CancelReason.CR_PARCELABLE_SIMPLE);
		}
	}
	
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		
		this.isTemporary = source.readBoolean();
		
		if(flag == OF_PARCELABLE_4_QUERY){
			if(isTemporary){
				this.setName(source.readString());
				this.setPrice(source.readFloat());
				this.setKitchen(source.readParcel(Kitchen.KITCHEN_CREATOR));
			}else{
				this.setStatus(source.readShort());
				this.mTasteGroup = source.readParcel(TasteGroup.TG_CREATOR);
			}
			
			this.setAliasId(source.readShort());
			this.mCurCnt = source.readInt();
			this.isHangup = source.readBoolean();
			this.mOrderDate = source.readLong();
			this.mWaiter = source.readString();
			
		}else if(flag == OF_PARCELABLE_4_COMMIT){
			if(isTemporary){
				this.setName(source.readString());
				this.setPrice(source.readFloat());
				this.setKitchen(source.readParcel(Kitchen.KITCHEN_CREATOR));
			}else{
				this.setStatus(source.readShort());
				this.mTasteGroup = source.readParcel(TasteGroup.TG_CREATOR);
			}
			
			this.setAliasId(source.readShort());
			this.mCurCnt = source.readInt();
			this.isHangup = source.readBoolean();
			this.mOrderDate = source.readLong();
			this.mWaiter = source.readString();
			this.isHurried = source.readBoolean();
			this.mCancelReason = source.readParcel(CancelReason.CR_CREATOR);
			
		}
		
		if(mTasteGroup != null){
			mTasteGroup.setAttachedFood(this);
		}
	}
	
	public static Parcelable.Creator<OrderFood> OF_CREATOR = new Parcelable.Creator<OrderFood>() {
		
		public OrderFood[] newInstance(int size) {
			return new OrderFood[size];
		}
		
		public OrderFood newInstance() {
			return new OrderFood();
		}
	};
}
