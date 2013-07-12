package com.wireless.pojo.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.distMgr.Discount;

public class MemberType implements Jsonable{
	
	public static enum Attribute{
		
		CHARGE(0, "充值"),		//充值
		POINT(1, "积分");		//积分
		//COUPON(2);			//优惠
		
		private final int val;
		
		private final String desc;
		
		private Attribute(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "member type attribute : charge(val = " + val + ",desc = " + desc + ")";
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
		
		public String getDesc(){
			return this.desc;
		}
	}
	
	public static enum DiscountType{
		DISCOUNT_PLAN(0, "折扣方案"),
		DISCOUNT_ENTIRE(1, "全单折扣");
		
		private final int val;
		private final String desc;
		
		DiscountType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "discount type(val = " + val + ",desc = " + desc + ")";
		}
		
		public static DiscountType valueOf(int val){
			for(DiscountType type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The discount type(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	private int typeId;
	private int restaurantId;
	private String name;
	private Discount discount;
	private DiscountType discountType;
	private float discountRate;
	private float exchangeRate;
	private float chargeRate;
	private Attribute attribute;
	private int initialPoint;
	
	
	public MemberType(){
		this.discount = new Discount();
	}
	
	public int getInitialPoint() {
		return initialPoint;
	}

	public void setInitialPoint(int initialPoint) {
		this.initialPoint = initialPoint;
	}
	
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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
	public DiscountType getDiscountType() {
		return discountType;
	}
	
	public void setDiscountType(DiscountType discountType){
		this.discountType = discountType;
	}
	
	public void setDiscountType(int val) {
		this.discountType = DiscountType.valueOf(val);
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

	public Attribute getAttribute() {
		return this.attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	public void setAttribute(int attributeVal) {
		this.attribute = Attribute.valueOf(attributeVal);
	}
	
	@Override
	public String toString(){
		return "member type(id = " + getTypeId() + ", name = " + getName() + ")";
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getTypeId();
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MemberType)){
			return false;
		}else{
			return getTypeId() == ((MemberType)obj).getTypeId();
		}
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.typeId);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.name);
		jm.put("discountRate", this.discountRate);
		jm.put("exchangeRate", this.exchangeRate);
		jm.put("chargeRate", this.chargeRate);
		jm.put("initialPoint", this.initialPoint);
		if(this.discountType != null){
			jm.put("discountTypeText", this.discountType.getDesc());			
			jm.put("discountTypeValue", this.discountType.getVal());
		}
		if(this.attribute != null){
			jm.put("attributeText", this.attribute.getDesc());			
			jm.put("attributeValue", this.attribute.getVal());
		}
		if(this.discount != null){
			jm.put("discount", this.discount);
		}
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

}
