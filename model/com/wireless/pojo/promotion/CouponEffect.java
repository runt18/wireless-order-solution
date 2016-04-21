package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class CouponEffect implements Jsonable{
	private int couponTypeId;        //优惠券类型
	private String beginDate;			 //开始时间
	private String endDate;			 //结束时间
	private int salesAmount;         //拉动消费次数
	private String couponName;		 //优惠活动名称
	private float couponPrice;	 	 //优惠活动面额
	private int issuedAmount; 		 //发送数量
	private float issuedPrice;		 //发送总价格
	private int usedAmount;			 //使用数量
	private float usedPrice;		 //共使用价格
	private float effectSales;		 //拉动消费额
	
	public CouponEffect(int couponTypeId){
		this.couponTypeId = couponTypeId;
	}
	
	public void setCouponTypeId(int CouponTypeId){
		this.couponTypeId = CouponTypeId;
	}
	
	public String getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(String beginDate){
		this.beginDate = beginDate;
	}

	public int getSalesAmount() {
		return salesAmount;
	}

	public void setSalesAmount(int salesAmount) {
		this.salesAmount = salesAmount;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCouponName() {
		return couponName;
	}

	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}

	public float getCouponPrice() {
		return couponPrice;
	}

	public void setCouponPrice(float couponPrice) {
		this.couponPrice = couponPrice;
	}

	public int getIssuedAmount() {
		return issuedAmount;
	}

	public void setIssuedAmount(int issuedAmount) {
		this.issuedAmount = issuedAmount;
	}

	public float getIssuedPrice() {
		return issuedPrice;
	}

	public void setIssuedPrice(float issuedPrice) {
		this.issuedPrice = issuedPrice;
	}

	public int getUsedAmount() {
		return usedAmount;
	}

	public void setUsedAmount(int usedAmount) {
		this.usedAmount = usedAmount;
	}

	public float getUsedPrice() {
		return usedPrice;
	}

	public void setUsedPrice(float usedPrice) {
		this.usedPrice = usedPrice;
	}

	public float getEffectSales() {
		return effectSales;
	}

	public void setEffectSales(float effectSales) {
		this.effectSales = effectSales;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("beginDate", this.beginDate);
		jm.putString("endDate", this.endDate);
		jm.putInt("couponTypeId", this.couponTypeId);
		jm.putString("couponName", this.couponName);
		jm.putFloat("couponPrice", this.couponPrice);
		jm.putInt("issuedAmount", this.issuedAmount);
		jm.putFloat("issuedPrice", this.issuedPrice);
		jm.putInt("usedAmount", this.usedAmount);
		jm.putFloat("usedPrice", this.usedPrice);
		jm.putInt("salesAmount", this.salesAmount);
		jm.putFloat("effectSales", this.effectSales);
		return jm;
	}

	@Override	
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int hashCode(){
		return this.couponTypeId * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null && !(obj instanceof CouponEffect)){
			return false;
		}else{
			return ((CouponEffect)obj).couponTypeId == this.couponTypeId;
		}
	}
	
	@Override
	public String toString(){
		return this.couponName;
	}

}
