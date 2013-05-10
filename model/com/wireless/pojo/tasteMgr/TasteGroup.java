package com.wireless.pojo.tasteMgr;

import java.util.List;

import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.util.SortedList;

public class TasteGroup implements Parcelable{
	
	public final static byte TG_PARCELABLE_COMPLEX = 0;
	public final static byte TG_PARCELABLE_SIMPLE = 1;
	
	public final static String NO_TASTE_PREF = "无口味";
	
	public final static int NEW_TASTE_GROUP_ID = 0;
	public final static int EMPTY_TASTE_GROUP_ID = 1;
	public final static int EMPTY_NORMAL_TASTE_GROUP_ID = 1;
	
	int mGroupId = EMPTY_TASTE_GROUP_ID;
	
	OrderFood mAttachedOrderFood;
	
	SortedList<Taste> mTastes = new SortedList<Taste>();
	SortedList<Taste> mSpecs = new SortedList<Taste>();
	
	Taste mTmpTaste;
	
	Taste mNormalTaste;
	
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
			if(mSpecs.containsElement(tasteToAdd)){
				return false;
			}else{
				mSpecs.add(tasteToAdd);
				return true;
			}
						
		}else{
			return false;
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
			
		}else if(tasteToRemove.isSpec()){
			return mSpecs.removeElement(tasteToRemove);
			
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
		for(Taste spec : mSpecs){
			hashCode = hashCode * 31 + spec.hashCode();
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
		return getTastePref();
	}
	
	/**
	 * Check to see whether the normal tastes is the same.
	 * @param tg the taste group to be compared
	 * @return true if the normal tastes to these two taste group is the same, otherwise false
	 */
	public boolean equalsByNormal(TasteGroup tg){
		return mTastes.equals(tg.mTastes) && mSpecs.equals(tg.mSpecs);
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
		float tastePrice = 0;
		for(Taste t : mTastes){
			tastePrice += t.isCalcByPrice() ? t.getPrice() : (mAttachedOrderFood.getPrice() * t.getRate());
		}
		return NumericUtil.roundFloat(tastePrice);
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
	 * Get the taste price (include both normal and temporary taste).
	 * @return the taste price
	 */
	public float getTastePrice(){
		return getNormalTastePrice() + getTmpTastePrice();
	}
	
	/**
	 * Get the preference string to normal tastes.
	 * The String is combined by each normal taste. 
	 * Each string to normal taste separated by commas.
	 * @return the preference string to normal tastes
	 */
	public String getNormalTastePref(){
		
		if(!mTastes.isEmpty() || !mSpecs.isEmpty()){
			StringBuilder tastePref = new StringBuilder();
			
			for(Taste taste : mTastes){
				if(tastePref.length() == 0){
					tastePref.append(taste.getPreference());
				}else{
					tastePref.append(",").append(taste.getPreference());
				}
			}
			
			for(Taste spec : mSpecs){
				if(tastePref.length() == 0){
					tastePref.append(spec.getPreference());
				}else{
					tastePref.append(",").append(spec.getPreference());
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
	 * Get the taste preference string along with both normal and temporary tastes.
	 * @return the taste preference string along with both normal and temporary tastes
	 */
	public String getTastePref(){
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
	
	public boolean hasInternalTaste(){
		return !mTastes.isEmpty();
	}
	
	public boolean hasSpec(){
		return !mSpecs.isEmpty();
	}
	
	public boolean hasNormalTaste(){
		if(hasCalc){
			return mNormalTaste != null;
		}else{
			return hasInternalTaste() || hasSpec();
		}
	}
	
	public boolean hasTmpTaste(){
		return mTmpTaste != null;
	}
	
	public boolean hasTaste(){
		return hasNormalTaste() || hasTmpTaste();
	}
	
	public int getGroupId(){
		return mGroupId;
	}
	
	public void setGroupId(int groupId){
		this.mGroupId = groupId;
	}
	
	public List<Taste> getTastes(){
		return mTastes;
	}
	
	public List<Taste> getSpecs(){
		return mSpecs;
	}
	
	public List<Taste> getNormalTastes(){
		List<Taste> normal = new SortedList<Taste>();
		normal.addAll(mTastes);
		normal.addAll(mSpecs);
		return normal;
	}
	
	public Taste getTmpTaste(){
		return mTmpTaste;
	}
	
	public void setTmpTaste(Taste tmpTaste){
		this.mTmpTaste = tmpTaste;
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
			dest.writeParcelList(this.mSpecs, Taste.TASTE_PARCELABLE_SIMPLE);
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
			this.mTastes = new SortedList<Taste>(source.readParcelList(Taste.TASTE_CREATOR));
			this.mSpecs = new SortedList<Taste>(source.readParcelList(Taste.TASTE_CREATOR));
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
	
}	
