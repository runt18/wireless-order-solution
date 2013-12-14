package com.wireless.pojo.tasteMgr;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class TasteCategory implements Parcelable{

	public final static byte TASTE_CATE_PARCELABLE_COMPLEX = 0;
	public final static byte TASTE_CATE_PARCELABLE_SIMPLE = 1;
	
	public static class SpecInsertBuilder extends InsertBuilder{
		public final static String NAME = "规格";
		public SpecInsertBuilder(int restaurantId){
			super(restaurantId, NAME);
			setType(Type.RESERVED);
			setStatus(Status.SPEC);
		}
	}
	
	public static class InsertBuilder{
		private final int restaurantId;
		private final String name;
		private Type type = Type.NORMAL;
		private Status status = Status.TASTE;
		
		public InsertBuilder(int restaurantId, String name){
			this.restaurantId = restaurantId;
			this.name = name;
		}
		
		InsertBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		InsertBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public TasteCategory build(){
			return new TasteCategory(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private final String name;
		
		public UpdateBuilder(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public TasteCategory build(){
			return new TasteCategory(this);
		}
	}
	
	/**
	 * The type to taste
	 */
	public static enum Type{
		NORMAL(1, "一般"),
		RESERVED(2, "保留");
		
		private final int val;
		private final String desc;
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "taste type(val = " + val + ",desc = " + desc + ")";
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	public static enum Status{
		SPEC(1, "规格"),
		TASTE(2, "口味");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
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
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type;
	private Status status;
	
	private TasteCategory(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
	}
	
	private TasteCategory(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
		setName(builder.name);
		setType(builder.type);
		setStatus(builder.status);
	}
	
	private TasteCategory(){
		
	}
	
	public TasteCategory(int id){
		this.id = id;
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
	
	public Status getStatus(){
		return this.status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public boolean isTaste(){
		return status == Status.TASTE;
	}
	
	public boolean isSpec(){
		return status == Status.SPEC;
	}
	
	@Override 
	public int hashCode(){
		return 17 * 31 + getId();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof TasteCategory)){
			return false;
		}else{
			return getId() == ((TasteCategory)obj).getId();
		}
	}

	@Override
	public String toString(){
		return "taste category(id=" + getId() + ",name=" + getName() + ")";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TASTE_CATE_PARCELABLE_SIMPLE){
			dest.writeInt(getId());
			
		}else if(flag == TASTE_CATE_PARCELABLE_COMPLEX){
			dest.writeInt(getId());
			dest.writeInt(getRestaurantId());
			dest.writeString(getName());
			dest.writeInt(getType().getVal());
			dest.writeInt(getStatus().getVal());
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TASTE_CATE_PARCELABLE_SIMPLE){
			setId(source.readInt());
			
		}else if(flag == TASTE_CATE_PARCELABLE_COMPLEX){
			setId(source.readInt());
			setRestaurantId(source.readInt());
			setName(source.readString());
			setType(Type.valueOf(source.readInt()));
			setStatus(Status.valueOf(source.readInt()));
		}
	}
	
	public final static Parcelable.Creator<TasteCategory> CREATOR = new Parcelable.Creator<TasteCategory>() {
		
		public TasteCategory[] newInstance(int size) {
			return new TasteCategory[size];
		}
		
		public TasteCategory newInstance() {
			return new TasteCategory();
		}
	};
}
