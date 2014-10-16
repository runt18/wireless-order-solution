package com.wireless.pojo.menuMgr;


public class PricePlan implements Comparable<PricePlan> {

	public static class InsertBuilder{
		private final Type type;
		private final String name;
		
		public InsertBuilder(Type type, String name){
			this.type = type;
			this.name = name;
		}
		
		public InsertBuilder(String name){
			this.type = Type.NORMAL;
			this.name = name;
		}
		
		public PricePlan build(){
			return new PricePlan(this);
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
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type;
	
	public PricePlan(int id){
		this.id = id;
	}

	private PricePlan(InsertBuilder builder){
		setType(builder.type);
		setName(builder.name);
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
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PricePlan)){
			return false;
		}else{
			return id == ((PricePlan)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return this.name;
	}

	@Override
	public int compareTo(PricePlan other) {
		if(id > other.id){
			return 1;
		}else if(id < other.id){
			return -1;
		}else{
			return 0;
		}
	}
	
}
