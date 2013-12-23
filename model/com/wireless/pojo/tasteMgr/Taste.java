package com.wireless.pojo.tasteMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class Taste implements Parcelable, Comparable<Taste>, Jsonable{
	
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
	
	/**
	 * The calculation type to taste. 
	 */
	public static enum Calc{
		BY_PRICE(0, "按价格"),
		BY_RATE(1, "按比例");
		
		private final int val;
		private final String desc;
		
		Calc(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "calc(val = " + val + ",desc = " + desc + ")";
		}
		
		public static Calc valueOf(int val){
			for(Calc calc : values()){
				if(calc.val == val){
					return calc;
				}
			}
			throw new IllegalArgumentException("The calc(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
	}
	
	//The helper class to insert '大牌'
	public static class LargeInsertBuilder extends InsertBuilder{
		public final static String PREF = "大牌";
		public LargeInsertBuilder(int restaurantId, TasteCategory category){
			super(restaurantId, PREF, category);
			if(!category.isSpec()){
				throw new IllegalArgumentException("The category should belongs to spec.");
			}
			setType(Type.RESERVED);
			setCalc(Calc.BY_RATE);
		}
	}
	
	//The helper class to insert '中牌'
	public static class MediumInsertBuilder extends InsertBuilder{
		public final static String PREF = "中牌";
		public MediumInsertBuilder(int restaurantId, TasteCategory category){
			super(restaurantId, PREF, category);
			if(!category.isSpec()){
				throw new IllegalArgumentException("The category should belongs to spec.");
			}
			setType(Type.RESERVED);
			setCalc(Calc.BY_RATE);
		}
	}
	
	//The helper class to insert '例牌'
	public static class RegularInsertBuilder extends InsertBuilder{
		public final static String PREF = "例牌";
		public RegularInsertBuilder(int restaurantId, TasteCategory category){
			super(restaurantId, PREF, category);
			if(!category.isSpec()){
				throw new IllegalArgumentException("The category should belongs to spec.");
			}
			setType(Type.RESERVED);
			setCalc(Calc.BY_RATE);
		}
	}
	
	//The helper class to insert a new taste
	public static class InsertBuilder{
		private final String preference;				// 口味名称
		private final int restaurantId;					// 餐厅编号
		private float price;							// 口味价格
		private float rate;								// 口味比例
		private final TasteCategory category;			// 口味类型    
		private Calc calc = Calc.BY_PRICE;				// 口味计算方式          0:按价格     1:按比例
		private Type type = Type.NORMAL;				// 类型
		
		public InsertBuilder(int restaurantId, String pref, TasteCategory category){
			this.restaurantId = restaurantId;
			if(pref.trim().length() != 0){
				this.preference = pref;
			}else{
				throw new IllegalArgumentException("输入的口味名称不能为空");
			}
			this.category = category;
		}
		
		public InsertBuilder setPrice(float price){
			if(price < 0){
				throw new IllegalArgumentException("The price should be more than zero.");
			}
			this.price = price;
			return this;
		}
		
		public InsertBuilder setRate(float rate){
			if(rate < 0){
				throw new IllegalArgumentException();
			}
			this.rate = rate;
			return this;
		}
		
		InsertBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		InsertBuilder setCalc(Calc calc){
			this.calc = calc;
			return this;
		}
		
		public Taste build(){
			return new Taste(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int tasteId;
		private String preference;						// 口味名称
		private float price = -1;						// 口味价格
		private float rate = -1;								// 口味比例
		private TasteCategory category;					// 口味类型    
		private Calc calc = Calc.BY_PRICE;				// 口味计算方式          0:按价格     1:按比例
		
		public UpdateBuilder(int tasteId){
			this.tasteId = tasteId;
		}
		
		public UpdateBuilder setPrice(float price){
			if(price < 0){
				throw new IllegalArgumentException("The price should be more than zero.");
			}
			this.price = price;
			return this;
		}
		
		public boolean isPriceChanged(){
			return this.price >= 0;
		}
		
		public UpdateBuilder setRate(float rate){
			if(rate < 0){
				throw new IllegalArgumentException();
			}
			this.rate = rate;
			return this;
		}
		
		public boolean isRateChanged(){
			return this.rate >= 0;
		}
		
		public UpdateBuilder setPrefence(String pref){
			if(pref.trim().length() != 0){
				this.preference = pref;
			}else{
				throw new IllegalArgumentException("输入的口味名称不能为空");
			}
			return this;
		}
		
		public boolean isPrefChanged(){
			return this.preference != null;
		}
		
		public UpdateBuilder setCategory(TasteCategory category){
			this.category = category;
			if(this.category.isTaste()){
				this.calc = Calc.BY_PRICE;
			}else if(this.category.isSpec()){
				this.calc = Calc.BY_RATE;
			}
			return this;
		}
		
		public boolean isCategoryChanged(){
			return this.category != null;
		}
		
		public Taste build(){
			return new Taste(this);
		}
	}
	
	public final static byte TASTE_PARCELABLE_COMPLEX = 0;
	public final static byte TASTE_PARCELABLE_SIMPLE = 1;
	
	private final static String NO_PREFERENCE = "无口味"; 
	
	private int restaurantId;						// 餐厅编号
	private int tasteId;							// 口味编号
	private String preference;						// 口味名称
	private float price;							// 口味价格
	private float rate;								// 口味比例
	private int rank;								// 排行	
	private TasteCategory category = new TasteCategory();					// 口味类型    
	private Calc calc = Calc.BY_PRICE;				// 口味计算方式          0:按价格     1:按比例
	private Type type = Type.NORMAL;				// 操作类型	0:默认    1:系统保留(不可删除)
	
	private Taste(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
		setPreference(builder.preference);
		setPrice(builder.price);
		setRate(builder.rate);
		setCategory(builder.category);
		setCalc(builder.calc);
		setType(builder.type);
	}
	
	private Taste(UpdateBuilder builder){
		setTasteId(builder.tasteId);
		setPreference(builder.preference);
		setPrice(builder.price);
		setRate(builder.rate);
		setCategory(builder.category);
		setCalc(builder.calc);
	}
	
	/**
	 * Generate a temporary taste.
	 * @param pref the preference to this temporary taste
	 * @param price the price to this temporary taste
	 * @return the instance to temporary taste 
	 */
	public static Taste newTmpTaste(String pref, float price){
		Taste tmpTaste = new Taste();
		tmpTaste.setTasteId((int)(System.currentTimeMillis() % 65535));
		tmpTaste.setPreference(pref);
		tmpTaste.setPrice(price);
		return tmpTaste;
	}
	
	private Taste(){ }
	
	public Taste(int tasteId){
		this.tasteId = tasteId;
	}
	
	public Taste(int tasteId, TasteCategory.Status status){
		this.tasteId = tasteId;
		TasteCategory category = new TasteCategory(0);
		category.setStatus(status);
		this.category = category;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public int getTasteId() {
		return tasteId;
	}
	
	public void setTasteId(int tasteId) {
		this.tasteId = tasteId;
	}
	
	public String getPreference() {
		if(preference == null){
			return NO_PREFERENCE;
		}else{
			String pref = preference.trim();
			return pref.length() == 0 ? NO_PREFERENCE : pref;
		}
	}
	
	public void setPreference(String pref) {
		this.preference = pref;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public float getRate() {
		return rate;
	}
	
	public void setRate(float tasteRate) {
		this.rate = tasteRate;
	}
	
	public TasteCategory getCategory() {
		return category;
	}
	
	public void setCategory(TasteCategory category){
		this.category = category;
	}
	
	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * Check if the taste belongs taste category.
	 * @return true if the taste belongs to taste, otherwise false
	 */
	public boolean isTaste(){
		return category.isTaste();
	}
	
	/**
	 * Check if the taste belongs taste specification.
	 * @return true if the taste belongs to specification, otherwise false
	 */
	public boolean isSpec(){
		return category.isSpec();
	}
	
	public Calc getCalc() {
		return calc;
	}
	
	public void setCalc(int calcVal) {
		this.calc = Calc.valueOf(calcVal);
	}
	
	public void setCalc(Calc calc){
		this.calc = calc;
	}

	public boolean isCalcByPrice(){
		return calc == Calc.BY_PRICE;
	}
	
	public boolean isCalcByRate(){
		return calc == Calc.BY_RATE;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(int typeVal) {
		this.type = Type.valueOf(typeVal);
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public void copyFrom(Taste src){
		if(src != null && src != this){
			this.tasteId = src.tasteId;
			this.restaurantId = src.restaurantId;
			this.preference = src.preference;
			this.category = src.category;
			this.calc = src.calc;
			this.rate = src.rate;
			this.price = src.price;
			this.type = src.type;
		}
	}
	
	@Override 
	public int hashCode(){
		int result = 17;
		result = result * 31 + getTasteId();
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Taste)){
			return false;
		}else{
			Taste t = (Taste)obj;
			return getTasteId() == t.getTasteId();
		}
	}
	
	@Override 
	public String toString(){
		return "taste(" +
			   "taste_id = " + getTasteId() +
			   ",restaurant_id = " + getRestaurantId() +
			   ",name = " + getPreference() + ")";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TASTE_PARCELABLE_SIMPLE){
			dest.writeInt(this.tasteId);
			
		}else if(flag == TASTE_PARCELABLE_COMPLEX){
			dest.writeInt(this.tasteId);
			dest.writeParcel(this.category, TasteCategory.TASTE_CATE_PARCELABLE_SIMPLE);
			dest.writeByte(this.calc.getVal());
			dest.writeByte(this.type.getVal());
			dest.writeFloat(this.price);
			dest.writeFloat(this.rate);
			dest.writeString(this.preference);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TASTE_PARCELABLE_SIMPLE){
			this.tasteId = source.readInt();
			
		}else if(flag == TASTE_PARCELABLE_COMPLEX){
			this.tasteId = source.readInt();
			this.category = source.readParcel(TasteCategory.CREATOR);
			this.calc = Calc.valueOf(source.readByte());
			this.type = Type.valueOf(source.readByte());
			this.price = source.readFloat();
			this.rate = source.readFloat();
			this.preference = source.readString();
		}
	}

	public final static Parcelable.Creator<Taste> TASTE_CREATOR = new Parcelable.Creator<Taste>() {
		
		public Taste[] newInstance(int size) {
			return new Taste[size];
		}
		
		public Taste newInstance() {
			return new Taste();
		}
	};

	@Override
	public int compareTo(Taste o) {
		if(getTasteId() > o.getTasteId()){
			return 1;
		}else if(getTasteId() < o.getTasteId()){
			return -1;
		}else{
			return 0;
		}
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.tasteId);
		jm.put("alias", this.tasteId);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.preference);
		jm.put("price", this.getPrice());
		jm.put("rate", this.rate);
		jm.put("rank", this.rank);
		jm.put("cateValue", this.category.getId());
		jm.put("cateText", this.category.getName());
		jm.put("calcValue", this.calc.getVal());
		jm.put("calcText", this.calc.getDesc());
		jm.put("typeValue", this.type.getVal());
		jm.put("typeText", this.type.getDesc());
		
		return Collections.unmodifiableMap(jm);
	}
	
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
