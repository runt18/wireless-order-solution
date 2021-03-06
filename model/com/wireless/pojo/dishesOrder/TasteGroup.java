package com.wireless.pojo.dishesOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;

public class TasteGroup implements Parcelable, Jsonable{
	
	public static class InsertBuilder{
		private List<Taste> normalTastes = new ArrayList<Taste>();
		private Taste tmpTaste;
		private final OrderFood attachedFood;

		public InsertBuilder(OrderFood of){
			this.attachedFood = of;
		}
		
		public InsertBuilder addTaste(Taste taste){ 
			normalTastes.add(taste);
			return this;
		}
		
		public InsertBuilder addTastes(List<Taste> tastes){
			normalTastes.addAll(tastes);
			return this;
		}
		
		public InsertBuilder setTmpTaste(Taste tmpTaste){
			this.tmpTaste = tmpTaste;
			return this;
		}
		
		public TasteGroup build(){
			return new TasteGroup(this);
		}
	}
	
	public static final TasteGroup EMPTY = new TasteGroup(){
		@Override
		public int getGroupId(){
			return EMPTY_TASTE_GROUP_ID;
		}
		@Override
		public void setGroupId(int groupId){
			throw new UnsupportedOperationException("Empty taste group does NOT support this operation.");
		}
		@Override
		public void setTmpTaste(Taste tmpTaste){
			throw new UnsupportedOperationException("Empty taste group does NOT support this operation.");
		}
		@Override
		public boolean addTaste(Taste tasteToAdd){
			throw new UnsupportedOperationException("Empty taste group does NOT support this operation.");
		}
	};
	
	public final static byte TG_PARCELABLE_COMPLEX = 0;
	public final static byte TG_PARCELABLE_SIMPLE = 1;
	
	private final static String NO_TASTE_PREF = "无口味";
	
	public final static int EMPTY_TASTE_GROUP_ID = 1;
	public final static int EMPTY_NORMAL_TASTE_GROUP_ID = 1;
	
	private int mGroupId = EMPTY_TASTE_GROUP_ID;
	
	private OrderFood mAttachedFood;
	
	private SortedList<Taste> mTastes = SortedList.newInstance();
	private Taste mSpec;
	
	private Taste mTmpTaste;
	private Taste mNormalTaste;
	
	private TasteGroup(InsertBuilder builder){
		this.mAttachedFood = builder.attachedFood;
		if(builder.normalTastes != null){
			for(Taste t : builder.normalTastes){
				addTaste(t);
			}
			refresh();
		}
		this.mTmpTaste = builder.tmpTaste;
	}
	
	private TasteGroup(){
		
	}
	
	public TasteGroup(int groupId, Taste normalTaste, List<Taste> normalDetails, Taste tmpTaste){
		this.mGroupId = groupId;
		this.mNormalTaste = normalTaste;
		this.mTmpTaste = tmpTaste; 
		if(normalDetails != null){
			for(Taste tasteToAdd : normalDetails){
				addTaste(tasteToAdd);
			}
		}
	}
	
	TasteGroup(OrderFood attachedFood){
		this.mAttachedFood = attachedFood;
	}
	
	private float makeNormalTastePrice(){ 
		float tastePrice = 0;
		for(Taste t : getNormalTastes()){
			tastePrice += t.isCalcByPrice() ? t.getPrice() : (mAttachedFood.getFoodPrice() * t.getRate());
		}
		return NumericUtil.roundFloat(tastePrice);
	}
	
	/**
	 * Make the preference to normal tastes which is combined by every normal taste and spec.
	 * @return the preference to normal tastes
	 */
	private String makeNormalTastePref(){
		
		StringBuilder tastePref = new StringBuilder();
		
		for(Taste taste : mTastes){
			if(tastePref.length() == 0){
				tastePref.append(taste.getPreference());
			}else{
				tastePref.append(",").append(taste.getPreference());
			}
		}
		
		if(hasSpec()){
			if(tastePref.length() == 0){
				tastePref.append(mSpec.getPreference());
			}else{
				tastePref.append(",").append(mSpec.getPreference());
			}
		}
		
		return tastePref.toString();
	}
	
	public void refresh(){
		if(!mTastes.isEmpty() || mSpec != null){
			if(mNormalTaste == null){
				mNormalTaste = new Taste(0);
			}
			mNormalTaste.setPreference(makeNormalTastePref());
			mNormalTaste.setPrice(makeNormalTastePrice());
		}else{
			mNormalTaste = null;
		}
	}
	
	/**
	 * Add the normal taste to list if NOT exist before.
	 * @param tasteToAdd
	 * 			the taste to add
	 * @return true if the taste to add NOT exist before and succeed to add to list, otherwise return false
	 */
	boolean addTaste(Taste tasteToAdd){
		if(tasteToAdd.isTaste()){
			if(mTastes.containsElement(tasteToAdd)){
				return false;
			}else{
				mTastes.add(tasteToAdd);
				return true;
			}
			
		}else if(tasteToAdd.isSpec()){
			if(mSpec != null && mSpec.equals(tasteToAdd)){
				return false;
			}else{
				mSpec = tasteToAdd;
				return true;
			}
						
		}else{
			throw new IllegalArgumentException("The taste to add should belong to taste or spec.");
		}
	}
	
	/**
	 * Remove the taste if exist before.
	 * @param tasteToRemove
	 * 			The taste to remove.
	 * @return	true if taste exist before and succeed to be removed, otherwise return false
	 */
	boolean removeTaste(Taste tasteToRemove){
		if(tasteToRemove.isTaste()){
			return mTastes.removeElement(tasteToRemove);
			
		}else if(tasteToRemove.isSpec() && tasteToRemove.equals(mSpec)){
			mSpec = null;
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		int hashCode = 0;
		for(Taste taste : mTastes){
			hashCode = hashCode * 31 + taste.hashCode();
		}
		if(hasSpec()){
			hashCode = hashCode * 31 + mSpec.hashCode();
		}
		return hashCode * 31 + (mTmpTaste != null ? mTmpTaste.hashCode() : 0);		
	}
	
	/**
	 * Check to see whether two taste groups is equal. 
	 * @return true if both normal tastes and temporary taste is matched, otherwise false
	 */
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof TasteGroup)){
			return false;
		}else{
			TasteGroup tg = (TasteGroup)obj;
			return equalsByNormal(tg) && equalsByTmp(tg);
		}
	}
	
	@Override
	public String toString(){
		return getPreference();
	}
	
	/**
	 * Check to see whether the normal tastes is the same.
	 * @param tg the taste group to be compared
	 * @return true if the normal tastes to these two taste group is the same, otherwise false
	 */
	public boolean equalsByNormal(TasteGroup tg){
		return mTastes.equals(tg.mTastes) && equalsBySpec(tg);
	}
	
	private boolean equalsBySpec(TasteGroup tg){
		if(hasSpec() && tg.hasSpec()){
			return mSpec.equals(tg.mSpec);
		}else if(!hasSpec() && !tg.hasSpec()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Check to whether the temporary taste is the same.
	 * @param tg the taste group to be compared
	 * @return true if the temporary taste to these two taste group is the same, otherwise false 
	 */
	public boolean equalsByTmp(TasteGroup tg){
		if(hasTmpTaste() && tg.hasTmpTaste()){
			return mTmpTaste.getPreference().equals(tg.mTmpTaste.getPreference()) && 
				   mTmpTaste.getPrice() == tg.mTmpTaste.getPrice();
			
		}else if(!hasTmpTaste() && !tg.hasTmpTaste()){
			return true;
			
		}else{
			return false;
		}
	}
	
	/**
	 * Get the price to normal taste.
	 * @return the price to normal taste
	 */
	public float getNormalTastePrice(){
		return mNormalTaste == null ? 0 : mNormalTaste.getPrice();
	}
	
	/**
	 * Get the price to temporary taste.
	 * @return the price to temporary taste
	 */
	public float getTmpTastePrice(){
		return mTmpTaste == null ? 0 : mTmpTaste.getPrice();
	}
	
	/**
	 * Get the price (include both normal and temporary taste).
	 * @return the taste price
	 */
	public float getPrice(){
		return getNormalTastePrice() + getTmpTastePrice();
	}
	
	/**
	 * Get the preference string to normal tastes.
	 * The String is combined by each normal taste. 
	 * Each string to normal taste separated by commas.
	 * @return the preference string to normal tastes
	 */
	public String getNormalTastePref(){
		return mNormalTaste != null ? mNormalTaste.getPreference() : NO_TASTE_PREF;
	}
	
	/**
	 * Get the preference string to temporary taste.
	 * @return the preference string to temporary taste
	 */
	public String getTmpTastePref(){
		return mTmpTaste == null ? "" : mTmpTaste.getPreference();
	}
	
	/**
	 * Get the preference string along with both normal and temporary tastes.
	 * @return the preference string along with both normal and temporary tastes
	 */
	public String getPreference(){
		if(hasNormalTaste() && hasTmpTaste()){
			return getNormalTastePref() + "," + getTmpTastePref();
			
		}else if(!hasNormalTaste() && !hasTmpTaste()){
			return NO_TASTE_PREF;
			
		}else if(hasNormalTaste() && !hasTmpTaste()){
			return getNormalTastePref();
			
		}else{
			return getTmpTastePref();				
		}
	}
	
	public boolean hasSpec(){
		return mSpec != null;
	}
	
	public boolean hasNormalTaste(){
		return mNormalTaste != null;
	}
	
	public boolean hasTmpTaste(){
		return mTmpTaste != null;
	}
	
	public boolean hasPreference(){
		return hasNormalTaste() || hasTmpTaste();
	}
	
	public int getGroupId(){
		return mGroupId;
	}
	
	public void setGroupId(int groupId){
		this.mGroupId = groupId;
	}
	
	public List<Taste> getTastes(){
		return Collections.unmodifiableList(mTastes);
	}
	
	public Taste getSpec(){
		return mSpec;
	}
	
	public Taste getNormalTaste(){
		return mNormalTaste;
	}
	
	public List<Taste> getNormalTastes(){
		List<Taste> normal = SortedList.newInstance();
		normal.addAll(mTastes);
		if(hasSpec()){
			normal.add(mSpec);
		}
		return normal;
	}
	
	public Taste getTmpTaste(){
		return mTmpTaste;
	}
	
	public void setTmpTaste(Taste tmpTaste){
		this.mTmpTaste = tmpTaste;
	}
	
	public boolean contains(Taste taste){
		if(mTastes.containsElement(taste)){
			return true;
		}else if(hasSpec()){
			return mSpec.equals(taste);
		}else{
			return false;
		}
	}
	
	void setAttachedFood(OrderFood attachedFood){
		this.mAttachedFood = attachedFood;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TG_PARCELABLE_SIMPLE){
			dest.writeInt(this.mGroupId);
			
		}else if(flag == TG_PARCELABLE_COMPLEX){
			dest.writeInt(this.mGroupId);
			dest.writeParcelList(this.mTastes, Taste.TASTE_PARCELABLE_SIMPLE);
			dest.writeParcel(this.mSpec, Taste.TASTE_PARCELABLE_SIMPLE);
			dest.writeParcel(this.mTmpTaste, Taste.TASTE_PARCELABLE_COMPLEX);
			dest.writeParcel(this.mNormalTaste, Taste.TASTE_PARCELABLE_COMPLEX);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TG_PARCELABLE_SIMPLE){
			this.mGroupId = source.readInt();
			
		}else if(flag == TG_PARCELABLE_COMPLEX){
			this.mGroupId = source.readInt();
			this.mTastes = SortedList.newInstance((source.readParcelList(Taste.CREATOR)));
			this.mSpec = source.readParcel(Taste.CREATOR);
			this.mTmpTaste = source.readParcel(Taste.CREATOR);
			this.mNormalTaste = source.readParcel(Taste.CREATOR);
		}
	}
	
	public final static Parcelable.Creator<TasteGroup> CREATOR = new Parcelable.Creator<TasteGroup>() {
		
		@Override
		public TasteGroup[] newInstance(int size) {
			return new TasteGroup[size];
		}
		
		@Override
		public TasteGroup newInstance() {
			return new TasteGroup();
		}
	};

	public static enum Key4Json{
		GROUP_ID("groupId", ""),
		TASTE_PREF("tastePref", ""),
		TASTE_PRICE("tastePrice", ""),
		NORMAL_TASTE("normalTaste", ""),
		NORMAL_TASTE_LIST("normalTasteContent", ""),
		TMP_TASTE("tmpTaste", "");
		
		private final String key;
		private final String desc;
		
		Key4Json(String key, String desc){
			this.key = key;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "key = " + key + ",desc = " + desc;
		}
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt(Key4Json.GROUP_ID.key, this.mGroupId);
		jm.putString(Key4Json.TASTE_PREF.key, this.getPreference());
		jm.putFloat(Key4Json.TASTE_PRICE.key, this.getPrice());
		jm.putJsonable(Key4Json.NORMAL_TASTE.key, this.mNormalTaste, 0);
		jm.putJsonableList(Key4Json.NORMAL_TASTE_LIST.key, this.getNormalTastes(), 0);
		jm.putJsonable(Key4Json.TMP_TASTE.key, this.mTmpTaste, 0);
		
		return jm;
	}

	public final static int TG_JSONABLE_4_COMMIT = 0;
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		if(flag == TG_JSONABLE_4_COMMIT){
			if(jsonMap.containsKey(Key4Json.NORMAL_TASTE_LIST.key)){
				for(Taste t : jsonMap.getJsonableList(Key4Json.NORMAL_TASTE_LIST.key, Taste.JSON_CREATOR, Taste.TASTE_JSONABLE_4_COMMIT)){
					addTaste(t);
					refresh();
				}
			}
			if(jsonMap.containsKey(Key4Json.TMP_TASTE.key)){
				setTmpTaste(jsonMap.getJsonable(Key4Json.TMP_TASTE.key, Taste.JSON_CREATOR, Taste.TMP_TASTE_JSONABLE_4_COMMIT));
			}
		}
	}

	public static Jsonable.Creator<TasteGroup> JSON_CREATOR = new Jsonable.Creator<TasteGroup>() {
		@Override
		public TasteGroup newInstance() {
			return new TasteGroup();
		}
	};
	
}	
