package com.wireless.pojo.tasteMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;

public class TasteGroup implements Parcelable, Jsonable{
	
	public final static byte TG_PARCELABLE_COMPLEX = 0;
	public final static byte TG_PARCELABLE_SIMPLE = 1;
	
	public final static String NO_TASTE_PREF = "无口味";
	
	public final static int NEW_TASTE_GROUP_ID = 0;
	public final static int EMPTY_TASTE_GROUP_ID = 1;
	public final static int EMPTY_NORMAL_TASTE_GROUP_ID = 1;
	
	int mGroupId = EMPTY_TASTE_GROUP_ID;
	
	private OrderFood mAttachedOrderFood;
	
	private SortedList<Taste> mTastes = SortedList.newInstance();
	private Taste mSpec;
	
	private Taste mTmpTaste;
	private Taste mNormalTaste;
	private boolean hasCalc = false;
	
	public TasteGroup(){
		
	}
	
	public TasteGroup(int groupId, Taste normalTaste, Taste tmpTaste){
		this.hasCalc = true;
		this.mGroupId = groupId;
		this.mNormalTaste = normalTaste;
		this.mTmpTaste = tmpTaste; 
	}
	
	public TasteGroup(OrderFood attachedOrderFood, Taste[] normalTastes, Taste tmpTaste){
		this.mAttachedOrderFood = attachedOrderFood;
		if(normalTastes != null){
			for(Taste t : normalTastes){
				addTaste(t);
			}
		}
		this.mTmpTaste = tmpTaste;
	}
	
	public TasteGroup(OrderFood attachedOrderFood, List<Taste> normalTastes, Taste tmpTaste){
		this.mAttachedOrderFood = attachedOrderFood;
		if(normalTastes != null){
			for(Taste t : normalTastes){
				addTaste(t);
			}
		}
		this.mTmpTaste = tmpTaste;
	}
	
	/**
	 * Add the normal taste to list if NOT exist before.
	 * @param tasteToAdd
	 * 			the taste to add
	 * @return true if the taste to add NOT exist before and succeed to add to list, otherwise return false
	 */
	public boolean addTaste(Taste tasteToAdd){
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
	public boolean removeTaste(Taste tasteToRemove){
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
		if(hasCalc){
			return mNormalTaste == null ? 0 : mNormalTaste.getPrice();
		}else{
			float tastePrice = 0;
			for(Taste t : getNormalTastes()){
				tastePrice += t.isCalcByPrice() ? t.getPrice() : (mAttachedOrderFood.getPrice() * t.getRate());
			}
			return NumericUtil.roundFloat(tastePrice);
		}
	}
	
	/**
	 * Get the price to temporary taste.
	 * @return the price to temporary taste
	 */
	public float getTmpTastePrice(){
		if(mTmpTaste != null){
			return mTmpTaste.getPrice();
		}else{
			return 0;
		}
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
		
		if(hasTaste() || hasTmpTaste()){
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
		}else{
			return NO_TASTE_PREF;
		}
		
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
		if(hasCalc){
			if(mGroupId == EMPTY_TASTE_GROUP_ID){
				return NO_TASTE_PREF;
				
			}else if(mNormalTaste != null && mTmpTaste == null){
				return mNormalTaste.getPreference();
				
			}else if(mNormalTaste == null && mTmpTaste != null){
				return mTmpTaste.getPreference();
				
			}else{
				return mNormalTaste.getPreference() + "," + mTmpTaste.getPreference();
			}
			
		}else{
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
	}
	
	public boolean hasTaste(){
		return !mTastes.isEmpty();
	}
	
	public boolean hasSpec(){
		return mSpec != null;
	}
	
	public boolean hasNormalTaste(){
		if(hasCalc){
			return true;
		}else{
			return hasTaste() || hasSpec();
		}
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
	
	public OrderFood getAttachedFood(){
		return mAttachedOrderFood;
	}
	
	public void setAttachedFood(OrderFood attachedFood){
		this.mAttachedOrderFood = attachedFood;
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
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TG_PARCELABLE_SIMPLE){
			this.mGroupId = source.readInt();
			
		}else if(flag == TG_PARCELABLE_COMPLEX){
			this.mGroupId = source.readInt();
			this.mTastes = SortedList.newInstance((source.readParcelList(Taste.TASTE_CREATOR)));
			this.mSpec = source.readParcel(Taste.TASTE_CREATOR);
			this.mTmpTaste = (Taste)source.readParcel(Taste.TASTE_CREATOR);
		}
	}
	
	public final static Parcelable.Creator<TasteGroup> TG_CREATOR = new Parcelable.Creator<TasteGroup>() {
		
		public TasteGroup[] newInstance(int size) {
			return new TasteGroup[size];
		}
		
		public TasteGroup newInstance() {
			return new TasteGroup();
		}
	};

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		HashMap<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("groupId", this.mGroupId);
		jm.put("tastePref", this.getPreference());
		jm.put("tastePrice", this.getPrice());
		jm.put("normalTaste", this.mNormalTaste);
		jm.put("normalTasteContent", this.getNormalTastes());
		jm.put("tmpTaste", this.mTmpTaste);
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

	
}	
