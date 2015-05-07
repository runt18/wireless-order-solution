package com.wireless.pojo.dishesOrder;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.tasteMgr.Taste;

public class ComboOrderFood implements Parcelable, Jsonable {
	
	//private int comboId;
	private ComboFood comboFood;
	private TasteGroup tasteGroup;
	
	private ComboOrderFood(){
		this.comboFood = null;
	}
	
	public ComboOrderFood(ComboFood comboFood){
		this.comboFood = comboFood;
	}
	
//	public void setComboId(int comboId){
//		this.comboId = comboId;
//	}
//	
//	public int getCombId(){
//		return this.comboId;
//	}
	
	public ComboFood asComboFood(){
		return this.comboFood;
	}
	
	public String getName(){
		if(comboFood != null){
			return comboFood.getName();
		}else{
			return "";
		}
	}
	
	public boolean hasTasteGroup(){
		return tasteGroup == null ? false : tasteGroup.hasPreference();
	}
	
	public boolean hasNormalTaste(){
		return tasteGroup == null ? false : tasteGroup.hasNormalTaste();
	}
	
	public boolean hasTmpTaste(){
		return tasteGroup == null ? false : tasteGroup.hasTmpTaste();
	}
	
	public void setTasteGroup(TasteGroup tg){
		if(tg != null){
			tasteGroup = tg;
			tasteGroup.setAttachedFood(new OrderFood(comboFood.asFood()));
		}
	}
	
	public void clearTasteGroup(){
		this.tasteGroup = null;
	}
	
	public TasteGroup getTasteGroup(){
		return tasteGroup == null ? TasteGroup.EMPTY : tasteGroup;
	}
	
	public boolean removeTaste(Taste tasteToRemove){
		if(tasteGroup != null){
			if(tasteGroup.removeTaste(tasteToRemove)){
				tasteGroup.refresh();
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	public boolean addTaste(Taste tasteToAdd){
		if(tasteGroup == null){
			tasteGroup = new TasteGroup(new OrderFood(comboFood.asFood()));
		}
		if(tasteGroup.addTaste(tasteToAdd)){
			tasteGroup.refresh();
			return true;
		}else{
			return false;
		}
	}
	
	public void setTmpTaste(Taste tmpTaste){
		if(tasteGroup == null){
			tasteGroup = new TasteGroup(new OrderFood(comboFood.asFood()));
		}
		tasteGroup.setTmpTaste(tmpTaste);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof ComboOrderFood)){
			return false;
		}else{
			return comboFood.equals(((ComboOrderFood)obj).comboFood);
		}
	}
	
	@Override
	public int hashCode(){
		int hashCode = (comboFood == null ? 0 : comboFood.hashCode());
		hashCode = hashCode * 31 + (tasteGroup == null ? 0 : tasteGroup.hashCode());
		return hashCode;
	}
	
	@Override
	public String toString(){
		return (comboFood == null ? "" : comboFood.getName()) + 
			   (tasteGroup == null ? "" : "-" + tasteGroup.getPreference());
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeParcel(this.comboFood, ComboFood.COMBO_FOOD_PARCELABLE_SIMPLE);
		dest.writeParcel(this.tasteGroup, TasteGroup.TG_PARCELABLE_COMPLEX);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.comboFood = source.readParcel(ComboFood.CREATOR);
		setTasteGroup(source.readParcel(TasteGroup.CREATOR));
	}
	
	public static Parcelable.Creator<ComboOrderFood> CREATOR = new Parcelable.Creator<ComboOrderFood>() {
		
		@Override
		public ComboOrderFood[] newInstance(int size) {
			return new ComboOrderFood[size];
		}
		
		@Override
		public ComboOrderFood newInstance() {
			return new ComboOrderFood();
		}
	};

	public static enum Key4Json{
		COMBO_FOOD("comboFood", "子菜"),
		COMBO_FOOD_TASTE_GROUP("tasteGroup", "子菜口味");
		
		Key4Json(String key, String desc){
			this.key = key;
			this.desc = desc;
		}
		
		private final String key;
		private final String desc;
		
		@Override
		public String toString(){
			return "key = " + key + ",desc = " + desc;
		}
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(Key4Json.COMBO_FOOD.key, this.comboFood, ComboFood.COMBO_FOOD_JSONABLE_COMPLEX);
		jm.putJsonable(Key4Json.COMBO_FOOD_TASTE_GROUP.key, this.tasteGroup, TasteGroup.TG_JSONABLE_4_COMMIT);
		return jm;
	}
	
	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		this.comboFood = jm.getJsonable(Key4Json.COMBO_FOOD.key, ComboFood.JSON_CREATOR, ComboFood.COMBO_FOOD_JSONABLE_SIMPLE);
		if(jm.containsKey(Key4Json.COMBO_FOOD_TASTE_GROUP.key)){
			setTasteGroup(jm.getJsonable(Key4Json.COMBO_FOOD_TASTE_GROUP.key, TasteGroup.JSON_CREATOR, TasteGroup.TG_JSONABLE_4_COMMIT));
		}
	}
	
	public static Jsonable.Creator<ComboOrderFood> JSON_CREATOR = new Jsonable.Creator<ComboOrderFood>() {
		@Override
		public ComboOrderFood newInstance() {
			return new ComboOrderFood();
		}
	};
}
