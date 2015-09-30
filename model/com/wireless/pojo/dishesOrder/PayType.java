package com.wireless.pojo.dishesOrder;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class PayType implements Parcelable, Comparable<PayType>, Jsonable{

	private static class ReservedType extends PayType{
		public ReservedType(int id, String name, Type type) {
			super(id);
			super.name = name;
			super.type = type;
		}
		@Override
		public void setName(String name){
			throw new UnsupportedOperationException();
		}
		@Override
		public void setType(Type type){
			throw new UnsupportedOperationException();
		}
		@Override
		public void setId(int id){
			throw new UnsupportedOperationException();
		}
	}
	
	public final static PayType CASH = new ReservedType(1, "现金", Type.DESIGNED);
	public final static PayType CREDIT_CARD = new ReservedType(2, "刷卡", Type.DESIGNED);
	public final static PayType MEMBER = new ReservedType(3, "会员卡", Type.MEMBER);
	public final static PayType SIGN = new ReservedType(4, "签单", Type.DESIGNED);
	public final static PayType HANG = new ReservedType(5, "挂账", Type.DESIGNED);
	public final static PayType WX = new ReservedType(6, "微信支付", Type.DESIGNED);
	public final static PayType MIXED = new ReservedType(100, "混合结账", Type.MIXED);
	
	public static class InsertBuilder{
		private final String name;
		private Type type = Type.EXTRA;
		
		public InsertBuilder(String name){
			this.name = name;
		}
		
		public PayType build(){
			return new PayType(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private String name;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.name != null;
		}
		
		public PayType build(){
			return new PayType(this);
		}
		
	}
	
	public static enum Type{
		
		EXTRA(1, "自定义"),		 // 自定义
		DESIGNED(2, "预定义"), 	 // 预定义
		MEMBER(3, "会员"),		 // 会员余额
		MIXED(4, "混合结账");	 // 混合结账
		
		private final int val;
		private final String desc;
		
		private Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.getVal() == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type;
	
	public PayType(int id){
		this.id = id;
	}
	
	private PayType(InsertBuilder builder){
		this.name = builder.name;
		this.type = builder.type;
	}
	
	private PayType(UpdateBuilder builder){
		this.id = builder.id;
		this.name = builder.name;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
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
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean isCash(){
		return this.equals(CASH);
	}
	
	public boolean isCreditCard(){
		return this.equals(CREDIT_CARD);
	}
	
	public boolean isMember(){
		return this.equals(MEMBER);
	}
	
	public boolean isSign(){
		return this.equals(SIGN);
	}
	
	public boolean isHang(){
		return this.equals(HANG);
	}
	
	public boolean isMixed(){
		return this.equals(MIXED);
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PayType)){
			return false;
		}else{
			return this.id == ((PayType)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return this.name;
	}

	public final static int PAY_TYPE_PARCELABLE_SIMPLE = 0;
	public final static int PAY_TYPE_PARCELABLE_COMPLEX = 1;
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == PAY_TYPE_PARCELABLE_SIMPLE){
			dest.writeInt(getId());
			
		}else if(flag == PAY_TYPE_PARCELABLE_COMPLEX){
			dest.writeInt(getId());
			dest.writeString(getName());
			dest.writeByte(type.getVal());
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		int flag = source.readByte();
		if(flag == PAY_TYPE_PARCELABLE_SIMPLE){
			setId(source.readInt());
			
		}else if(flag == PAY_TYPE_PARCELABLE_COMPLEX){
			setId(source.readInt());
			setName(source.readString());
			setType(Type.valueOf(source.readByte()));
		}
	}
	
	public final static Parcelable.Creator<PayType> CREATOR = new Parcelable.Creator<PayType>() {
		
		@Override
		public PayType[] newInstance(int size) {
			return new PayType[size];
		}
		
		@Override
		public PayType newInstance() {
			return new PayType(0);
		}
	};

	@Override
	public int compareTo(PayType other) {
		if(id < other.id){
			return -1;
		}else if(id > other.id){
			return 1;
		}else{
			return 0;
		}
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("name", this.name);
		jm.putInt("typeValue", this.getType().getVal());
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
