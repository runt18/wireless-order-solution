package com.wireless.protocol;

public class OrderFood extends Food {
	public long orderDate;
	public String waiter;
	public int payManner = Order.MANNER_CASH;
	
	public static final int FOOD_NORMAL = 0;		/* 普通 */
	public static final int FOOD_HANG_UP = 1;		/* 叫起 */
	public static final int FOOD_IMMEDIATE = 2;		/* 即起 */
	public short hangStatus = FOOD_NORMAL;			//the hang status to the food
	
	public Taste[] tastes = new Taste[3];			//three tastes the food can consist of
	
	public Taste tmpTaste;							//the temporary taste to this food
	
	public Table table = new Table();				//the table this order food belongs to
	
	public boolean isTemporary = false;				//indicates whether the food is temporary
	
	/**
	 * The value of discount ranges from 0.00 through 1.00
	 * So the real price should be divided 100 at last. 
	 */
	int discount = 100;	//the discount to this food 
	
	public void setDiscount(Float _discount){
		discount = Util.float2Int(_discount);
	}
	
	public Float getDiscount(){
		return Util.int2Float(discount);
	}
	
	/**
	 * Here we use an integer to represent the amount of ordered food.
	 */
	int count = 0;		//the number of the food to be ordered
	
	public void setCount(Float _count){
		count = Util.float2Int(_count);
	}
	
	public Float getCount(){
		return Util.int2Float(count);
	}
	
	/**
	 * Indicates the food is hurried
	 */
	public boolean isHurried = false;
	
	/**
	 * Comparing two foods without the tastes
	 * @param food
	 * @return
	 */
	public boolean equalsIgnoreTaste(OrderFood food){
		if(isTemporary != food.isTemporary){
			return false;
		}else if(isTemporary && food.isTemporary){
			return name.equals(food.name) && (price == food.price);
		}else{
			return aliasID == food.aliasID && hangStatus == food.hangStatus;
		}
	}
	
	/**
	 * Comparing two foods without the hang status.
	 * @param food
	 * @return
	 */
	public boolean equalsIgnoreHangStauts(OrderFood food){
		if(isTemporary != food.isTemporary){
			return false;
		}else if(isTemporary && food.isTemporary){
			return name.equals(food.name) && (price == food.price);
		}else{
			return aliasID == food.aliasID &&
				   tastes[0].aliasID == food.tastes[0].aliasID &&
				   tastes[1].aliasID == food.tastes[1].aliasID &&
				   tastes[2].aliasID == food.tastes[2].aliasID &&
				   ((tmpTaste == null && food.tmpTaste == null) ? true : 
					   ((tmpTaste != null && food.tmpTaste != null) ? tmpTaste.aliasID == food.tmpTaste.aliasID : false));
		}
	}
	
	/**
	 * There are three ways to determine whether two foods is the same as each other.
	 * 1 - If one food is temporary while the other NOT, means they are NOT the same.
	 * 2 - If both of foods are temporary, check to see whether their names and price are the same.
	 *     They are the same if both name and price is matched.
	 * 3 - If both of foods are NOT temporary, check to see their food, all tastes id and hang status.
	 *     They are the same if all of the things above are matched.
	 */
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof OrderFood)){
			return false;
			
		}else{
			OrderFood food = (OrderFood)obj;
			if(isTemporary != food.isTemporary){
				return false;
				
			}else if(hangStatus != food.hangStatus){
				return false;
				
			}else{
				return equalsIgnoreHangStauts(food);
			}
			
		}
	}

	/**
	 * Generate the hash code according to the equals method.
	 */
	public int hashCode(){
		if(isTemporary){
			return name.hashCode() ^ price ^ hangStatus;
		}else{
			return new Integer(aliasID).hashCode() ^ 
				   new Integer(tastes[0].aliasID).hashCode() ^ 
				   new Integer(tastes[1].aliasID).hashCode() ^ 
				   new Integer(tastes[2].aliasID).hashCode() ^
				   (tmpTaste == null ? new Integer(0).hashCode() : new Integer(tmpTaste.aliasID).hashCode()) ^
				   new Short(hangStatus).hashCode();
		}
	}
	
	/**
	 * Add a taste to the food.
	 * @param taste the taste to be add
	 * @return a negative number if no available taste can be set,
	 * 		   a positive number if add taste successfully.
	 * 
	 */
	public int addTaste(Taste taste){
		/**
		 * Enumerate to check whether an available taste can be added
		 */
		int tastePos = 0;
		for(; tastePos < tastes.length; tastePos++){
			if(tastes[tastePos].aliasID == taste.aliasID){
				return tastePos;
			}
			if(tastes[tastePos].aliasID == Taste.NO_TASTE){
				break;
			}
		}
		
		if(tastePos < tastes.length){
			/**
			 * Add the taste to one of the three available tastes 
			 */
			try{
				//assign the taste id 
				tastes[tastePos].aliasID = taste.aliasID;
				//assign the taste preference 
				tastes[tastePos].preference = taste.preference;
				//assign the taste category
				tastes[tastePos].category = taste.category;
				//assign the calculate type
				tastes[tastePos].calc = taste.calc;
				//assign the taste price rate
				tastes[tastePos].setRate(taste.getRate());
				//assign the taste price
				tastes[tastePos].setPrice(taste.getPrice());
			}catch(ArrayIndexOutOfBoundsException e){}	
			
			//sort the tastes
			for(int i = 0; i < tastes.length; i++){
				for(int j = i + 1; j < tastes.length; j++){
					if(tastes[i].compare(tastes[j]) > 0){
						Taste tmpTaste = tastes[i];
						tastes[i] = tastes[j];
						tastes[j] = tmpTaste;
					}
				}
			}
			
			/**
			 * Calculate the taste price and preference
			 */
			//tasteNormalPref = Util.genTastePref(tastes);
			//setTasteNormalPrice(Util.genTastePrice(tastes, getPrice()));
			
			return tastePos;
			
		}else{
			return -1;
		}
	}
	
	/**
	 * Remove the specific taste from the food's taste list
	 * @param taste the taste to be deleted
	 * @return a negative number if the taste to be deleted is NOT exist,
	 * 		   a positive number if remove taste successfully.
	 */
	public int removeTaste(Taste taste){
		/**
		 * Enumerate to check whether the taste to delete is exist
		 */
		int tastePos = 0;
		for(; tastePos < tastes.length; tastePos++){
			if(taste.aliasID == tastes[tastePos].aliasID){
				break;
			}
		}
		
		if(tastePos < tastes.length){
			tastes[tastePos] = new Taste();
			//sort the tastes
			for(int i = 0; i < tastes.length; i++){
				for(int j = i + 1; j < tastes.length; j++){
					if(tastes[i].compare(tastes[j]) > 0){
						Taste tmpTaste = tastes[i];
						tastes[i] = tastes[j];
						tastes[j] = tmpTaste;
					}
				}
			}
			/**
			 * Calculate the taste price and preference
			 */
			//tasteNormalPref = Util.genTastePref(tastes);
			//setTasteNormalPrice(Util.genTastePrice(tastes, getPrice()));
			return tastePos;
		}else{
			return -1;
		}
	}

	int mTasteNormalPrice = Integer.MIN_VALUE; 						//the normal taste price to this food
	
	public void setTasteNormalPrice(Float price){
		if(price.floatValue() >= 0){
			mTasteNormalPrice = Util.float2Int(price);
		}else{
			mTasteNormalPrice = Integer.MIN_VALUE;
		}
	}
	
	/**
	 * There are two ways to get the normal taste price.
	 * One is to combine three normal tastes price.
	 * The other is to use the mTasteNormalPrice directly.
	 * Note that the mTasteNormalPrice is preferred.
	 * @return
	 */
	int getTasteNormalPriceInternal(){
		if(mTasteNormalPrice >= 0){
			return mTasteNormalPrice;
			
		}else{
			int tastePrice = 0;
			for(int i = 0; i < tastes.length; i++){
				if(tastes[i].aliasID != Taste.NO_TASTE){
					tastePrice += (tastes[i].calc == Taste.CALC_PRICE ? tastes[i].price : price * tastes[i].rate / 100);
				}
			}
			return tastePrice;
		}
	}
	
	public Float getTasteNormalPrice(){
		return Util.int2Float(getTasteNormalPriceInternal());
	}
	
	/**
	 * The taste price along with both normal and temporary taste.
	 * @return the taste price represented as an integer
	 */
	int getTastePriceInternal(){
		return getTasteNormalPriceInternal() + (tmpTaste == null ? 0 : tmpTaste.price);
	}
	
	/**
	 * The taste price along with both normal and temporary taste.
	 * @return the taste price represented as a Float
	 */
	public Float getTastePrice(){
		return Util.int2Float(getTastePriceInternal());
	}
	
	/**
	 * The unit price with taste to a specific food is as below.
	 * unit_price = (food_price + taste_price + tmp_taste_price) * discount 
	 * If taste price is calculated by rate, then
	 * taste_price = food_price * taste_rate
	 * @return the unit price represented as integer
	 */
	int getPriceWithTasteInternal(){
		//return price * discount / 100 + tastePrice();
		return (price + getTastePriceInternal()) * discount / 100;
	}	
	
	/**
	 * The unit price with taste to a specific food is as below.
	 * unit_price = food_price * discount + taste_price + tmp_taste_price
	 * If taste price is calculated by rate, then
	 * taste_price = food_price * taste_rate
	 * @return the unit price represented as a Float
	 */
	public Float getPriceWithTaste(){
		return Util.int2Float(getPriceWithTasteInternal());
	}
	
	/**
	 * Calculate the total price to this food without taste as below.
	 * <br>price = food_price * discount * count 
	 * @return the total price to this food
	 */
	public Float calcPrice(){
		return Util.int2Float((price * discount * count) / 10000);
	}	

	/**
	 * Calculate the total price to this food along with taste as below<br>.
	 * price = ((food_price + taste_price) * discount) * count 
	 * @return the total price to this food represented as integer
	 */
	int calcPriceWithTasteInternal(){
		return getPriceWithTasteInternal() * count / 100;
	}
	
	/**
	 * Calculate the total price to this food along with taste as below<br>.
	 * price = ((food_price + taste_price) * discount) * count 
	 * @return the total price to this food represented as float
	 */
	public Float calcPriceWithTaste(){
		return Util.int2Float(calcPriceWithTasteInternal());
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * price = unit_price * (1 - discount)
	 * @return the discount price to this food represented as an integer
	 */
	int calcDiscountPriceInternal(){
		if(discount != 100){
			return (price + getTastePriceInternal()) * count * (100 - discount) / 10000;
		}else{
			return 0;
		}
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * price = unit_price * (1 - discount)
	 * @return the discount price to this food represented as an float
	 */
	public Float calcDiscountPrice(){
		return Util.int2Float(calcDiscountPriceInternal());
	}	
	
	public OrderFood(){
		for(int i = 0; i < tastes.length; i++){
			tastes[i] = new Taste();
		}
	}

	public OrderFood(Food food){
		super(food.restaurantID,
			  food.foodID,
			  food.aliasID,
			  food.name,
			  food.getPrice(),
			  food.status,
			  food.pinyin,
			  food.tasteRefType,
			  food.desc,
			  food.image,
			  food.kitchen);
		popTastes = food.popTastes;
		childFoods = food.childFoods;
		
		for(int i = 0; i < tastes.length; i++){
			tastes[i] = new Taste();
		}
	}

	public OrderFood(OrderFood src){
		this((Food)src);
		this.orderDate = src.orderDate;
		this.waiter = src.waiter;
		this.payManner = src.payManner;
		this.hangStatus = src.hangStatus;
		this.isTemporary = src.isTemporary;
		this.discount = src.discount;
		this.count = src.count;
		this.isHurried = src.isHurried;
		this.table = new Table(src.table);
		this.tmpTaste = new Taste(src.tmpTaste);
		this.tastes = new Taste[src.tastes.length];
		for(int i = 0; i < this.tastes.length; i++){
			this.tastes[i] = new Taste(src.tastes[i]);
		}		
	}
	
	/**
	 * Check to see whether the food has temporary taste.
	 * @return true if the food has temporary taste, otherwise false
	 */
	public boolean hasTmpTaste(){
		return tmpTaste != null;
	}

	/**
	 * Check to see whether the food has taste(either normal {@link #hasNormalTaste()} or temporary {@link #hasTmpTaste()}).
	 * @return true if the food has taste, otherwise false
	 */
	public boolean hasTaste(){
		return hasNormalTaste() || hasTmpTaste();
	}	

	String mNormalTastePref;
	
	public void setNormalTastePref(String pref){
		mNormalTastePref = pref;
	}
	
	/**
	 * Check to see if the food has any normal taste.
	 * @return true if food has normal taste, otherwise false
	 */
	public boolean hasNormalTaste(){
		if(mNormalTastePref != null){
			return true;
			
		}else{
			boolean isNormalTasted = false;
			for(int i = 0; i < tastes.length; i++){
				if(tastes[i].aliasID != Taste.NO_TASTE){
					isNormalTasted = true;
					break;
				}
			}
			return isNormalTasted;			
		}
	}
	
	/**
	 * There are two ways to get the normal taste preference.
	 * One is to combine three normal tastes into a single string.
	 * The other is to use the mNormalTastePref directly.
	 * Note that the mNormalTastePref is preferred.
	 * @return the normal taste string to this food
	 */
	public String getNormalTastePref(){
		
		if(mNormalTastePref != null){
			return mNormalTastePref;
			
		}else{
			String tastePref = "";
			for(int i = 0; i < tastes.length; i++){
				if(tastes[i].aliasID != Taste.NO_TASTE && tastes[i].preference != null){
					if(tastePref.length() != 0){
						tastePref += ",";
					}
					tastePref += tastes[i].preference;
				}
			}			
			return tastePref.length() == 0 ? Taste.NO_PREFERENCE : tastePref;			
		}		
	}
	
	/**
	 * Get the taste combined with both normal and temporary preference.
	 * @return the combined taste preference
	 */
	public String getTastePref(){
		if(hasTmpTaste()){
			return (hasNormalTaste() ? getNormalTastePref() + "," : "") + tmpTaste.preference;
		}else{
			return getNormalTastePref();
		}
	}
	
	/**
	 * Return the order food string.
	 * The string format is as below.
	 * name-taste1,taste2,taste3
	 */
	public String toString(){

		String tastePref = getTastePref();
		
		return name + (tastePref.equals(Taste.NO_PREFERENCE) ? "" : ("-" + tastePref));
	}
}
