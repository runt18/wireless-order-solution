package com.wireless.protocol;

public class OrderFood extends Food {
	public long orderDate;
	public String waiter;
	public int payManner = Order.MANNER_CASH;
	
	public static final int FOOD_NORMAL = 0;		/* 普通 */
	public static final int FOOD_HANG_UP = 1;		/* 叫起 */
	public static final int FOOD_IMMEDIATE = 2;		/* 即起 */
	public short hangStatus = FOOD_NORMAL;			//the hang status to the food
	
	TasteGroup tasteGroup;					//the taste group to this order food
	
	public Table table = new Table();				//the table this order food belongs to
	
	public boolean isTemporary = false;				//indicates whether the food is temporary
	
	public int getAliasId(){
		if(isTemporary){
			return (name.hashCode() + price) % 65535;
		}else{
			return this.aliasID;
		}
	}
	
	/**
	 * The value of discount ranges from 0.00 through 1.00
	 * So the real price should be divided 100 at last. 
	 */
	private int discount = 100;	//the discount to this food 
	
	/**
	 * Set the discount for internal.
	 * @param discount the discount to set
	 */
	void setDiscountInternal(int discount){
		//The discount remains as before in case of temporary, gift or special
		if(isTemporary || isGift() || isSpecial()){
			//this.discount = 100;
		}else{
			this.discount = discount;
		}
	}
	
	int getDiscountInternal(){
		return discount;
	}
	
	public void setDiscount(Float discount){
		setDiscountInternal(Util.float2Int(discount));
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
			return restaurantID == food.restaurantID && 
				   aliasID == food.aliasID && 
				   (tasteGroup != null ? tasteGroup.equals(food.tasteGroup) : food.tasteGroup == null);
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
				   (tasteGroup != null ? tasteGroup.hashCode() : 0)^
				   new Short(hangStatus).hashCode();
		}
	}
	
	/**
	 * Check to see if the order food has taste(either normal taste or temporary taste).
	 * @return true if the order food has taste, otherwise false
	 */
	public boolean hasTaste(){
		return tasteGroup == null ? false : tasteGroup.hasTaste();
	}
	
	/**
	 * Check to see if the order food has normal taste.
	 * @return true if the order food has normal taste, otherwise false.
	 */
	public boolean hasNormalTaste(){
		return tasteGroup == null ? false : tasteGroup.hasNormalTaste();
	}
	
	/**
	 * Check to see if the order food has temporary taste.
	 * @return true if the order food has temporary taste, otherwise false
	 */
	public boolean hasTmpTaste(){
		return tasteGroup == null ? false : tasteGroup.hasTmpTaste();
	}
	
	/**
	 * The unit price with taste before discount to a specific food.
	 * @return The unit price represented as integer.
	 */
	int getPriceBeforeDiscountInternal(){
		return (price + (tasteGroup == null ? 0 : tasteGroup.getTastePriceInternal()));
	}
	
	/**
	 * Calculate the price with taste before discount to a specific food.
	 * @return The price represented as integer.
	 */
	int calcPriceBeforeDiscountInternal(){
		return getPriceBeforeDiscountInternal() * count / 100;
	}
	
	/**
	 * Calculate the price with taste before discount to a specific food.
	 * @return The price represented as float.
	 */	
	Float calcPriceBeforeDiscount(){
		return Util.int2Float(calcPriceBeforeDiscountInternal());
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
		return (price + (tasteGroup == null ? 0 : tasteGroup.getTastePriceInternal())) * discount / 100;
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
	 * Calculate the pure total price to this food without taste as below.
	 * <br>price = food_price * discount * count 
	 * @return the total price to this food
	 */
	public Float calcPurePrice(){
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
			return (price + (tasteGroup == null ? 0 : tasteGroup.getTastePriceInternal())) * count * (100 - discount) / 10000;
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

	}

	public OrderFood(Food food){
		super(food.restaurantID,
			  food.foodID,
			  food.aliasID,
			  food.name,
			  food.getPrice(),
			  food.statistics,
			  food.status,
			  food.pinyin,
			  food.tasteRefType,
			  food.desc,
			  food.image,
			  food.kitchen);
		popTastes = food.popTastes;
		childFoods = food.childFoods;
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
		if(src.table != null){
			this.table = new Table(src.table);
		}
		tasteGroup = src.tasteGroup;
	}
	
	public TasteGroup makeTasteGroup(){
		tasteGroup = new TasteGroup(this, null, null);
		return tasteGroup;
	}
	
	public TasteGroup makeTasetGroup(Taste[] normal, Taste tmp){
		tasteGroup = new TasteGroup(this, normal, tmp);
		return tasteGroup;
	}
	
	public TasteGroup makeTasteGroup(int groupID, Taste normal, Taste tmp){
		tasteGroup = new TasteGroup(groupID, normal, tmp);
		return tasteGroup;
	}
	
	public void clearTasetGroup(){
		tasteGroup = null;
	}
	
	public TasteGroup getTasteGroup(){
		return tasteGroup;
	}
	
	public void setTasteGroup(TasteGroup tg){
		if(tg != null){
			tasteGroup = tg;
			tasteGroup.setAttachedFood(this);
		}
	}
	
	/**
	 * Return the order food string.
	 * The string format is as below.
	 * name-taste1,taste2,taste3
	 */
	public String toString(){
		return name + (hasTaste() ? ("-" + tasteGroup.getTastePref()) : "");
	}
}
