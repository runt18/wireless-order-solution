package com.wireless.pojo.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.restaurantMgr.Restaurant;

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
	
	public static enum PriceType{
		NORMAL(1, "普通"),
		DEFAULT(2, "默认");
		
		private final int val;
		private final String desc;
		
		PriceType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static PriceType valueOf(int val){
			for(PriceType type : values()){
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
		POINT(1, "积分");		//积分
		
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
	
	public static class Discount4Chain{
		private final int branchId;
		private final List<Discount> discounts = new ArrayList<Discount>();
		private final Discount defaultDiscount;
		
		public Discount4Chain(Restaurant branch, Discount defaultDiscount){
			this(branch.getId(), defaultDiscount);
		}
		
		public Discount4Chain(int branchId, Discount defaultDiscount){
			this.branchId = branchId;
			this.discounts.add(defaultDiscount);
			this.defaultDiscount = defaultDiscount;
		}
		
		public Discount4Chain addDiscount(Discount discount){
			if(!this.discounts.contains(discount)){
				this.discounts.add(discount);
			}
			return this;
		}
		
		public List<Discount> getDiscounts(){
			return Collections.unmodifiableList(this.discounts);
		}
		
		public Discount getDefaultDiscount(){
			return this.defaultDiscount;
		}
		
		public int getBranchId(){
			return this.branchId;
		}
	}
	
	public static class Price4Chain{
		private final int branchId;
		private final List<PricePlan> prices = new ArrayList<PricePlan>();
		private final PricePlan defaultPrice;
		
		public Price4Chain(Restaurant branch, PricePlan defaultPrice){
			this(branch.getId(), defaultPrice);
		}
		
		public Price4Chain(int branchId, PricePlan defaultPrice){
			this.branchId = branchId;
			this.prices.add(defaultPrice);
			this.defaultPrice = defaultPrice;
		}
		
		public Price4Chain addPrice(PricePlan price){
			if(!this.prices.contains(price)){
				this.prices.add(price);
			}
			return this;
		}
		
		public List<PricePlan> getPrices(){
			return Collections.unmodifiableList(this.prices);
		}
		
		public PricePlan getDefaultPrice(){
			return this.defaultPrice;
		}
		
		public int getBranchId(){
			return this.branchId;
		}
	}
	
	//The helper class to insert a member type
	public static class InsertBuilder{
		private final String name;
		
		private Type type = Type.NORMAL;
		private Discount defaultDiscount;
		private final List<Discount> discounts = new ArrayList<Discount>();
		private PricePlan defaultPrice;
		private final List<PricePlan> prices = new ArrayList<PricePlan>();
		private float exchangeRate = 1;
		private float chargeRate = 1;
		private Attribute attribute = Attribute.POINT;
		private int initialPoint = 0;
		private String desc;

		private final List<Discount4Chain> chainDiscounts = new ArrayList<Discount4Chain>();

		private final List<Price4Chain> chainPrices = new ArrayList<Price4Chain>();
		
		public InsertBuilder(String name){
			this.name = name;
		}
		
		public InsertBuilder(String name, Discount defaultDiscount){
			this.name = name;
			this.discounts.add(defaultDiscount);
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

		public InsertBuilder setDefaultDiscount(Discount defaultDiscount){
			this.defaultDiscount = defaultDiscount;
			return addDiscount(defaultDiscount);
		}
		
		public InsertBuilder addPrice(PricePlan price){
			if(!prices.contains(price)){
				prices.add(price);
			}
			return this;
		}
		
		public InsertBuilder setDefaultPrice(PricePlan defaultPrice){
			this.defaultPrice = defaultPrice;
			addPrice(defaultPrice);
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
		
		public InsertBuilder setDesc(String desc){
			this.desc = desc;
			return this;
		}
		
		public InsertBuilder addChainDiscount(Discount4Chain chainDiscount){
			this.chainDiscounts.add(chainDiscount);
			return this;
		}
		
		public List<Discount4Chain> getChainDiscounts(){
			return Collections.unmodifiableList(this.chainDiscounts);
		}
		
		public InsertBuilder addChainPrice(Price4Chain chainPrice){
			this.chainPrices.add(chainPrice);
			return this;
		}
		
		public List<Price4Chain> getChainPrices(){
			return Collections.unmodifiableList(this.chainPrices);
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
		private PricePlan defaultPrice;
		private List<PricePlan> prices;
		private float exchangeRate = -1;
		private float chargeRate = -1;
		private Attribute attribute;
		private int initialPoint = -1;
		private String desc;
		
		private List<Discount4Chain> chainDiscounts;
		private List<Price4Chain> chainPrices;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return name != null;
		}
		
		public UpdateBuilder setDefaultDiscount(Discount discount){
			this.defaultDiscount = discount;
			addDiscount(discount);
			return this;
		}
		
		public UpdateBuilder addDiscount(Discount discount){
			if(!discounts.contains(discount)){
				discounts.add(discount);
			}
			return this;
		}
		
		public boolean isDiscountChanged(){
			return !discounts.isEmpty();
		}
		
		public boolean isDefaultDiscountChanged(){
			return defaultDiscount != null;
		}
		
		public UpdateBuilder setDefaultPrice(PricePlan defaultPrice){
			this.defaultPrice = defaultPrice;
			addPrice(defaultPrice);
			return this;
		}
		
		public UpdateBuilder addPrice(PricePlan price){
			if(prices == null){
				prices = new ArrayList<PricePlan>();
			}
			if(!prices.contains(price)){
				prices.add(price);
			}
			return this;
		}
		
		public UpdateBuilder setEmptyPrice(){
			prices = new ArrayList<PricePlan>();
			return this;
		}
		
		public boolean isPriceChanged(){
			return prices != null;
		}
		
		public boolean isDefaultPriceChanged(){
			return defaultPrice != null;
		}
		
		public UpdateBuilder setExchangeRate(float exchangeRate){
			if(exchangeRate < 0){
				throw new IllegalArgumentException();
			}else{
				this.exchangeRate = exchangeRate;
			}
			return this;
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
		
		public boolean isChargeRateChanged(){
			return chargeRate >= 0;
		}
		
		public UpdateBuilder setAttribute(Attribute attribute){
			this.attribute = attribute;
			return this;
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
		
		public boolean isInitialPointChanged(){
			return initialPoint >= 0;
		}
		
		public boolean isDescChanged(){
			return this.desc != null;
		}
		
		public UpdateBuilder setDesc(String desc){
			this.desc = desc;
			return this;
		}
		
		public boolean isChainDiscountChanged(){
			return this.chainDiscounts != null;
		}
		
		public UpdateBuilder clearChainDiscount(){
			if(this.chainDiscounts == null){
				this.chainDiscounts = new ArrayList<Discount4Chain>();
			}
			this.chainDiscounts.clear();
			return this;
		}
		
		public UpdateBuilder addChainDiscount(Discount4Chain chainDiscount){
			if(this.chainDiscounts == null){
				this.chainDiscounts = new ArrayList<Discount4Chain>();
			}
			this.chainDiscounts.add(chainDiscount);
			return this;
		}
		
		public List<Discount4Chain> getChainDiscounts(){
			return Collections.unmodifiableList(this.chainDiscounts);
		}
		
		public UpdateBuilder clearChainPrice(){
			if(this.chainPrices == null){
				this.chainPrices = new ArrayList<Price4Chain>();
			}
			return this;
		}
		
		public UpdateBuilder addChainPrice(Price4Chain chainPrice){
			if(this.chainPrices == null){
				this.chainPrices = new ArrayList<Price4Chain>();
			}
			this.chainPrices.add(chainPrice);
			return this;
		}
		
		public List<Price4Chain> getChainPrices(){
			return Collections.unmodifiableList(this.chainPrices);
		}
		
		public boolean isChainPricesChanged(){
			return this.chainPrices != null;
		}
		
		public MemberType build(){
			return new MemberType(this);
		}
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type = Type.NORMAL;
	private List<Discount> discounts = new ArrayList<Discount>();
	private Discount defaultDiscount;
	private List<PricePlan> prices = new ArrayList<PricePlan>();
	private PricePlan defaultPrice;
	private float exchangeRate;
	private float chargeRate;
	private Attribute attribute;
	private int initialPoint;
	private String desc;
	
	private MemberType(InsertBuilder builder){
		setName(builder.name);
		setType(builder.type);
		setDiscounts(builder.discounts);
		setDefaultDiscount(builder.defaultDiscount);
		setPrices(builder.prices);
		setDefaultPrice(builder.defaultPrice);
		setExchangeRate(builder.exchangeRate);
		setChargeRate(builder.chargeRate);
		setAttribute(builder.attribute);
		setInitialPoint(builder.initialPoint);
		setDesc(builder.desc);
	}
	
	private MemberType(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setDiscounts(builder.discounts);
		setDefaultDiscount(builder.defaultDiscount);
		setPrices(builder.prices);
		setDefaultPrice(builder.defaultPrice);
		setExchangeRate(builder.exchangeRate);
		setChargeRate(builder.chargeRate);
		setAttribute(builder.attribute);
		setInitialPoint(builder.initialPoint);
		setDesc(builder.desc);
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
			setPrices(src.getPrices());
			setDefaultPrice(src.getDefaultPrice());
			setExchangeRate(src.getExchangeRate());
			setChargeRate(src.getChargeRate());
			setAttribute(src.getAttribute());
			setInitialPoint(src.getInitialPoint());
			setDesc(src.getDesc());
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
	
	public void setType(int type){
		this.type = Type.valueOf(type);
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
		if(discount != null && !discounts.contains(discount)){
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

	public boolean hasDefaultDiscount(){
		return this.defaultDiscount != null;
	}
	
	public void setPrices(List<PricePlan> plans){
		if(plans != null){
			this.prices.clear();
			this.prices.addAll(plans);
		}
	}

	public List<PricePlan> getPrices(){
		return Collections.unmodifiableList(prices);
	}
	
	public void addPricePlan(PricePlan plan){
		if(!prices.contains(plan)){
			prices.add(plan);
		}
	}
	
	public void setDefaultPrice(PricePlan defaultPrice){
		this.defaultPrice = defaultPrice;
	}
	
	public PricePlan getDefaultPrice(){
		return this.defaultPrice;
	}
	
	public boolean hasDefaultPrice(){
		return this.defaultPrice != null;
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
	
	public boolean isPoint(){
		return this.attribute == Attribute.POINT;
	}
	
	public boolean isCharge(){
		return this.attribute == Attribute.CHARGE;
	}
	
	public void setDesc(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){
		if(desc == null){
			return "";
		}
		return desc;
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("rid", this.restaurantId);
		jm.putString("name", this.name);
		jm.putInt("type", this.type.getVal());
		jm.putFloat("exchangeRate", this.exchangeRate);
		jm.putFloat("chargeRate", this.chargeRate);
		jm.putInt("initialPoint", this.initialPoint);
		jm.putString("desc", this.desc);
//		if(this.discountType != null){
//			jm.put("discountTypeText", this.discountType.getDesc());			
//			jm.put("discountTypeValue", this.discountType.getVal());
//		}
		if(this.attribute != null){
			jm.putString("attributeText", this.attribute.getDesc());			
			jm.putInt("attributeValue", this.attribute.getVal());
		}
		jm.putJsonable("discount", this.defaultDiscount, 0);
		jm.putJsonableList("discounts", this.discounts, 0);
		jm.putJsonableList("pricePlans", this.prices, flag);
		jm.putJsonable("pricePlan", this.defaultPrice, flag);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
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
