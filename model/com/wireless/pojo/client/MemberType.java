package com.wireless.pojo.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.distMgr.Discount;

public class MemberType implements Jsonable, Parcelable{
	
	public static final int MEMBER_TYPE_PARCELABLE_SIMPLE = 0;
	public static final int MEMBER_TYPE_PARCELABLE_COMPLEX = 1;
	
	public static enum Type{
		NORMAL(1, "普通"),
		WEIXIN(2, "微信");
		
		private final int val;
		private final String desc;
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(" + val + ") is invalid.");
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
	
	public static enum DiscountType{
		NORMAL(1, "普通"),
		DEFAULT(2, "默认");
		
		private final int val;
		private final String desc;
		
		DiscountType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static DiscountType valueOf(int val){
			for(DiscountType type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	public static enum Attribute{
		
		CHARGE(0, "充值"),		//充值
		POINT(1, "积分"),		//积分
		INTERESTED(2, "关注");
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
	
	//The helper class to insert a member type
	public static class InsertBuilder{
		private final int restaurantId;
		private final String name;
		private final Discount defaultDiscount;
		
		private Type type = Type.NORMAL;
		private List<Discount> discounts = new ArrayList<Discount>();
		private float exchangeRate = 1;
		private float chargeRate = 1;
		private Attribute attribute = Attribute.POINT;
		private int initialPoint = 0;

		public InsertBuilder(int restaurantId, String name, Discount defaultDiscount){
			this.restaurantId = restaurantId;
			this.name = name;
			discounts.add(defaultDiscount);
			this.defaultDiscount = defaultDiscount;
		}

		public InsertBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		public InsertBuilder addDiscount(Discount discount){
			if(!discounts.contains(discount)){
				discounts.add(discount);
			}
			return this;
		}
		
		public InsertBuilder setExchangeRate(float exchangeRate){
			if(exchangeRate < 0){
				throw new IllegalArgumentException();
			}else{
				this.exchangeRate = exchangeRate;
			}
			return this;
		}
		
		public InsertBuilder setChargeRate(float chargeRate){
			if(chargeRate < 0){
				throw new IllegalArgumentException();
			}else{
				this.chargeRate = chargeRate;
			}
			return this;
		}
		
		public InsertBuilder setAttribute(Attribute attribute){
			this.attribute = attribute;
			return this;
		}
		
		public InsertBuilder setInitialPoint(int initialPoint){
			this.initialPoint = initialPoint;
			return this;
		}
		
		public MemberType build(){
			return new MemberType(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		
		private String name;
		private Discount defaultDiscount;
		private List<Discount> discounts = new ArrayList<Discount>();
		private float exchangeRate = -1;
		private float chargeRate = -1;
		private Attribute attribute;
		private int initialPoint = -1;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public int getId(){
			return this.id;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public String getName(){
			return this.name;
		}
		
		public boolean isNameChanged(){
			return name != null;
		}
		
		public UpdateBuilder setDefaultDiscount(Discount discount){
			this.defaultDiscount = discount;
			addDiscount(discount);
			return this;
		}
		
		public Discount getDefaultDiscount(){
			return this.defaultDiscount;
		}
		
		public UpdateBuilder addDiscount(Discount discount){
			if(!discounts.contains(discount)){
				discounts.add(discount);
			}
			return this;
		}
		
		public List<Discount> getDiscounts(){
			return this.discounts;
		}
		
		public boolean isDiscountChanged(){
			return discounts.isEmpty();
		}
		
		public boolean isDefaultDiscountChanged(){
			return defaultDiscount != null;
		}
		
		public UpdateBuilder setExchangeRate(float exchangeRate){
			if(exchangeRate < 0){
				throw new IllegalArgumentException();
			}else{
				this.exchangeRate = exchangeRate;
			}
			return this;
		}
		
		public float getExchangeRate(){
			return exchangeRate;
		}
		
		public boolean isExchangRateChanged(){
			return exchangeRate >= 0;
		}
		
		public UpdateBuilder setChargeRate(float chargeRate){
			if(chargeRate < 0){
				throw new IllegalArgumentException();
			}else{
				this.chargeRate = chargeRate;
			}
			return this;
		}
		
		public float getChargeRate(){
			return chargeRate;
		}
		
		public boolean isChargeRateChanged(){
			return chargeRate >= 0;
		}
		
		public UpdateBuilder setAttribute(Attribute attribute){
			this.attribute = attribute;
			return this;
		}
		
		public Attribute getAttribute(){
			return this.attribute;
		}
		
		public boolean isAttributeChanged(){
			return attribute != null;
		}
		
		public UpdateBuilder setInitialPoint(int initialPoint){
			if(initialPoint < 0){
				throw new IllegalArgumentException();
			}else{
				this.initialPoint = initialPoint;
			}
			return this;
		}
		
		public int getInitialPoint(){
			return this.initialPoint;
		}
		
		public boolean isInitialPointChanged(){
			return initialPoint >= 0;
		}
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type = Type.NORMAL;
	private List<Discount> discounts = new ArrayList<Discount>();
	private Discount defaultDiscount = new Discount();
	private float exchangeRate;
	private float chargeRate;
	private Attribute attribute;
	private int initialPoint;
	
	
	private MemberType(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
		setName(builder.name);
		setType(builder.type);
		setDiscounts(builder.discounts);
		setDefaultDiscount(builder.defaultDiscount);
		setExchangeRate(builder.exchangeRate);
		setChargeRate(builder.chargeRate);
		setAttribute(builder.attribute);
		setInitialPoint(builder.initialPoint);
	}
	
	public MemberType(int id){
		setId(id);
	}
	
	public void copyFrom(MemberType src){
		if(src != null && src != this){
			setId(src.getId());
			setRestaurantId(src.getRestaurantId());
			setName(src.getName());
			setType(src.getType());
			setDiscounts(src.getDiscounts());
			setDefaultDiscount(src.getDefaultDiscount());
			setExchangeRate(src.getExchangeRate());
			setChargeRate(src.getChargeRate());
			setAttribute(src.getAttribute());
			setInitialPoint(src.getInitialPoint());
		}
	}
	
	public int getInitialPoint() {
		return initialPoint;
	}

	public void setInitialPoint(int initialPoint) {
		this.initialPoint = initialPoint;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int typeId) {
		this.id = typeId;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public String getName() {
		if(name == null){
			return "";
		}
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public List<Discount> getDiscounts(){
		return Collections.unmodifiableList(this.discounts);
	}
	
	public void setDiscounts(List<Discount> discounts){
		if(discounts != null){
			this.discounts.clear();
			this.discounts.addAll(discounts);
		}
	}
	
	public void addDiscount(Discount discount){
		if(!discounts.contains(discount)){
			discounts.add(discount);
		}
	}
	
	public Discount getDefaultDiscount() {
		return defaultDiscount;
	}
	
	public void setDefaultDiscount(Discount defaultDiscount) {
		addDiscount(defaultDiscount);
		this.defaultDiscount = defaultDiscount;
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
		return "member type(id = " + getId() + ", name = " + getName() + ")";
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getId();
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MemberType)){
			return false;
		}else{
			return getId() == ((MemberType)obj).getId();
		}
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.name);
		jm.put("type", this.type.getVal());
		jm.put("exchangeRate", this.exchangeRate);
		jm.put("chargeRate", this.chargeRate);
		jm.put("initialPoint", this.initialPoint);
//		if(this.discountType != null){
//			jm.put("discountTypeText", this.discountType.getDesc());			
//			jm.put("discountTypeValue", this.discountType.getVal());
//		}
		if(this.attribute != null){
			jm.put("attributeText", this.attribute.getDesc());			
			jm.put("attributeValue", this.attribute.getVal());
		}
		if(this.defaultDiscount != null){
			jm.put("discount", this.defaultDiscount);
		}
		if(!this.discounts.isEmpty()){
			jm.put("discounts", this.discounts);
		}
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == MEMBER_TYPE_PARCELABLE_SIMPLE){
			dest.writeInt(this.getId());
			dest.writeString(this.getName());
			
		}else if(flag == MEMBER_TYPE_PARCELABLE_COMPLEX){
			dest.writeInt(this.getId());
			dest.writeString(this.getName());
		}		
	}

	@Override
	public void createFromParcel(Parcel source) {
		int flag = source.readByte();
		if(flag == MEMBER_TYPE_PARCELABLE_SIMPLE){
			setId(source.readInt());
			setName(source.readString());
			
		}else if(flag == MEMBER_TYPE_PARCELABLE_COMPLEX){
			setId(source.readInt());
			setName(source.readString());
		}
	}

	public final static Parcelable.Creator<MemberType> CREATOR = new Parcelable.Creator<MemberType>() {
		
		public MemberType[] newInstance(int size) {
			return new MemberType[size];
		}
		
		public MemberType newInstance() {
			return new MemberType(0);
		}
	};
}
