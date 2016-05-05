package com.wireless.pojo.billStatistics.repaid;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class RepaidStatistics implements Jsonable{

	private int mId;		
	private long mOrderDate;
	private float mTotalPrice = 0;	
	private float mActualPrice = 0;
	private float mRepaidPrice = 0;
	private PayType mPaymentType = PayType.CASH;
	private String restaurantName;
	private int restaurantId;
	private Staff staff;
	
	public void setRestaurantName(String name){
		this.restaurantName = name;
	}

	public String getRestaurantName(){
		return this.restaurantName;
	}
	
	public void setRestaurantId(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public int getRestaurantId(){
		return this.restaurantId;
	}
	
	public Staff getStaff() {
		if(staff == null){
			staff = new Staff();
		}
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public int getmId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public long getmOrderDate() {
		return mOrderDate;
	}

	public void setOrderDate(long mOrderDate) {
		this.mOrderDate = mOrderDate;
	}

	public float getTotalPrice() {
		return NumericUtil.roundFloat(mTotalPrice);
	}

	public void setTotalPrice(float mTotalPrice) {
		this.mTotalPrice = mTotalPrice;
	}

	public float getActualPrice() {
		return NumericUtil.roundFloat(mActualPrice);
	}

	public void setActualPrice(float mActualPrice) {
		this.mActualPrice = mActualPrice;
	}

	public float getRepaidPrice() {
		return NumericUtil.roundFloat(mRepaidPrice);
	}

	public void setRepaidPrice(float mRepaidPrice) {
		this.mRepaidPrice = mRepaidPrice;
	}
	
	public void setPaymentType(PayType payType){
		this.mPaymentType = payType;
	}
	
	public PayType getPaymentType(){
		return this.mPaymentType;
	}
	
	@Override
	public int hashCode(){
		return 17 + 31 * mId;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof RepaidStatistics)){
			return false;
		}else{
			return mId == ((RepaidStatistics)obj).mId;
		}
	}
	
	@Override
	public String toString(){
		return "RepaidStatistics(orderId = " + mId + ")";
	}
	

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("orderId", this.mId);
		jm.putInt("rid", this.restaurantId);
		jm.putString("restaurantName", this.restaurantName);
		jm.putString("orderDateFormat", DateUtil.format(this.mOrderDate));
		jm.putFloat("actualPrice", this.mActualPrice);
		jm.putFloat("totalPrice", this.mTotalPrice);
		jm.putFloat("repaidPrice", this.mRepaidPrice);
		jm.putInt("payTypeValue", this.mPaymentType.getId());
		jm.putString("payTypeText", this.mPaymentType.getName());
		jm.putFloat("oldTotalPrice", NumericUtil.roundFloat(this.mTotalPrice - this.mRepaidPrice));
		jm.putFloat("oldActualPrice", NumericUtil.roundFloat(this.mActualPrice - this.mRepaidPrice));
		jm.putString("operateStaff", this.getStaff().getName());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}
