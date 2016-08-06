package com.wireless.pojo.inventoryMgr;

import java.text.DecimalFormat;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class Material implements Jsonable {
	
	public static class InsertBuilder{
		private MaterialCate cate;
		private float price;
		private float delta;
		private float stock;
		private String name;
		private String lastModStaff;
		private long lastModDate;
		private String pinyin;
		private boolean isGood = false;
		private int associateId;
		private Float minAlarmAmount;
		private Float maxAlarmAmount;
		
		public InsertBuilder(){}
		
		public InsertBuilder setMinAlarmAmount(Float minAlarmAmount){
			this.minAlarmAmount = minAlarmAmount;
			return this;
		}

		public InsertBuilder setMaxAlarmAmount(Float maxAlarmAmount){
			this.maxAlarmAmount = maxAlarmAmount;
			return this;
		}
		
		public InsertBuilder setMaterialCate(MaterialCate cate){
			this.cate = cate;
			return this;
		}
		
		public InsertBuilder setMaterialCate(int materialCate){
			this.cate = new MaterialCate(materialCate);
			return this;
		}
		
		public InsertBuilder setAssociateId(int associateId){
			this.associateId = associateId;
			return this;
		}
		
		public InsertBuilder setPrice(float price){
			this.price = price;
			return this;
		}
		
		public InsertBuilder setDelta(float delta){
			this.delta = delta;
			return this;
		}
		
		public InsertBuilder setStock(float stock){
			this.stock = stock;
			return this;
		}
		
		public InsertBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public InsertBuilder setLastModStaff(String staffName){
			this.lastModStaff = staffName;
			return this;
		}
		
		public InsertBuilder setLastModDate(long date){
			this.lastModDate = date;
			return this;
		}
		
		public InsertBuilder setPinYin(String pinYin){
			this.pinyin = pinYin;
			return this;
		}
		
		public InsertBuilder setIsGood(boolean isGood){
			this.isGood = isGood;
			return this;
		}
		
		public Material build(){
			return new Material(this);
		}
		
	}
	
	public static class UpdateBuilder{
		private int id;
		private MaterialCate cate;
		private float price;
		private float delta;
		private float stock;
		private String name;
		private String lastModStaff;
		private long lastModDate;
		private String pinyin;
		private boolean isGood = false;
		private boolean isStockChanged = false;
		private Float minAlarmAmount;
		private Float MaxAlarmAmount;
		
		public UpdateBuilder setMinAlarmAmount(Float minAlarmAmount){
			this.minAlarmAmount = minAlarmAmount;
			return this;
		}
		
		public UpdateBuilder setMaxAlarmAmount(Float maxAlarmAmount){
			this.MaxAlarmAmount = maxAlarmAmount;
			return this;
		}
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public boolean isStockOperation(){
			return this.isStockChanged;
		}
		
		public UpdateBuilder setIsStockOperation(boolean isStockChanged){
			this.isStockChanged = isStockChanged;
			return this;
		}
		
		public boolean hasMinAlarmChanged(){
			return this.minAlarmAmount != null;
		}
		
		public boolean hasMaxAlarmChanged(){
			return this.MaxAlarmAmount != null;
		}
		
		public boolean hasNameChanged(){
			return this.name != null && this.name.length() != 0;
		} 
		
		public boolean hasCateChanged(){
			return this.cate != null;
		}
		
		public boolean hasPriceChanged(){
			return this.price != 0;
		}
		
		public boolean hasStockChanged(){
			return this.stock != 0;
		}
		
		public UpdateBuilder setMaterialCate(MaterialCate cate){
			this.cate = cate;
			return this;
		}
		
		public UpdateBuilder setPrice(float price){
			this.price = price;
			return this;
		}
		
		public UpdateBuilder setDelta(float delta){
			this.delta = delta;
			return this;
		}
		
		public UpdateBuilder setStock(float stock){
			this.stock = stock;
			return this;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public UpdateBuilder setLastModStaff(String staffName){
			this.lastModStaff = staffName;
			return this;
		}
		
		public UpdateBuilder setLastModDate(long date){
			this.lastModDate = date;
			return this;
		}
		
		public UpdateBuilder setPinYin(String pinYin){
			this.pinyin = pinYin;
			return this;
		}
		
		public UpdateBuilder setIsGood(boolean isGood){
			this.isGood = isGood;
			return this;
		}
		
		public Material build(){
			return new Material(this);
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
	private String pinyin;
	private boolean isGood = false;
	private int associateId;
	private Float minAlarmAmount;
	private Float maxAlarmAmount;
	
	public Material(){}

	public Material(int id){
		this.id = id;
	}
	
	public Material(InsertBuilder builder){
		this.cate = builder.cate;
		this.price = builder.price;
		this.delta = builder.delta;
		this.stock = builder.stock;
		this.name = builder.name;
		this.lastModStaff = builder.lastModStaff;
		this.lastModDate = builder.lastModDate;
		this.pinyin = builder.pinyin;
		this.isGood = builder.isGood;
		this.associateId = builder.associateId;
		this.minAlarmAmount = builder.minAlarmAmount;
		this.maxAlarmAmount = builder.maxAlarmAmount;
	}
	
	public Material(UpdateBuilder builder){
		this.id = builder.id;
		this.cate = builder.cate;
		this.price = builder.price;
		this.delta = builder.delta;
		this.stock = builder.stock;
		this.name = builder.name;
		this.lastModStaff = builder.lastModStaff;
		this.lastModDate = builder.lastModDate;
		this.pinyin = builder.pinyin;
		this.isGood = builder.isGood;
		this.minAlarmAmount = builder.minAlarmAmount;
		this.maxAlarmAmount = builder.MaxAlarmAmount;
	}
	
	/**
	 * update basic model
	 * @param id
	 * @param restaurantId
	 * @param cateId
	 * @param name
	 * @param lastModStaff
	 */
	
	/**
	 * insert basic model
	 * @param restaurantId
	 * @param name
	 * @param cateId
	 * @param lastModStaff
	 * @param status
	 */
	
	public Material(MonthlyChangeTypeUpdateBuilder builder){
		setId(builder.id);
		setDelta(builder.delta);
		setLastModStaff(builder.lastModStaff);
	}
	
	public Material(MonthlyUpdateBuilder builder){
		setId(builder.id);
		setDelta(builder.delta);
		setPrice(builder.price);
		setLastModStaff(builder.lastModStaff);
		setRestaurantId(builder.restaurantId);
	}
	
	public Float getMinAlarmAmount() {
		return minAlarmAmount;
	}

	public void setMinAlarmAmount(Float minAlarmAmount) {
		this.minAlarmAmount = minAlarmAmount;
	}

	public Float getMaxAlarmAmount() {
		return maxAlarmAmount;
	}

	public void setMaxAlarmAmount(Float maxAlarmAmount) {
		this.maxAlarmAmount = maxAlarmAmount;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAssociateId(){
		return this.associateId;
	}
	public void setAssociateId(int associateId){
		this.associateId = associateId;
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
		this.cate = new MaterialCate(id);
		this.cate.setRestaurantId(this.restaurantId);
		this.cate.setName(name);
	}
	
	public void setCate(int id, String name, int type) {
		this.cate = new MaterialCate(id);
		this.cate.setRestaurantId(this.restaurantId);
		this.cate.setName(name);
		this.cate.setType(MaterialCate.Type.valueOf(type));
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
	
	public String getPinyin() {
		return pinyin;
	}
	
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	
	public void addStock(float count){
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
		private float delta;
		private String lastModStaff;
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
		if(this.minAlarmAmount != null){
			jm.putFloat("minAlarmAmount", this.minAlarmAmount);
		}
		if(this.maxAlarmAmount != null){
			jm.putFloat("maxAlarmAmount", this.maxAlarmAmount);
		}
		if(this.cate != null){
			jm.putInt("cateId", this.getCate().getId());
			jm.putString("cateName", this.getCate().getName());
			jm.putInt("cateType", this.getCate().getType().getValue());
		}
		//记录当前价格是否修改
		jm.putBoolean("changed", this.delta != 0 ? true : false);
		jm.putBoolean("isGood", this.isGood);
		jm.putInt("associateId", this.associateId);
		
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
