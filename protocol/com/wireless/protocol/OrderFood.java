package com.wireless.protocol;

public class OrderFood extends Food {
	public long orderDate;
	public int payManner = Order.MANNER_CASH;
	
	public static final int FOOD_NORMAL = 0;		/* 正常 */
	public static final int FOOD_HANG_UP = 1;		/* 叫起 */
	public static final int FOOD_IMMEDIATE = 2;		/* 即起 */
	public short hangStatus = FOOD_NORMAL;			//the hang status to the food
	
	public Taste[] tastes = new Taste[3];			//three tastes the food can consist of
	
	public boolean isTemporary = false;				//indicates whether the food is temporary

	int tastePrice = 0; //the taste price to this food
	
	public void setTastePrice(Float price){
		tastePrice = Util.float2Int(price);
	}
	
	public Float getTastePrice(){
		return Util.int2Float(tastePrice);
	}
	
	/**
	 * Since a food can consist of three tastes at most,
	 * combine the taste into a signal string.
	 */
	public String tastePref = Taste.NO_PREFERENCE;
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = (food_price * discount + taste_price) * count 
	 * @return the total price to this food
	 */
	public Float calcPrice2(){
		return Util.int2Float(price2() * count / 100);
	}
	
	/**
	 * The value of discount ranges from 0.00 through 1.00
	 * So the real price should be divided 100 at last. 
	 */
	byte discount = 100;	//the discount to this food 
	
	public void setDiscount(Float _discount){
		discount = (byte)Util.float2Int(_discount);
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
	 * There are three ways to determine whether two foods is the same as each other.
	 * 1 - If one food is temporary while the other NOT, means they are NOT the same.
	 * 2 - If both of foods are temporary, check to see whether their names and price are the same.
	 *     They are the same if both name and price is matched.
	 * 3 - If both of foods are NOT temporary, check to see their food, all tastes id and hang status.
	 *     They are the same if all of the things above are matched.
	 */
	public boolean equals(Object obj){
		OrderFood food = (OrderFood)obj;
		if(isTemporary != food.isTemporary){
			return false;
		}else if(isTemporary && food.isTemporary){
			return name.equals(food.name) && (price == food.price) && (hangStatus == food.hangStatus);
		}else{
			return alias_id == food.alias_id &&
				   tastes[0].alias_id == food.tastes[0].alias_id &&
				   tastes[1].alias_id == food.tastes[1].alias_id &&
				   tastes[2].alias_id == food.tastes[2].alias_id &&
				   hangStatus == food.hangStatus;
		}
	}

	/**
	 * Two foods are the same if both food and taste id is matched
	 * @param food
	 * @return
	 */
	public boolean equals2(OrderFood food){
		return alias_id == food.alias_id &&
		   tastes[0].alias_id == food.tastes[0].alias_id &&
		   tastes[1].alias_id == food.tastes[1].alias_id &&
		   tastes[2].alias_id == food.tastes[2].alias_id ;
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
			if(tastes[tastePos].alias_id == Taste.NO_TASTE){
				break;
			}
		}
		
		if(tastePos < tastes.length){
			/**
			 * Add the taste to one of the three available tastes 
			 */
			try{
				//assign the taste id 
				tastes[tastePos].alias_id = taste.alias_id;
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
			tastePref = Util.genTastePref(tastes);
			setTastePrice(Util.genTastePrice(tastes, getPrice()));
			
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
			if(taste.alias_id == tastes[tastePos].alias_id){
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
			tastePref = Util.genTastePref(tastes);
			setTastePrice(Util.genTastePrice(tastes, getPrice()));
			return tastePos;
		}else{
			return -1;
		}
	}

	/**
	 * The 2nd unit price to food is as below.
	 * unit_price = food_price * discount + taste_price
	 * @return the unit price represented as Float 
	 */
	public Float getPrice2(){
		return Util.int2Float(price2());
	}
	
	/**
	 * The 2nd unit price to food is as below.
	 * unit_price = food_price * discount + taste_price
	 * If taste price is calculated by rate, then
	 * taste_price = food_price * taste_rate
	 * @return the unit price represented as an integer
	 */
	int price2(){
		return price * discount / 100 + tastePrice;
	}
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = food_price * discount * count 
	 * @return the total price to this food
	 */
	public Float calcPrice(){
		return Util.int2Float((price * discount * count) / 10000);
	}
	
	public OrderFood(){
		for(int i = 0; i < tastes.length; i++){
			tastes[i] = new Taste();
		}
	}

	public OrderFood(Food food){
		super(food.food_id,
			  food.alias_id,
			  food.name,
			  food.getPrice(),
			  food.kitchen,
			  food.status,
			  food.pinyin);
		for(int i = 0; i < tastes.length; i++){
			tastes[i] = new Taste();
		}
	}
}
