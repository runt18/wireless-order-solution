package com.wireless.pojo.dishesOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order.PayType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class RepaidStatistics implements Jsonable{

	private int mId;		
	private long mOrderDate;
	private float mTotalPrice = 0;	
	private float mActualPrice = 0;
	private float mRepaidPrice = 0;
	private Order.PayType mPaymentType = PayType.CASH;	
	private Staff staff;
	
	
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

	public void setmId(int mId) {
		this.mId = mId;
	}

	public long getmOrderDate() {
		return mOrderDate;
	}

	public void setmOrderDate(long mOrderDate) {
		this.mOrderDate = mOrderDate;
	}

	public float getmTotalPrice() {
		return NumericUtil.roundFloat(mTotalPrice);
	}

	public void setmTotalPrice(float mTotalPrice) {
		this.mTotalPrice = mTotalPrice;
	}

	public float getmActualPrice() {
		return NumericUtil.roundFloat(mActualPrice);
	}

	public void setmActualPrice(float mActualPrice) {
		this.mActualPrice = mActualPrice;
	}

	public float getmRepaidPrice() {
		return NumericUtil.roundFloat(mRepaidPrice);
	}

	public void setmRepaidPrice(float mRepaidPrice) {
		this.mRepaidPrice = mRepaidPrice;
	}
	
	public void setPaymentType(int payTypeVal){
		this.mPaymentType = PayType.valueOf(payTypeVal);
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("orderId", this.mId);
		jm.put("orderDateFormat", DateUtil.format(this.mOrderDate));
		jm.put("actualPrice", this.mActualPrice);
		jm.put("totalPrice", this.mTotalPrice);
		jm.put("repaidPrice", this.mRepaidPrice);
		jm.put("payTypeValue", this.mPaymentType.getVal());
		jm.put("payTypeText", this.mPaymentType.getDesc());
		jm.put("oldTotalPrice", NumericUtil.roundFloat(this.mTotalPrice - this.mRepaidPrice));
		jm.put("oldActualPrice", NumericUtil.roundFloat(this.mActualPrice - this.mRepaidPrice));
		jm.put("operateStaff", this.getStaff().getName());
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
	

}
