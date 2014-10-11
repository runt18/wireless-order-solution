package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.Kitchen;

public class Discount implements Jsonable, Parcelable, Comparable<Discount>{
	
	public final static List<Discount> EMPTY_LIST = Collections.emptyList();
	
	public final static Discount EMPTY = new EmptyBuilder().build();
	
	public final static byte DISCOUNT_PARCELABLE_COMPLEX = 0;
	public final static byte DISCOUNT_PARCELABLE_SIMPLE = 1;
	
	//The helper class insert a '无折扣'
	public static class EmptyBuilder extends InsertBuilder{
		public final static String TAG = "无折扣";
		public EmptyBuilder(){
			super(TAG, Status.DEFAULT, Type.RESERVED);
			setRate(1);
		}
	}
	
	//The helper class to insert a new discount
	public static class InsertBuilder{
		private final String name;
		private Status status = Status.NORMAL;
		private final Type type;
		private float rate = 1;
		
		InsertBuilder(String name, Status status, Type type){
			this.name = name;
			this.status = status;
			this.type = type;
		}
		
		public InsertBuilder(String name){
			this.name = name;
			this.type = Type.NORMAL;
		}
		
		public InsertBuilder setDefault(){
			this.status = Status.DEFAULT;
			return this;
		}
		
		public InsertBuilder setRate(float rate){
			if(rate < 0 || rate > 1){
				throw new IllegalArgumentException("The initial rate must be ranged from 0 to 1.");
			}
			this.rate = rate;
			return this;
		}
		
		public float getRate(){
			return this.rate;
		}
		
		public Discount build(){
			return new Discount(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private String name;
		private Status status;
		
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
		
		public UpdateBuilder setDefault(){
			this.status = Status.DEFAULT;
			return this;
		}
		
		public boolean isStatusChanged(){
			return status != null;
		}
		
		public Discount build(){
			return new Discount(this);
		}
	}
	
	public static class UpdatePlanBuilder{
		private final int id;
		private final List<DiscountPlan> dps = new ArrayList<DiscountPlan>(); 
		
		public UpdatePlanBuilder(int id){
			this.id = id;
		}
		
		public UpdatePlanBuilder add(Kitchen kitchen, float rate){
			dps.add(new DiscountPlan(kitchen, rate));
			return this;
		}

		public Discount build(){
			return new Discount(this);
		}
	}
	
	public static enum Status{
		
		NORMAL(1, "normal"),							// 普通
		DEFAULT(2, "default");							// 默认
		
		private final int val;
		private final String desc;
		
		private Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "status(val = " + val + ", desc = " + desc + ")";
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.getVal() == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The discount status(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	public static enum Type{
		
		NORMAL(1, "normal"),	// 普通
		RESERVED(2, "reserved");// 保留
		
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
			throw new IllegalArgumentException("The discount type(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	private String name;
	private int id;
	private int restaurantId;
	private Status status = Status.NORMAL;
	private Type type = Type.NORMAL;
	private List<DiscountPlan> plans = new ArrayList<DiscountPlan>();
	
	private Discount(UpdatePlanBuilder builder){
		setId(builder.id);
		setPlans(builder.dps);
	}
	
	private Discount(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setStatus(builder.status);
	}
	
	private Discount(InsertBuilder builder){
		setName(builder.name);
		setStatus(builder.status);
		setType(builder.type);
	}
	
	public Discount(){
		
	}
	
	public Discount(int id){
		this.id = id;
	}
	
	public String getName(){
		if(name == null){
			return "";
		}
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id; 
	}
	
	public int getRestaurantId(){
		return restaurantId;
	}
	
	public void setRestaurantId(int restId){
		this.restaurantId = restId;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(int statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public void setPlans(List<DiscountPlan> plans) {
		if(plans != null){
			this.plans.clear();
			addPlans(plans);
		}
	}
	public List<DiscountPlan> getPlans(){
		return Collections.unmodifiableList(plans);
	}
	
	public void addPlans(List<DiscountPlan> plans){
		for(DiscountPlan plan : plans){
			addPlan(plan);
		}
	}
	
	public void addPlan(DiscountPlan plan){
		if(plan != null){
			plan.setDiscount(this);
			plans.add(plan);
		}
	}
	
	public boolean isDefault(){
		return this.status == Status.DEFAULT;
	}
	
	public boolean isReserved(){
		return this.type == Type.RESERVED;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Discount)){
			return false;
		}else{
			return this.id == ((Discount)obj).id;
		}
	}
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "discount(id = " + id + ", restaurant_id = " + restaurantId + ", name = " + getName() + ")";
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DISCOUNT_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == DISCOUNT_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeByte(this.status.getVal());
			dest.writeByte(this.type.getVal());
			dest.writeString(this.name);
			dest.writeParcelList(this.plans, 0);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == DISCOUNT_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == DISCOUNT_PARCELABLE_COMPLEX){
			this.id = source.readInt();
			this.status = Status.valueOf(source.readByte());
			this.type = Type.valueOf(source.readByte());
			this.name = source.readString();
			this.plans = source.readParcelList(DiscountPlan.DP_CREATOR);
		}
	}
	
	public final static Parcelable.Creator<Discount> CREATOR = new Parcelable.Creator<Discount>() {
		
		public Discount[] newInstance(int size) {
			return new Discount[size];
		}
		
		public Discount newInstance() {
			return new Discount();
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("name", this.name);
		jm.putInt("rid", this.restaurantId);
		jm.putInt("type", this.getType().getVal());
		//FIXME level not exist
		jm.putInt("level", 0);
		jm.putInt("status", this.status.getVal());
		jm.putBoolean("isDefault", this.isDefault());
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public int compareTo(Discount discount) {
		if(getId() > discount.getId()){
			return 1;
		}else if(getId() < discount.getId()){
			return -1;
		}else{
			return 0;
		}
	}

}
