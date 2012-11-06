package com.wireless.protocol;

public class TasteGroup {
	
	public final static String NO_TASTE_PREF = "无口味";
	
	public final static int NEW_TASTE_GROUP_ID = 0;
	public final static int EMPTY_TASTE_GROUP_ID = 1;
	public final static int EMPTY_NORMAL_TASTE_GROUP_ID = 1;
	
	int mGroupId = EMPTY_TASTE_GROUP_ID;
	
	OrderFood mAttachedOrderFood;
	
	Taste[] mNormalTastes;
	
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
		this.mNormalTastes = normalTastes;
		sort();
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
		int index = indexOf(tasteToAdd);
		if(index < 0){
			if(mNormalTastes != null){
				Taste[] newTastes = new Taste[mNormalTastes.length + 1];
				System.arraycopy(mNormalTastes, 0, newTastes, 0, mNormalTastes.length);
				newTastes[mNormalTastes.length] = tasteToAdd;
				mNormalTastes = newTastes;
				sort();
			}else{
				mNormalTastes = new Taste[1];
				mNormalTastes[0] = tasteToAdd;
			}
			return true;
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
		int index = indexOf(tasteToRemove);
		if(index >= 0){
			mNormalTastes[index] = null;
			Taste[] newTastes = new Taste[mNormalTastes.length - 1];
			int pos = 0;
			for(int i = 0; i < mNormalTastes.length; i++){
				if(mNormalTastes[i] != null){
					newTastes[pos++] = mNormalTastes[i];
				}
			}
			mNormalTastes = newTastes;
			//sort();
			return true;
		}else{
			return false;			
		}
	}
	
	/**
	 * Returns the index of the first occurrence of the specified taste in this list, 
	 * or -1 if this list does not contain the taste to search.
	 * @param tasteToSrch
	 * 			The taste to search.
	 * @return the index of the first occurrence of the specified taste in this list, or -1 if this list does not contain the taste
	 */
	private int indexOf(Taste tasteToSrch){
		int index = -1;
		if(mNormalTastes != null){
			for(int i = 0; i < mNormalTastes.length; i++){
				if(mNormalTastes[i].equals(tasteToSrch)){
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
	private void sort(){
		if(mNormalTastes != null){
			for(int i = 0; i < mNormalTastes.length; i++){
				for(int j = i + 1; j < mNormalTastes.length; j++){
					if(mNormalTastes[i].compare(mNormalTastes[j]) > 0){
						Taste tmpTaste = mNormalTastes[i];
						mNormalTastes[i] = mNormalTastes[j];
						mNormalTastes[j] = tmpTaste;
					}
				}
			}			
		}
	}
	
//	@Override
	public int hashCode(){
		int hashCode = 0;
		if(mNormalTastes != null){
			for(int i = 0; i < mNormalTastes.length; i++){
				hashCode ^= new Integer(mNormalTastes[i].aliasID).hashCode();
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
			return equalsByNormalTastes(tg.mNormalTastes) && equalsByTmpTaste(tg.mTmpTaste);
		}
	}
	
	public boolean equalsByNormalTastes(Taste[] tastesToCompared){
		if(mNormalTastes == null && tastesToCompared == null){
			return true;
			
		}else if(mNormalTastes != null && tastesToCompared == null){
			return false;
			
		}else if(mNormalTastes == null && tastesToCompared != null){
			return false;
			
		}else{
			if(mNormalTastes.length != tastesToCompared.length){
				return false;
				
			}else{
				boolean isMatched = true;
				for(int i = 0; i < mNormalTastes.length; i++){
					if(!mNormalTastes[i].equals(tastesToCompared[i])){
						isMatched = false;
						break;
					}
				}
				return isMatched;
			}
		}
	}
	
	public boolean equalsByTmpTaste(Taste tmpTaste){
		if(mTmpTaste != null){
			return mTmpTaste.equals(tmpTaste);
		}else{
			return tmpTaste == null;
		}
	}
	
	/**
	 * Get the price to normal tastes represented as integer.
	 * @return the price to normal tastes represented as integer
	 */
	int getNormalTastePriceInternal(){
		if(mNormalTastes != null){
			int tastePrice = 0;
			for(int i = 0; i < mNormalTastes.length; i++){
				tastePrice += (mNormalTastes[i].calc == Taste.CALC_PRICE ? mNormalTastes[i].price : mAttachedOrderFood.price * mNormalTastes[i].rate / 100);
			}
			return tastePrice;
		}else{
			return 0;
		}
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
		return Util.int2Float(getNormalTastePriceInternal());
	}
	
	/**
	 * Get the price to temporary taste.
	 * @return the price to temporary taste
	 */
	public Float getTmpTastePrice(){
		return Util.int2Float(getTmpTastePriceInternal());
	}
	
	/**
	 * Get the taste price (include both normal and temporary taste).
	 * @return the taste price
	 */
	public Float getTastePrice(){
		return Util.int2Float(getTastePriceInternal());
	}
	
	/**
	 * Get the preference string to normal tastes.
	 * The String is combined by each normal taste. 
	 * Each string to normal taste separated by commas.
	 * @return the preference string to normal tastes
	 */
	public String getNormalTastePref(){
		if(mNormalTastes != null){
			String tastePref = "";
			for(int i = 0; i < mNormalTastes.length; i++){
				if(tastePref.length() != 0){
					tastePref += ",";
				}
				tastePref += mNormalTastes[i].preference;
			}			
			return tastePref;
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
			if(mNormalTastes != null && mTmpTaste != null){
				return getNormalTastePref() + "," + getTmpTastePref();
				
			}else if(mNormalTastes == null && mTmpTaste == null){
				return NO_TASTE_PREF;
				
			}else if(mNormalTastes != null && mTmpTaste == null){
				return getNormalTastePref();
				
			}else{
				return getTmpTastePref();				
			}
		}		
	}
	
	public boolean hasNormalTaste(){
		if(hasCalc){
			return mNormalTaste != null;
		}else{
			if(mNormalTastes != null){
				return mNormalTastes.length != 0;
			}else{
				return false;
			}
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
	
	public Taste[] getNormalTastes(){
		return mNormalTastes;
	}
	
	public void setNormalTastes(Taste[] normalTastes){
		this.mNormalTastes = normalTastes;
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
	
}	
