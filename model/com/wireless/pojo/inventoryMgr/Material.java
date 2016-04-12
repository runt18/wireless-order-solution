package com.wireless.pojo.inventoryMgr;

import java.text.DecimalFormat;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class Material implements Jsonable {
	
	public enum Status{
		NORMAL(1, "正常"),
		STOP(2, "停用"),
		WARNING(3, "预警");
		
		private int value;
		private String text;
		Status(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		public static Status valueOf(int value){
			for(Status temp : values()){
				if(temp.getValue() == value){
					return temp;
				}
			}
			throw new IllegalArgumentException("The status value(val = " + value + ") passed is invalid.");
		}
	}
	
	private int id;
	private int restaurantId;
	private MaterialCate cate;
	private float price;
	private float delta;
	private float stock;
	private String name;
	private String lastModStaff;
	private long lastModDate;
	private Status status;
	private String pinyin;
	private boolean isGood = false;
	
	void init(int id, int restaurantId, int cateId, float price, float stock, String name, String lastModStaff, long lastModDate, Status status){
		this.id = id;
		this.restaurantId = restaurantId;
		this.cate = new MaterialCate(cateId, restaurantId, "");
		this.price = price;
		this.stock = stock;
		this.name = name;
		this.lastModStaff = lastModStaff;
		this.lastModDate = lastModDate;
		this.status = status;
	}
	public Material(){}
	
	/**
	 * update basic model
	 * @param id
	 * @param restaurantId
	 * @param cateId
	 * @param name
	 * @param lastModStaff
	 */
	public Material(int id, int restaurantId, int cateId, String name, String lastModStaff){
		init(id, restaurantId, cateId, 0, 0, name, lastModStaff, 0, Status.NORMAL);
	}
	
	/**
	 * insert basic model
	 * @param restaurantId
	 * @param name
	 * @param cateId
	 * @param lastModStaff
	 * @param status
	 */
	public Material(int restaurantId, String name, int cateId, String lastModStaff, int status){
		init(id, restaurantId, cateId, 0, 0, name, lastModStaff, 0, Status.valueOf(status));
	}
	
	public Material(MonthlyChangeTypeUpdateBuilder builder){
		setId(builder.id);
		setDelta(builder.delta);
		setLastModStaff(builder.lastModStaff);
		setRestaurantId(builder.restaurantId);
	}
	
	public Material(MonthlyUpdateBuilder builder){
		setId(builder.id);
		setDelta(builder.delta);
		setPrice(builder.price);
		setLastModStaff(builder.lastModStaff);
		setRestaurantId(builder.restaurantId);
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
	public MaterialCate getCate() {
		return cate;
	}
	
	public void setCate(MaterialCate cate) {
		this.cate = cate;
	}
	
	public void setCate(int id, String name) {
		this.cate = new MaterialCate(id, this.restaurantId, name);
	}
	
	public void setCate(int id, String name, int type) {
		this.cate = new MaterialCate(id, this.restaurantId, name, MaterialCate.Type.valueOf(type));
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public float getDelta() {
		return delta;
	}
	
	public void setDelta(float delta) {
		this.delta = delta;
	}
	
	public float getStock() {
		return stock;
	}
	
	public void setStock(float stock) {
		this.stock = stock;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLastModStaff() {
		return lastModStaff;
	}
	
	public void setLastModStaff(String lastModStaff) {
		this.lastModStaff = lastModStaff;
	}
	
	public long getLastModDate() {
		return lastModDate;
	}
	
	public void setLastModDate(long lastModDate) {
		this.lastModDate = lastModDate;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatus(int status) {
		this.status = Status.valueOf(status);
	}
	
	public String getPinyin() {
		return pinyin;
	}
	
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	
	public void plusStock(float count){
		this.stock = stock + count;
	}
	
	public void cutStock(float count){
		this.stock = stock - count;
	}
	
	
	public boolean isGood() {
		return isGood;
	}
	public void setGood(boolean isGood) {
		this.isGood = isGood;
	}
	public void stockInAvgPrice(float detailPrice, float detailAmount){
		float avgPrice = (this.stock * this.price + detailPrice * detailAmount) / (this.stock + detailAmount);
		this.price = (float)(Math.round(avgPrice * 100)) / 100;
	}
	
	public void stockOutAvgPrice(float detailPrice, float detailAmount){
		float avgPrice = (this.stock * this.price - detailPrice * detailAmount) / (this.stock - detailAmount);
		this.price = (float)(Math.round(avgPrice * 100)) / 100;;
	}
	
	
	public static class MonthlyChangeTypeUpdateBuilder{
		private final int id;
		
		private int restaurantId;
		private float delta;
		private String lastModStaff;
		public int getRestaurantId() {
			return restaurantId;
		}
		public MonthlyChangeTypeUpdateBuilder setRestaurantId(int restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}
		public float getDelta() {
			return delta;
		}
		public MonthlyChangeTypeUpdateBuilder setDelta(float delta) {
			this.delta = delta;
			return this;
		}
		public String getLastModStaff() {
			return lastModStaff;
		}
		public MonthlyChangeTypeUpdateBuilder setLastModStaff(String lastModStaff) {
			this.lastModStaff = lastModStaff;
			return this;
		}
		public int getId() {
			return id;
		}
		
		public MonthlyChangeTypeUpdateBuilder(int id){
			this.id = id;
		}
		
		public Material build(){
			return new Material(this);
		}
		
	}
	
	public static class MonthlyUpdateBuilder{
		private final int id;
		
		private int restaurantId;
		private float delta;
		private float price;
		private String lastModStaff;
		public int getRestaurantId() {
			return restaurantId;
		}
		public MonthlyUpdateBuilder setRestaurantId(int restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}
		public float getDelta() {
			return delta;
		}
		public MonthlyUpdateBuilder setDelta(float delta) {
			this.delta = delta;
			return this;
		}
		public MonthlyUpdateBuilder setPrice(float price){
			this.price = price;
			return this;
		}
		public String getLastModStaff() {
			return lastModStaff;
		}
		public MonthlyUpdateBuilder setLastModStaff(String lastModStaff) {
			this.lastModStaff = lastModStaff;
			return this;
		}
		public int getId() {
			return id;
		}
		
		public MonthlyUpdateBuilder(int id){
			this.id = id;
		}
		
		public Material build(){
			return new Material(this);
		}
		
	}
	
	@Override
	public String toString() {
		return "id=" + this.getId() + ", name=" + this.getName() + ", stock=" + this.getStock();
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		DecimalFormat df = new DecimalFormat("0.00");
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.getId());
		jm.putString("pinyin", this.getPinyin());
		jm.putInt("rid", this.getRestaurantId());
		jm.putString("price", df.format(this.getPrice()));
		jm.putString("presentPrice", df.format(this.getPrice() + this.getDelta()));
		jm.putFloat("delta", this.getDelta());
		jm.putString("name", this.getName());
		jm.putFloat("stock", this.getStock());
		jm.putString("lastModStaff", this.getLastModStaff());
		jm.putLong("lastModDate", this.getLastModDate());
		jm.putString("lastModDateFormat", DateUtil.format(this.getLastModDate()));
		jm.putString("pinyin", this.pinyin);
		if(this.cate != null){
			jm.putInt("cateId", this.getCate().getId());
			jm.putString("cateName", this.getCate().getName());
			jm.putInt("cateType", this.getCate().getType().getValue());
		}
		if(this.status != null){
			jm.putInt("statusValue", this.getStatus().getValue());
			jm.putString("statusText", this.getStatus().getText());
		}
		//记录当前价格是否修改
		jm.putBoolean("changed", this.delta != 0 ? true : false);
		jm.putBoolean("isGood", this.isGood);
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Material)){
			return false;
		}else{
			return id == ((Material)obj).id && restaurantId == ((Material)obj).restaurantId;
		}
	}
	
}
