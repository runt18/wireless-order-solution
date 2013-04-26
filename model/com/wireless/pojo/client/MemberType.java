package com.wireless.pojo.client;

import java.util.HashMap;
import java.util.Map;

import com.wireless.pojo.distMgr.Discount;

@SuppressWarnings("rawtypes")
public class MemberType {
	
	public static enum Attribute{
		
		CHARGE(0),		//充值
		POINT(1),		//积分
		COUPON(2);		//优惠
		
		private final int val;
		private Attribute(int val){
			this.val = val;
		}
		
		@Override
		public String toString(){
			if(this == CHARGE){
				return "member type attribute : charge(val = " + val + ")";
			}else if(this == POINT){
				return "member type attribute : point(val = " + val + ")";
			}else if(this == COUPON){
				return "member type attribute : coupon(val = " + val + ")";
			}else{
				return "member type attribute : unknown(val = " + val + ")";
			}
		}
		
		public static Attribute valueOf(int val){
			for(Attribute attr : values()){
				if(attr.val == val){
					return attr;
				}
			}
			throw new IllegalArgumentException("The attribute(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return val;
		}
	}
	
	public static final String OLD_DISCOUNTID_KEY = "OLD_DISCOUNTID_KEY";
	public static final int DISCOUNT_TYPE_DISCOUNT = 0;
	public static final int DISCOUNT_TYPE_ENTIRE = 1;
	
	private int typeID;
	private int restaurantID;
	private String name;
	private Discount discount;
	private int discountType;
	private float discountRate;
	private float chargeRate;
	private float exchangeRate;
	private Attribute attribute;
	private Map other;
	
	public MemberType(){
		this.other = new HashMap();
		this.discount = new Discount();
	}
	
	public int getTypeID() {
		return typeID;
	}
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Discount getDiscount() {
		return discount;
	}
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}
	public int getDiscountType() {
		return discountType;
	}
	public void setDiscountType(int discountType) {
		this.discountType = discountType;
	}
	public float getDiscountRate() {
		return discountRate;
	}
	public void setDiscountRate(float discountRate) {
		this.discountRate = discountRate;
	}
	public float getChargeRate() {
		return chargeRate;
	}
	public void setChargeRate(float chargeRate) {
		this.chargeRate = chargeRate;
	}
	public float getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(float exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	public Integer getAttributeValue() {
		return attribute != null ? attribute.getVal() : null;
	}
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(int attributeVal) {
		this.attribute = Attribute.valueOf(attributeVal);
	}
	public Map getOther() {
		return other;
	}
	public void setOther(Map other) {
		this.other = other;
	}
	
}
