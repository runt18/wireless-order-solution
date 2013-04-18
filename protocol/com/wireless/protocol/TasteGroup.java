package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.util.NumericUtil;

public class TasteGroup implements Parcelable{
	
	public final static byte TG_PARCELABLE_COMPLEX = 0;
	public final static byte TG_PARCELABLE_SIMPLE = 1;
	
	public final static String NO_TASTE_PREF = "无口味";
	
	public final static int NEW_TASTE_GROUP_ID = 0;
	public final static int EMPTY_TASTE_GROUP_ID = 1;
	public final static int EMPTY_NORMAL_TASTE_GROUP_ID = 1;
	
	int mGroupId = EMPTY_TASTE_GROUP_ID;
	
	OrderFood mAttachedOrderFood;
	
	Taste[] mTastes;
	Taste[] mSpecs;
	
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
		this.setNormalTastes(normalTastes);
		this.mTmpTaste = tmpTaste;
	}
	
	/**
	 * Add the normal taste to list if NOT exist before,
	 * the taste list would be resorted after added.
	 * @param tasteToAdd
	 * 			The taste to add.
	 * @return true if the taste to add NOT exist before and succeed to add to list, otherwise return false
	 */
	public boolean addTaste(Taste tasteToAdd){
		if(tasteToAdd.isTaste()){
			Taste[] newTastes = addTaste(mTastes, tasteToAdd);
			if(newTastes != null){
				mTastes = newTastes;
				return true;
			}else{
				return false;
			}
			
		}else if(tasteToAdd.isSpec()){
			Taste[] newTastes = addTaste(mSpecs, tasteToAdd);
			if(newTastes != null){
				mSpecs = newTastes;
				return true;
			}else{
				return false;
			}
						
		}else{
			return false;
		}
	}
	
	Taste[] addTaste(Taste[] addTo, Taste tasteToAdd){
		int index = indexOf(addTo, tasteToAdd);
		if(index < 0){
			Taste[] newTastes;
			if(addTo != null){
				newTastes = new Taste[addTo.length + 1];
				System.arraycopy(addTo, 0, newTastes, 0, addTo.length);
				newTastes[addTo.length] = tasteToAdd;
				sort(newTastes);
				
			}else{
				newTastes = new Taste[1];
				newTastes[0] = tasteToAdd;
			}			
			return newTastes;
			
		}else{
			return null;
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
			Taste[] newTastes = removeTaste(mTastes, tasteToRemove);
			if(newTastes != null){
				mTastes = newTastes;
				return true;
			}else{
				return false;
			}
			
		}else if(tasteToRemove.isSpec()){
			Taste[] newTastes = removeTaste(mSpecs, tasteToRemove);
			if(newTastes != null){
				mSpecs = newTastes;
				return true;
			}else{
				return false;
			}
			
		}else{
			return false;
		}
	}
	
	private Taste[] removeTaste(Taste[] removeFrom, Taste tasteToRemove){
		int index = indexOf(removeFrom, tasteToRemove);
		if(index >= 0){
			removeFrom[index] = null;
			Taste[] newTastes = new Taste[removeFrom.length - 1];
			int pos = 0;
			for(int i = 0; i < removeFrom.length; i++){
				if(removeFrom[i] != null){
					newTastes[pos++] = removeFrom[i];
				}
			}
			
			return newTastes;			
			
		}else{
			return null;			
		}	
	}
	
	/**
	 * Returns the index of the first occurrence of the specified taste in this list, 
	 * or -1 if this list does not contain the taste to search.
	 * @param tasteToSrch
	 * 			The taste to search.
	 * @return the index of the first occurrence of the specified taste in this list, or -1 if this list does not contain the taste
	 */
//	private int indexOf(Taste tasteToSrch){
//		int index = -1;
//		if(tasteToSrch.isTaste()){
//			return indexOf(mTastes, tasteToSrch);
//			
//		}else if(tasteToSrch.isSpec()){
//			return indexOf(mSpecs, tasteToSrch);
//			
//		}else{
//			return index;
//		}
//	}	
	
	/**
	 * Returns the index of the first occurrence of the specified taste in this list, 
	 * or -1 if this list does not contain the taste to search.
	 * @param srchFrom
	 * 			The source tastes searches from.
	 * @param tasteToSrch
	 * 			The taste to search.
	 * @return the index of the first occurrence of the specified taste in this list, or -1 if this list does not contain the taste
	 */
	private int indexOf(Taste[] srchFrom, Taste tasteToSrch){
		int index = -1;
		if(srchFrom != null){
			for(int i = 0; i < srchFrom.length; i++){
				if(srchFrom[i].equals(tasteToSrch)){
					index = i;
					break;
				}
			}			
			return index;
		}else{
			return index;			
		}	
	}
	
	/**
	 * Sort the tastes according to a specified order.
	 */
	private void sort(Taste[] tastesToSort){
		if(tastesToSort != null){
			for(int i = 0; i < tastesToSort.length; i++){
				for(int j = i + 1; j < tastesToSort.length; j++){
					if(tastesToSort[i].compareTo(tastesToSort[j]) > 0){
						Taste tmpTaste = tastesToSort[i];
						tastesToSort[i] = tastesToSort[j];
						tastesToSort[j] = tmpTaste;
					}
				}
			}			
		}
	}
	
//	@Override
	public int hashCode(){
		int hashCode = 0;
		if(mTastes != null){
			for(int i = 0; i < mTastes.length; i++){
				hashCode ^= new Integer(mTastes[i].aliasId).hashCode();
			}
		}
		if(mSpecs != null){
			for(int i = 0; i < mSpecs.length; i++){
				hashCode ^= new Integer(mSpecs[i].aliasId).hashCode();
			}			
		}
		return hashCode ^ (mTmpTaste != null ? mTmpTaste.hashCode() : 0);		
	}
	
	/**
	 * Check to see whether two taste groups is equal. 
	 * @return true if both normal tastes and temporary taste is matched, otherwise false
	 */
//	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof TasteGroup)){
			return false;
		}else{
			TasteGroup tg = (TasteGroup)obj;
			return equalsByNormal(tg) && equalsByTmp(tg);
		}
	}
	
	public String toString(){
		return getTastePref();
	}
	
	/**
	 * Check to see whether the normal tastes is the same.
	 * @param tg the taste group to be compared
	 * @return true if the normal tastes to these two taste group is the same, otherwise false
	 */
	public boolean equalsByNormal(TasteGroup tg){
		if(!hasNormalTaste() && !tg.hasNormalTaste()){
			return true;
			
		}else if(hasNormalTaste() != tg.hasNormalTaste()){
			return false;
			
		}else if(hasNormalTaste() && tg.hasNormalTaste()){
			Taste[] tastes = getNormalTastes();
			Taste[] anotherTastes = tg.getNormalTastes();
			if(tastes.length != anotherTastes.length){
				return false;
				
			}else{
				boolean isMatched = true;
				for(int i = 0; i < tastes.length; i++){
					if(!tastes[i].equals(anotherTastes[i])){
						isMatched = false;
						break;
					}
				}
				return isMatched;
			}
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
			return mTmpTaste.preference.equals(tg.mTmpTaste.preference) && 
				   mTmpTaste.price == tg.mTmpTaste.price;
			
		}else if(!hasTmpTaste() && !tg.hasTmpTaste()){
			return true;
			
		}else{
			return false;
		}
	}
	
	/**
	 * Get the price to normal tastes represented as integer.
	 * @return the price to normal tastes represented as integer
	 */
	int getNormalTastePriceInternal(){		
		return getNormalTastePriceInternal(mTastes) + getNormalTastePriceInternal(mSpecs);

	}
	
	int getNormalTastePriceInternal(Taste[] src){
		int tastePrice = 0;
		if(src != null){
			for(int i = 0; i < src.length; i++){
				tastePrice += (src[i].calc == Taste.CALC_PRICE ? src[i].price : mAttachedOrderFood.mUnitPrice * src[i].rate / 100);
			}
		}
		return tastePrice;
	}
	
	/**
	 * Get the price to temporary tastes represented as integer.
	 * @return the price to temporary tastes represented as integer
	 */
	int getTmpTastePriceInternal(){
		if(mTmpTaste != null){
			return mTmpTaste.price;
		}else{
			return 0;
		}
	}
	
	/**
	 * Get the taste price (include normal and temporary taste) represented as integer.
	 * @return the taste price represented as integer
	 */
	int getTastePriceInternal(){
		if(hasCalc){
			return (mNormalTaste == null ? 0 : mNormalTaste.price) + (mTmpTaste == null ? 0 : mTmpTaste.price);
		}else{
			return getNormalTastePriceInternal() + getTmpTastePriceInternal();
		}
	}
	
	/**
	 * Get the price to normal taste.
	 * @return the price to normal taste
	 */
	public Float getNormalTastePrice(){
		return NumericUtil.int2Float(getNormalTastePriceInternal());
	}
	
	/**
	 * Get the price to temporary taste.
	 * @return the price to temporary taste
	 */
	public Float getTmpTastePrice(){
		return NumericUtil.int2Float(getTmpTastePriceInternal());
	}
	
	/**
	 * Get the taste price (include both normal and temporary taste).
	 * @return the taste price
	 */
	public Float getTastePrice(){
		return NumericUtil.int2Float(getTastePriceInternal());
	}
	
	/**
	 * Get the preference string to normal tastes.
	 * The String is combined by each normal taste. 
	 * Each string to normal taste separated by commas.
	 * @return the preference string to normal tastes
	 */
	public String getNormalTastePref(){
		
		String tastePref = null;
		if(hasInternalTaste()){
			tastePref = combineTastePref(mTastes);
		}
		
		String specPref = null;
		if(hasSpec()){
			specPref = combineTastePref(mSpecs);
		}
		
		if(tastePref != null && specPref != null){
			return tastePref + "," + specPref;
			
		}else if(tastePref != null && specPref == null){
			return tastePref;
			
		}else if(tastePref == null && specPref != null){	
			return specPref;
			
		}else{
			return NO_TASTE_PREF;
		}
		
	}
	
	String combineTastePref(Taste[] src){
		if(src != null){
			StringBuffer tastePref = new StringBuffer();
			for(int i = 0; i < src.length; i++){
				if(tastePref.length() != 0){
					tastePref.append(",");
				}
				tastePref.append(src[i].preference);
			}			
			return tastePref.toString();
		}else{
			return "";
		}
	}
	
	/**
	 * Get the preference string to temporary taste.
	 * @return the preference string to temporary taste
	 */
	public String getTmpTastePref(){
		return mTmpTaste == null ? "" : mTmpTaste.preference;
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
				return mNormalTaste.preference;
				
			}else if(mNormalTaste == null && mTmpTaste != null){
				return mTmpTaste.preference;
				
			}else{
				return mNormalTaste.preference + "," + mTmpTaste.preference;
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
		if(mTastes != null){
			return mTastes.length != 0;
		}else{
			return false;
		}
	}
	
	public boolean hasSpec(){
		if(mSpecs != null){
			return mSpecs.length != 0;
		}else{
			return false;
		}
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
	
	public Taste[] getTastes(){
		if(mTastes != null){
			return mTastes;
		}else{
			return new Taste[0];
		}
	}
	
	public Taste[] getSpecs(){
		if(mSpecs != null){
			return mSpecs;
		}else{
			return new Taste[0];
		}
	}
	
	public Taste[] getNormalTastes(){
		Taste[] normalTastes = new Taste[(mTastes != null ? mTastes.length : 0) + (mSpecs != null ? mSpecs.length : 0)];
		
		int pos = 0;
		if(mTastes != null){
			for(int i = 0; i < mTastes.length; i++){
				normalTastes[pos++] = mTastes[i];
			}
		}
		if(mSpecs != null){
			for(int i = 0; i < mSpecs.length; i++){
				normalTastes[pos++] = mSpecs[i];
			}
		}	
		
		sort(normalTastes);
		
		return normalTastes;
	}
	
	public void setNormalTastes(Taste[] normalTastes){
		mTastes = null;
		mSpecs = null;
		if(normalTastes != null){
			for(int i = 0; i < normalTastes.length; i++){
				addTaste(normalTastes[i]);
			}
		}
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

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TG_PARCELABLE_SIMPLE){
			dest.writeInt(this.mGroupId);
			
		}else if(flag == TG_PARCELABLE_COMPLEX){
			dest.writeInt(this.mGroupId);
			dest.writeParcelArray(this.mTastes, Taste.TASTE_PARCELABLE_SIMPLE);
			dest.writeParcelArray(this.mSpecs, Taste.TASTE_PARCELABLE_SIMPLE);
			dest.writeParcel(this.mTmpTaste, Taste.TASTE_PARCELABLE_COMPLEX);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TG_PARCELABLE_SIMPLE){
			this.mGroupId = source.readInt();
			
		}else if(flag == TG_PARCELABLE_COMPLEX){
			this.mGroupId = source.readInt();
			
			Parcelable[] parcelables;
			parcelables = source.readParcelArray(Taste.TASTE_CREATOR);
			if(parcelables != null){
				this.mTastes = new Taste[parcelables.length];
				for(int i = 0; i < mTastes.length; i++){
					mTastes[i] = (Taste)parcelables[i];
				}
			}
			
			parcelables = source.readParcelArray(Taste.TASTE_CREATOR);
			if(parcelables != null){
				this.mSpecs = new Taste[parcelables.length];
				for(int i = 0; i < mSpecs.length; i++){
					mSpecs[i] = (Taste)parcelables[i];
				}
			}
			
			this.mTmpTaste = (Taste)source.readParcel(Taste.TASTE_CREATOR);
		}
	}
	
	public final static Parcelable.Creator TG_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new TasteGroup[size];
		}
		
		public Parcelable newInstance() {
			return new TasteGroup();
		}
	};
	
}	
