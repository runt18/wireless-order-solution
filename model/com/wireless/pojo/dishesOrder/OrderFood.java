package com.wireless.pojo.dishesOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class OrderFood implements Parcelable, Jsonable {
	
	public final static byte OF_PARCELABLE_4_COMMIT = 0;
	public final static byte OF_PARCELABLE_4_QUERY = 1;
	
	public static enum Operation{
		ADD(1, "加菜"),
		CANCEL(2, "退菜"),
		TRANSFER(3, "转菜");
		
		private final int val;
		private final String desc;
		
		Operation(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Operation valueOf(int val){
			for(Operation operation : values()){
				if(operation.val == val){
					return operation;
				}
			}
			throw new IllegalArgumentException("The operation(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	//the id to this order food
	private long id;
	
	//the order id associated with this order food
	private int mOrderId;
	
	//the order date to this order food
	private long mOrderDate;

	//the waiter to this order food
	private String mWaiter;
	
	//the operation to this order food
	private Operation operation;
	
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

	//indicates the order food is gift
	private boolean isGift = false;
	
	//the price plan to this order food
	private PricePlan mPricePlan;
	
	//the discount to this order food
	private float mDiscount = 1;	 
	
	final static int MAX_ORDER_AMOUNT = Short.MAX_VALUE;

	//the current order amount to this order food
	private float mCurCnt;		
	
	//the last order amount to this order food
	private float mLastCnt;	

	private List<ComboOrderFood> combo;
	
	private final Food mFood = new Food(0);
	
	public Food asFood(){
		return mFood;
	}
	
	public Operation getOperation(){
		return this.operation;
	}
	
	public void setOperation(Operation operation){
		this.operation = operation;
	}
	
	public void addCombo(ComboOrderFood comboFood){
		if(combo == null){
			combo = new ArrayList<ComboOrderFood>();
		}
		combo.add(comboFood);
	}
	
	public List<ComboOrderFood> getCombo(){
		if(combo != null){
			return Collections.unmodifiableList(combo);
		}else{
			return Collections.emptyList();
		}
	}
	
	public boolean hasCombo(){
		return combo != null ? !combo.isEmpty() : false;
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
	
	public boolean isGift(){
		return this.isGift;
	}
	
	public void setGift(boolean isGift){
		this.isGift = isGift;
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
				//mLastCnt = mCurCnt;
				mCurCnt = amount;
			}else{
				throw new BusinessException("对不起，\"" + mFood.getName() + "\"每次最多只能点" + MAX_ORDER_AMOUNT + "份");
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
					//mLastCnt = mCurCnt;
					mCurCnt -= countToRemove;
				}else{
					throw new BusinessException("输入的删除数量大于已点数量, 请重新输入");
				}
			}else{
				throw new IllegalArgumentException("The count(" + countToRemove + ") to remove should be positive.");
			}
		}else{
			throw new BusinessException(StaffError.CANCEL_FOOD_NOT_ALLOW);
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
		//mLastCnt = mCurCnt;
		mLastCnt = mCurCnt = (count <= MAX_ORDER_AMOUNT ? count : MAX_ORDER_AMOUNT);
	}
	
	/**
	 * Get the current count to this order food.
	 * @return the current count to this order food
	 */
	public float getCount(){
		return NumericUtil.roundFloat(mCurCnt);
	}
	
	/**
	 * Check to see if the order food has taste group(either normal taste or temporary taste).
	 * @return true if the order food has taste, otherwise false
	 */
	public boolean hasTasteGroup(){
		return mTasteGroup == null ? false : mTasteGroup.hasPreference();
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
	 * The unit price with taste to a specific food is as below.
	 * <pre>
	 * if(isWeight){
	 *     unit_price = food_price + taste_price;
	 * }else{
	 *     unit_price = food_price;
	 * } 
	 * </pre>
	 * @return the unit price to this order food
	 */
	private float getUnitPrice(){
		return NumericUtil.roundFloat(getFoodPrice() + (!hasTasteGroup() || mFood.isWeigh() ? 0 : mTasteGroup.getPrice()));
	}
	
	/**
	 * The unit price with taste to a specific food is as below.
	 * <pre>
	 * if(isGift){
	 *     return 0;
	 * }else{
	 * 	   return {@link getUnitPrice}
	 * } 
	 * </pre>
	 * @return the unit price to this order food
	 */
	public float calcUnitPrice(){
		if(isGift){
			return 0;
		}else{
			return getUnitPrice();
		}
	}
	
	/**
	 * Calculate the total price to this order food along with taste before discount.
	 * <pre>
	 * if(isWeight){
	 *    return {@link OrderFood#getUnitPrice()} * count + tastePrice
	 * }else{
	 *    return {@link OrderFood#getUnitPrice()} * count 
	 * }
	 * @return the total price to this food before discount
	 */
	private float getPriceBeforeDiscount(){
		if(mFood.isWeigh()){
			return NumericUtil.roundFloat(getUnitPrice() * getCount() + (hasTasteGroup() ? mTasteGroup.getPrice() : 0));			
		}else{
			return NumericUtil.roundFloat(getUnitPrice() * getCount());	
		}
	}
	
	/**
	 * Calculate the price with taste before discount to a specific food.
	 * <pre>
	 * if(isGift){
	 *    return 0;
	 * }else{
	 *    return {@link OrderFood#getPriceBeforeDiscount()}
	 * }
	 * @return The price represented as float.
	 */	
	public float calcPriceBeforeDiscount(){
		if(isGift){
			return 0;
		}else {
			return getPriceBeforeDiscount();
		}
	}
	
	/**
	 * Calculate the total price to this order food along with taste as below.
	 * <pre>
	 * if(isGift){
	 *    return 0;
	 * }else{
	 *    return {@link OrderFood#getPriceBeforeDiscount} * discount
	 * }
	 * @return the total price to this order food 
	 */
	public float calcPrice(){
		if(isGift){
			return 0;
		}else{
			return NumericUtil.roundFloat(getPriceBeforeDiscount() * getDiscount());
		}
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * <pre>
	 * if(isGift){
	 *     return 0;
	 * }else{
	 *     return {@link OrderFood#getPriceBeforeDiscount} * (1 - discount)
	 * }
	 * </pre>
	 * @return the discount price to this food represented as an float
	 */
	public float calcDiscountPrice(){
		if(getDiscount() != 1 && !isGift){
			return NumericUtil.roundFloat(getPriceBeforeDiscount() * (1 - getDiscount()));
		}else{
			return 0;
		}
	}	
	
	/**
	 * Calculate the gift price to this order food.
	 * <pre>
	 * if(isGift){
	 *     return {@link OrderFood#getPriceBeforeDiscount()}
	 * }
	 * @return the gift price to this order food
	 */
	public float calcGiftPrice(){
		if(isGift){
			return getPriceBeforeDiscount();
		}else{
			return 0;
		}
	}
	
	public OrderFood(){
		
	}

	public OrderFood(long id){
		this.id = id;
	}
	
	public OrderFood(Food src){
		mFood.copyFrom(src);
	}

	public OrderFood(Food src, float amount){
		mFood.copyFrom(src);
		setCount(amount);
	}
	
	public OrderFood(OrderFood src){
		mFood.copyFrom(src.mFood);
		this.mOrderDate = src.mOrderDate;
		this.mOrderId = src.mOrderId;
		this.mWaiter = src.mWaiter;
		this.isHangup = src.isHangup;
		this.isGift = src.isGift;
		this.isTemporary = src.isTemporary;
		this.mDiscount = src.mDiscount;
		this.mLastCnt = src.mLastCnt;
		this.mCurCnt = src.mCurCnt;
		this.isHurried = src.isHurried;
		this.isRepaid = src.isRepaid;
		this.mTasteGroup = src.mTasteGroup;
	}
	
	public void clearTasetGroup(){
		mTasteGroup = null;
	}
	
	public TasteGroup getTasteGroup(){
		if(mTasteGroup == null){
			return TasteGroup.EMPTY;
		}
		return mTasteGroup;
	}

	public void setTasteGroup(TasteGroup tg){
		if(tg != null){
			mTasteGroup = tg;
			mTasteGroup.setAttachedFood(mFood);
		}
	}

	public boolean addTaste(Taste tasteToAdd){
		if(mTasteGroup == null){
			mTasteGroup = new TasteGroup(mFood);
		}
		if(mTasteGroup.addTaste(tasteToAdd)){
			mTasteGroup.refresh();
			return true;
		}else{
			return false;
		}
	}
	
	public void setTmpTaste(Taste tmpTaste){
		if(mTasteGroup == null){
			mTasteGroup = new TasteGroup(mFood);
		}
		mTasteGroup.setTmpTaste(tmpTaste);
	}
	
	public boolean removeTaste(Taste tasteToRemove){
		if(mTasteGroup != null){
			if(mTasteGroup.removeTaste(tasteToRemove)){
				mTasteGroup.refresh();
				return true;
			}else{
				return false;
			}
		}else{
			return false;
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
	
	public long getId(){
		return this.id;
	}
	
	public void setId(long id){
		this.id = id;
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
	
	void setPricePlan(PricePlan pricePlan){
		this.mPricePlan = pricePlan;
	}
	
	/**
	 * Set the discount to this order food.
	 * @param discount the discount to set
	 */
	public void setDiscount(float discount){
		if(isGift() || asFood().isSpecial()){
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
	
	public int getFoodId(){
		if(isTemporary){
			CRC32 crc = new CRC32();
			crc.update((mFood.getName() + mFood.getPrice() + mFood.getKitchen().getId()).getBytes());
			return Math.abs((int)crc.getValue() % Integer.MAX_VALUE);
		}else{
			return mFood.getFoodId();
		}
	}
	
	/**
	 * Override the same method to super.
	 * Get the alias id according to name and price in case of temporary,
	 * otherwise return its own alias.
	 * @return the alias id to this order food
	 */
	public int getAliasId(){
		return mFood.getAliasId();
	}
	
	public int getRestaurantId(){
		return mFood.getRestaurantId();
	}
	
	public String getName(){
		return mFood.getName();
	}
	
	public float getFoodPrice(){
		return mFood.getPrice(mPricePlan);
	}
	
	public float getPrice(){
		return getUnitPrice();
	}
	
	public Kitchen getKitchen(){
		return mFood.getKitchen();
	}
	
	/**
	 * Comparing two foods without the tastes
	 * @param of the order food to compare
	 * @return true if the order food is the same ignoring taste, otherwise false
	 */
	public boolean equalsIgnoreTaste(OrderFood of){
		if(isTemporary != of.isTemporary){
			return false;
		}else if(isTemporary && of.isTemporary){
			return mFood.getName().equals(of.asFood().getName()) && (mFood.getPrice() == of.asFood().getPrice());
		}else{
			return mFood.equals(of.asFood());
		}
	}
	
	/**
	 * Check to see whether the taste group to two foods is the same.
	 * @param of the order food to compared
	 * @return true if the taste group is the same, otherwise false
	 */
	boolean equalsByTasteGroup(OrderFood of){
		if(hasTasteGroup() && of.hasTasteGroup()){
			return mTasteGroup.equals(of.mTasteGroup);
			
		}else if(!hasTasteGroup() && !of.hasTasteGroup()){
			return true;
			
		}else{
			return false;
		}
	}
	
	/**
	 * There are three ways to determine whether two foods is the same as each other.
	 * <li>If one food is gift while the order NOT, means they are NOT the same.
	 * <li>If one food is temporary while the other NOT, means they are NOT the same.
	 * <li>If both of foods are temporary, check to see whether their names and price are the same.
	 * <li>If both of foods is NOT temporary, check to see the food along with its associated tastes.
	 */
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof OrderFood)){
			return false;
			
		}else{
			OrderFood of = (OrderFood)obj;
			if(isGift != of.isGift){
				return false;
				
			}else if(isTemporary != of.isTemporary){
				return false;
				
			}else if(isTemporary && of.isTemporary){
				return mFood.getName().equals(of.asFood().getName()) && (mFood.getPrice() == of.asFood().getPrice());
				
			}else{
				return mFood.equals(of.asFood()) && equalsByTasteGroup(of);
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
		return mFood.getName() + (hasTasteGroup() ? ("-" + mTasteGroup.getPreference()) : "");
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		dest.writeBoolean(this.isTemporary);
		dest.writeBoolean(this.isGift);
		if(flag == OF_PARCELABLE_4_QUERY){

			if(!this.isTemporary){
				dest.writeShort(mFood.getStatus());
				dest.writeParcel(this.mTasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
			}
			dest.writeLong(this.getId());
			dest.writeString(mFood.getName());
			dest.writeFloat(mFood.getPrice());
			dest.writeParcel(mFood.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);
			if(mFood.isCombo()){
				dest.writeParcelList(mFood.getChildFoods(), ComboFood.COMBO_FOOD_PARCELABLE_SIMPLE);
			}
			dest.writeInt(this.getFoodId());
			dest.writeShort(this.getAliasId());
			dest.writeFloat(this.getDiscount());
			dest.writeFloat(this.getCount());
			dest.writeBoolean(this.isHangup());
			dest.writeBoolean(this.isGift());
			dest.writeLong(this.getOrderDate());
			dest.writeString(this.getWaiter());
			dest.writeParcelList(this.combo, 0);

		}else if(flag == OF_PARCELABLE_4_COMMIT){
			if(this.isTemporary){
				dest.writeString(mFood.getName());
				dest.writeFloat(mFood.getPrice());
				dest.writeParcel(mFood.getKitchen(), Kitchen.KITCHEN_PARCELABLE_SIMPLE);
			}else{
				dest.writeShort(mFood.getStatus());
				dest.writeParcel(this.mTasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
			}
			dest.writeLong(this.getId());
			dest.writeInt(this.getFoodId());
			dest.writeShort(this.getAliasId());
			dest.writeFloat(this.getCount());
			dest.writeBoolean(this.isHangup());
			dest.writeBoolean(this.isGift());
			dest.writeLong(this.mOrderDate);
			dest.writeString(this.mWaiter);
			dest.writeBoolean(this.isHurried);
			dest.writeParcel(this.mCancelReason, CancelReason.CR_PARCELABLE_SIMPLE);
			dest.writeParcelList(this.combo, 0);
		}
	}
	
	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		
		this.isTemporary = source.readBoolean();
		this.isGift = source.readBoolean();
		
		if(flag == OF_PARCELABLE_4_QUERY){
			if(!isTemporary){
				mFood.setStatus(source.readShort());
				setTasteGroup(source.readParcel(TasteGroup.CREATOR));
			}
			this.setId(source.readLong());
			mFood.setName(source.readString());
			mFood.setPrice(source.readFloat());
			mFood.setKitchen(source.readParcel(Kitchen.CREATOR));
			if(mFood.isCombo()){
				mFood.setChildFoods(source.readParcelList(ComboFood.CREATOR));
			}
			mFood.setFoodId(source.readInt());
			mFood.setAliasId(source.readShort());
			this.setDiscount(source.readFloat());
			this.setCount(source.readFloat());
			this.setHangup(source.readBoolean());
			this.setGift(source.readBoolean());
			this.setOrderDate(source.readLong());
			this.setWaiter(source.readString());
			this.combo = source.readParcelList(ComboOrderFood.CREATOR);
			
		}else if(flag == OF_PARCELABLE_4_COMMIT){
			if(isTemporary){
				mFood.setName(source.readString());
				mFood.setPrice(source.readFloat());
				mFood.setKitchen(source.readParcel(Kitchen.CREATOR));
			}else{
				mFood.setStatus(source.readShort());
				setTasteGroup(source.readParcel(TasteGroup.CREATOR));
			}
			this.setId(source.readLong());
			mFood.setFoodId(source.readInt());
			mFood.setAliasId(source.readShort());
			this.setCount(source.readFloat());
			this.setHangup(source.readBoolean());
			this.setGift(source.readBoolean());
			this.mOrderDate = source.readLong();
			this.mWaiter = source.readString();
			this.isHurried = source.readBoolean();
			this.mCancelReason = source.readParcel(CancelReason.CREATOR);
			this.combo = source.readParcelList(ComboOrderFood.CREATOR);
		}
		
	}
	
	public static Parcelable.Creator<OrderFood> CREATOR = new Parcelable.Creator<OrderFood>() {
		
		@Override
		public OrderFood[] newInstance(int size) {
			return new OrderFood[size];
		}
		
		@Override
		public OrderFood newInstance() {
			return new OrderFood();
		}
	};

	public static enum Key4Json{
		ORDER_ID("orderId", "账单id"),
		ORDER_DATE("orderDateFormat", "账单日期"),
		WAITER("waiter", "服务员"),
		CANCEL_REASON("cancelReason", "退菜原因"),
		IS_TEMP("isTemporary", "是否临时菜"),
		IS_REPAID("isRepaid", "是否反结账"),
		IS_HURRIED("isHurried", "是否催菜"),
		IS_GIFT("isGift", "是否赠送"),
		IS_HANG("isHangup", "是否叫起"),
		IS_COMMISSION("isCommission", "是否提成"),
		IS_TRANSFER("isTransfer", "是否转菜"),
		IS_RETURN("isReturn", "是否退菜"),
		DISCOUNT("discount", "折扣"),
		COUNT("count", "数量"),
		UNIT_PRICE("unitPrice", "单价"),
		ACTUAL_PRICE("actualPrice", "实收"),
		TOTAL_PRICE("totalPrice", "应收"),
		TASTE_GROUP("tasteGroup", "口味"),
		OPERATION("operation", "操作"),
		TOTAL_PRICE_BEFORE_DISCOUNT("totalPriceBeforeDiscount", "应收(折扣前)");
		
		Key4Json(String key, String desc){
			this.key = key;
			this.desc = desc;
		}
		
		private final String key;
		private final String desc;
		
		@Override
		public String toString(){
			return "key = " + key + ",desc = " + desc;
		}
		
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		// extends food
		jm.putJsonable(this.mFood, 0);
		jm.putInt(Key4Json.ORDER_ID.key, this.mOrderId);
		jm.putString(Key4Json.ORDER_DATE.key, DateUtil.format(this.mOrderDate));
		jm.putString(Key4Json.WAITER.key, this.mWaiter);
		jm.putJsonable(Key4Json.CANCEL_REASON.key, this.mCancelReason, 0);
		jm.putBoolean(Key4Json.IS_TEMP.key, this.isTemporary);
		jm.putBoolean(Key4Json.IS_REPAID.key, this.isRepaid);
		jm.putBoolean(Key4Json.IS_HURRIED.key, this.isHurried);
		jm.putBoolean(Key4Json.IS_HANG.key, this.isHangup);
		jm.putBoolean(Key4Json.IS_GIFT.key, this.isGift);
		jm.putBoolean(Key4Json.IS_TRANSFER.key, this.operation == Operation.TRANSFER);
		jm.putBoolean(Key4Json.IS_COMMISSION.key, this.mFood.isCommission());
		jm.putBoolean(Key4Json.IS_RETURN.key, this.getCount() < 0 ? true : false);
		jm.putFloat(Key4Json.DISCOUNT.key, this.mDiscount);
		jm.putFloat(Key4Json.COUNT.key, this.getCount());
		jm.putFloat(Key4Json.UNIT_PRICE.key, this.asFood().getPrice());
		jm.putFloat(Key4Json.ACTUAL_PRICE.key, this.asFood().getPrice());
		jm.putFloat(Key4Json.TOTAL_PRICE.key, this.calcPrice());
		jm.putJsonable(Key4Json.TASTE_GROUP.key, this.getTasteGroup(), 0);
		jm.putString(Key4Json.OPERATION.key, this.getOperation() != null? this.getOperation().getDesc() : "点菜");
		jm.putFloat(Key4Json.TOTAL_PRICE_BEFORE_DISCOUNT.key, this.calcPriceBeforeDiscount());
		
		return jm;
	}

	public final static int OF_JSONABLE_4_COMMIT = 0; 
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		if(flag == OF_JSONABLE_4_COMMIT){
			if(jsonMap.getBoolean(Key4Json.IS_TEMP.key)){
				setTemp(true);
				//name to temporary...must
				if(jsonMap.containsKey(Food.Key4Json.FOOD_NAME.key)){
					mFood.setName(jsonMap.getString(Food.Key4Json.FOOD_NAME.key));
				}else{
					throw new IllegalStateException("提交的临时菜数据缺少(" + Food.Key4Json.FOOD_NAME.toString() + ")");
				}
				//price to temporary...must
				if(jsonMap.containsKey(Food.Key4Json.FOOD_PRICE.key)){
					mFood.setPrice(jsonMap.getFloat(Food.Key4Json.FOOD_PRICE.key));
				}else{
					throw new IllegalStateException("提交的临时菜数据缺少(" + Food.Key4Json.FOOD_PRICE.toString() + ")");
				}
				//kitchen to temporary...must
				mFood.setKitchen(jsonMap.getJsonable(Food.Key4Json.ASSOCIATED_KITCHEN.key, Kitchen.JSON_CREATOR, Kitchen.KITCHEN_JSONABLE_SIMPLE));
			}else{
				setTemp(false);
				//food id...must
				if(jsonMap.containsKey(Food.Key4Json.FOOD_ID.key)){
					mFood.setFoodId(jsonMap.getInt(Food.Key4Json.FOOD_ID.key));
				}else{
					throw new IllegalStateException("提交的数据缺少(" + Food.Key4Json.FOOD_ID.toString() + ")");
				}
			}

			if(jsonMap.containsKey(Key4Json.COUNT.key)){
				setCount(jsonMap.getFloat(Key4Json.COUNT.key));
			}else{
				throw new IllegalStateException("提交的数据缺少(" + Key4Json.COUNT.toString() + ")");
			}
			
			if(jsonMap.containsKey(Key4Json.IS_HANG.key)){
				setHangup(jsonMap.getBoolean(Key4Json.IS_HANG.key));
			}
			
			
			if(jsonMap.containsKey(Key4Json.IS_GIFT.key)){
				setGift(jsonMap.getBoolean(Key4Json.IS_GIFT.key));
			}
			
			if(jsonMap.containsKey(Key4Json.TASTE_GROUP.key)){
				setTasteGroup(jsonMap.getJsonable(Key4Json.TASTE_GROUP.key, TasteGroup.JSON_CREATOR, TasteGroup.TG_JSONABLE_4_COMMIT));
			}
			
			if(jsonMap.containsKey(Key4Json.CANCEL_REASON.key)){
				setCancelReason(jsonMap.getJsonable(Key4Json.CANCEL_REASON.key, CancelReason.JSON_CREATOR, CancelReason.CR_JSONABLE_4_COMMIT));
			}
		}
	}
	
	public static Jsonable.Creator<OrderFood> JSON_CREATOR = new Jsonable.Creator<OrderFood>() {
		@Override
		public OrderFood newInstance() {
			return new OrderFood(0);
		}
	};
}
