package com.wireless.pojo.billStatistics;

public class IncomeByCoupon {
	
	private int mCouponAmount;		//优惠券使用账单数
	private float mTotalCoupon;		//合计优惠券使用金额
	
	public IncomeByCoupon(){
		
	}

	public IncomeByCoupon(int couponAmount, float totalCoupon){
		setCouponAmount(couponAmount);
		setTotalCoupon(totalCoupon);
	}
	
	public int getCouponAmount() {
		return mCouponAmount;
	}

	public void setCouponAmount(int mCouponAmount) {
		this.mCouponAmount = mCouponAmount;
	}

	public float getTotalCoupon() {
		return mTotalCoupon;
	}

	public void setTotalCoupon(float totalCoupon) {
		this.mTotalCoupon = totalCoupon;
	}
}
