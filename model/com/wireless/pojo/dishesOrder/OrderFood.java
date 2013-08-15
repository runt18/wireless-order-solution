package com.wireless.pojo.dishesOrder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class OrderFood implements Parcelable, Comparable<OrderFood>, Jsonable {
	
	public final static byte OF_PARCELABLE_4_COMMIT = 0;
	public final static byte OF_PARCELABLE_4_QUERY = 1;
	
	//the order id associated with this order food
	private int mOrderId;
	
	//the order date to this order food
	private long mOrderDate;

	//the waiter to this order food
	private String mWaiter;
	
	//the taste group to this order food
	private TasteGroup mTasteGroup;							
	
	//the cancel reason to this order food
	private CancelReason mCancelReason;
	
	//indicates whether the food is temporary
	private boolean isTemporary = false;				
	
	//indicates whether the food is repaid.
	private boolean isRepaid = false;
	
	//indicates the order food is need to be hurried
	private boolean isHurried = false;
	
	//indicates the order food is need to be hang up
	private boolean isHangup = false;
	
	//the discount to this food represent as integer
	private float mDiscount = 1;	 
	
	final static int MAX_ORDER_AMOUNT = 255;

	//the current order amount to this order food
	private float mCurCnt;		
	
	//the last order amount to this order food
	private float mLastCnt;	

	private final Food mFood = new Food();
	
	public Food asFood(){
		return mFood;
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
	 * @throws BusinessException
	 * 			throws if the count to add exceeds {@link MAX_ORDER_AMOUNT}
	 */
	public void addCount(float countToAdd) throws BusinessException{
		if(countToAdd >= 0){
			float amount = mCurCnt + countToAdd; 
			if(amount <= MAX_ORDER_AMOUNT){
				mLastCnt = mCurCnt;
				mCurCnt = amount;
			}else{
				throw new BusinessException("对不起，\"" + mFood.getName() + "\"每次最多只能点" + MAX_ORDER_AMOUNT / 100 + "份");
			}
			
		}else{
			throw new IllegalArgumentException("The count(" + countToAdd + ") to add should be positive.");
		}
	}
	
	/**
	 * Remove the order amount to order food.
	 * @param countToRemove 
	 * 			the count to remove
	 * @param staff
	 * 			the staff to remove count
	 * @throws BusinessException
	 * 			throws if the count to remove is greater than original count
	 * 			throws if the staff does NOT own the cancel food privilege
	 */
	public void removeCount(float countToRemove, Staff staff) throws BusinessException{
		if(staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){
			if(countToRemove >= 0){
				if(countToRemove <= getCount()){
					mLastCnt = mCurCnt;
					mCurCnt -= countToRemove;
				}else{
					throw new BusinessException("输入的删除数量大于已点数量, 请重新输入");
				}
			}else{
				throw new IllegalArgumentException("The count(" + countToRemove + ") to remove should be positive.");
			}
		}else{
			throw new BusinessException(StaffError.PERMISSION_NOT_ALLOW);
		}
	}
	
	/**
	 * Get the delta to order count.
	 * @return the offset to order count
	 */
	public float getDelta(){
		return NumericUtil.roundFloat(mLastCnt - mCurCnt);
	}
	
	/**
	 * Set the current count and reset the offset to zero.
	 * The current count would set to {@link MAX_ORDER_AMOUNT} if the parameter exceeds it.
	 * @param count
	 * 			the order amount to set
	 */
	public void setCount(float count){
		mLastCnt = mCurCnt;
		mCurCnt = (count <= MAX_ORDER_AMOUNT ? count : MAX_ORDER_AMOUNT);		
	}
	
	/**
	 * Get the current count to this order food.
	 * @return the current count to this order food
	 */
	public float getCount(){
		return NumericUtil.roundFloat(mCurCnt);
	}
	
	/**
	 * Get the original count to this order food.
	 * @return the original count to this order food
	 */
	public Float getOriCount(){
		return NumericUtil.roundFloat(mLastCnt);
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
		if(mFood.isWeigh()){
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
		return NumericUtil.roundFloat(mFood.getPrice() + (!hasTaste() || mFood.isWeigh() ? 0 : mTasteGroup.getTastePrice()));
	}
	
	/**
	 * Calculate the total price to this food along with taste as below<br>.
	 * price = ((food_price + taste_price) * discount) * count 
	 * @return the total price to this food represented as float
	 */
	public float calcPriceWithTaste(){
		if(mFood.isWeigh()){
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
			return NumericUtil.roundFloat((mFood.getPrice() + (mTasteGroup == null ? 0 : mTasteGroup.getTastePrice())) * getCount() * (1 - getDiscount()));
		}else{
			return 0;
		}
	}	
	
	public OrderFood(){
		
	}

	public OrderFood(Food src){
		mFood.copyFrom(src);
	}

	public OrderFood(OrderFood src){
		mFood.copyFrom(src.mFood);
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
	 * Set the discount to this order food.
	 * @param discount the discount to set
	 */
	public void setDiscount(float discount){
		if(asFood().isGift() || asFood().isSpecial()){
			this.mDiscount = 1;
		}else{
			this.mDiscount = discount;
		}
	}
	
	/**
	 * Get the discount to this order food.
	 * @return the discount to this order food
	 */
	public float getDiscount(){
		return NumericUtil.roundFloat(mDiscount);
	}
	
	public long getFoodId(){
		return mFood.getFoodId();
	}
	
	public Food getFood(){
		return this.mFood;
	}
	
	/**
	 * Override the same method to super.
	 * Get the alias id according to name and price in case of temporary,
	 * otherwise return its own alias.
	 * @return the alias id to this order food
	 */
	public int getAliasId(){
		if(isTemporary){
			return Math.abs((mFood.getName().hashCode() + Math.round(mFood.getPrice())) % 65535);
		}else{
			return mFood.getAliasId();
		}
	}
	
	public int getRestaurantId(){
		return mFood.getRestaurantId();
	}
	
	public String getName(){
		return mFood.getName();
	}
	
	public float getPrice(){
		return mFood.getPrice();
	}
	
	public Kitchen getKitchen(){
		return mFood.getKitchen();
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
			return mFood.getName().equals(food.asFood().getName()) && (mFood.getPrice() == food.asFood().getPrice());
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
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof OrderFood)){
			return false;
			
		}else{
			OrderFood food = (OrderFood)obj;
			if(isTemporary != food.isTemporary){
				return false;
				
			}else if(isTemporary && food.isTemporary){
				return mFood.getName().equals(food.asFood().getName()) && (mFood.getPrice() == food.asFood().getPrice());
				
			}else{
				return mFood.getRestaurantId() == food.asFood().getRestaurantId() && 
						mFood.getAliasId() == food.asFood().getAliasId() && 
					   equalsByTasteGroup(food);
			}
		}
	}
	
	/**
	 * Generate the hash code according to the equals method.
	 */
	@Override
	public int hashCode(){
		int result = 17;
		if(isTemporary){
			result = 31 * result + mFood.getName().hashCode();
			result = 31 * result + Math.round(mFood.getPrice());
		}else{
			result = 31 * result + this.getAliasId();
			result = 31 * (mTasteGroup != null ? mTasteGroup.hashCode() : 0);
		}
		return result;
	}
	
	/**
	 * Return the order food string.
	 * The string format is as below.
	 * name-taste1,taste2,taste3
	 */
	@Override
	public String toString(){
		return mFood.getName() + (hasTaste() ? ("-" + mTasteGroup.toString()) : "");
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		dest.writeBoolean(this.isTemporary);
		if(flag == OF_PARCELABLE_4_QUERY){
			
			if(this.isTemporary){
				dest.writeString(mFood.getName());
				dest.writeFloat(mFood.getPrice());
				dest.writeParcel(mFood.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);
			}else{
				dest.writeShort(mFood.getStatus());
				dest.writeParcel(this.mTasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
			}
			
			dest.writeShort(this.getAliasId());
			dest.writeFloat(this.mCurCnt);
			dest.writeBoolean(this.isHangup);
			dest.writeLong(this.mOrderDate);
			dest.writeString(this.mWaiter);

		}else if(flag == OF_PARCELABLE_4_COMMIT){
			if(this.isTemporary){
				dest.writeString(mFood.getName());
				dest.writeFloat(mFood.getPrice());
				dest.writeParcel(mFood.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);
			}else{
				dest.writeShort(mFood.getStatus());
				dest.writeParcel(this.mTasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
			}
			
			dest.writeShort(this.getAliasId());
			dest.writeFloat(this.mCurCnt);
			dest.writeBoolean(this.isHangup);
			dest.writeLong(this.mOrderDate);
			dest.writeString(this.mWaiter);
			dest.writeBoolean(this.isHurried);
			dest.writeParcel(this.mCancelReason, CancelReason.CR_PARCELABLE_SIMPLE);
		}
	}
	
	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		
		this.isTemporary = source.readBoolean();
		
		if(flag == OF_PARCELABLE_4_QUERY){
			if(isTemporary){
				mFood.setName(source.readString());
				mFood.setPrice(source.readFloat());
				mFood.setKitchen(source.readParcel(Kitchen.KITCHEN_CREATOR));
			}else{
				mFood.setStatus(source.readShort());
				this.mTasteGroup = source.readParcel(TasteGroup.TG_CREATOR);
			}
			
			mFood.setAliasId(source.readShort());
			this.mCurCnt = source.readFloat();
			this.isHangup = source.readBoolean();
			this.mOrderDate = source.readLong();
			this.mWaiter = source.readString();
			
		}else if(flag == OF_PARCELABLE_4_COMMIT){
			if(isTemporary){
				mFood.setName(source.readString());
				mFood.setPrice(source.readFloat());
				mFood.setKitchen(source.readParcel(Kitchen.KITCHEN_CREATOR));
			}else{
				mFood.setStatus(source.readShort());
				this.mTasteGroup = source.readParcel(TasteGroup.TG_CREATOR);
			}
			
			mFood.setAliasId(source.readShort());
			this.mCurCnt = source.readFloat();
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

	@Override
	public int compareTo(OrderFood o) {
		if(getAliasId() > o.getAliasId()){
			return 1;
		}else if(getAliasId() < o.getAliasId()){
			return -1;
		}else{
			return 0;
		}
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		// extends food
		jm.putAll(new LinkedHashMap<String, Object>(this.mFood.toJsonMap(0)));
		jm.put("orderId", this.mOrderId);
		jm.put("orderDateFormat", DateUtil.format(this.mOrderDate));
		jm.put("waiter", this.mWaiter);
		jm.put("cancelReason", this.mCancelReason);
		jm.put("isTemporary", this.isTemporary);
		jm.put("isRepaid", this.isRepaid);
		jm.put("isHurried", this.isHurried);
		jm.put("isHangup", this.isHangup);
		jm.put("discount", this.mDiscount);
		jm.put("count", this.getCount());
		jm.put("unitPrice", this.getPrice());
		jm.put("actualPrice", this.getPrice());
		jm.put("totalPrice", this.calcPriceWithTaste());
		jm.put("tasteGroup", this.mTasteGroup);
		jm.put("totalPriceBeforeDiscount", this.calcPriceBeforeDiscount());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static int calcTotalCount(List<OrderFood> list){
		int total = 0;
		if(list != null && !list.isEmpty()){
			for(OrderFood temp : list){
				total += temp.getCount();
			}
		}
		return total;
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static float calcTotalPrice(List<OrderFood> list){
		float price = 0;
		if(list != null && !list.isEmpty()){
			for(OrderFood temp : list){
				price += temp.getPrice();
			}
		}
		return price;
	}
	
}
