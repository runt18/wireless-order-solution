package com.wireless.pojo.tasteMgr;


public class TasteCategory {

	public static class InsertBuilder{
		private final int restaurantId;
		private final String name;
		private Type type = Type.NORMAL;
		
		public InsertBuilder(int restaurantId, String name){
			this.restaurantId = restaurantId;
			this.name = name;
		}
		
		public InsertBuilder setType(Type type){
			this.type = type;
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
		NORMAL(0, "一般"),
		RESERVED(1, "保留");
		
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
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type;
	
	private TasteCategory(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
	}
	
	private TasteCategory(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
		setName(builder.name);
		setType(builder.type);
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
}
