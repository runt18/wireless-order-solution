package com.wireless.pojo.inventoryMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	private float stock;
	private String name;
	private String lastModStaff;
	private long lastModDate;
	private Status status;
	private String pinyin;
	
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
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
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
	
	
	public void stockInAvgPrice(float detailPrice, float detailAmount){
		float avgPrice = (this.stock * this.price + detailPrice * detailAmount) / (this.stock + detailAmount);
		this.price = (float)(Math.round(avgPrice * 100)) / 100;
	}
	
	public void stockOutAvgPrice(float detailPrice, float detailAmount){
		float avgPrice = (this.stock * this.price - detailPrice * detailAmount) / (this.stock - detailAmount);
		this.price = (float)(Math.round(avgPrice * 100)) / 100;;
	}
	
	
	@Override
	public String toString() {
		return "id=" + this.getId() + ", name=" + this.getName() + ", stock=" + this.getStock();
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.getId());
		jm.put("pinyin", this.getPinyin());
		jm.put("rid", this.getRestaurantId());
		jm.put("price", this.getPrice());
		jm.put("name", this.getName());
		jm.put("stock", this.getStock());
		jm.put("lastModStaff", this.getLastModStaff());
		jm.put("lastModDate", this.getLastModDate());
		jm.put("lastModDateFormat", DateUtil.format(this.getLastModDate()));
		jm.put("pinyin", this.pinyin);
		if(this.cate != null){
			jm.put("cateId", this.getCate().getId());
			jm.put("cateName", this.getCate().getName());
		}
		if(this.status != null){
			jm.put("statusValue", this.getStatus().getValue());
			jm.put("statusText", this.getStatus().getText());
		}
		
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
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
