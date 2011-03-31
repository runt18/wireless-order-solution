package com.wireless.protocol;

/******************************************************
 * Design the print order 2 request looks like below
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2]
 * mode - PRINT
 * type - PRINT_BILL_2
 * seq - auto calculated and filled in
 * reserved - the meaning to each bit is as below
 * 			  [0] - PRINT_SYNC
 * 		      [1] - PRINT_ORDER_2
 * 			  [2] - PRINT_ORDER_DETAIL_2
 * 			  [3] - PRINT_RECEIPT_2
 * 			  [4..7] - Not Used
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * total_price[4] : restaurant[4] : table[2] : custom_num : food_num : <Food1> : <Food2>...
 * tatal_price[4] - 4-byte indicates the total price
 * 				   total_price[0] indicates the float part
 * 				   total_price[1..3] indicates the fixed part
 * restaurant[4] - 4-byte indicates the restaurant id
 * table[2] - 2-byte indicates the table id  
 * custom_num - 1-byte indicating the custom number for this table
 * food_num - 1-byte indicating the number of foods
 * <Food>
 * order_num[2] : food_len : food[len] : taste_len : taste[len]
 * order_num[2] - 2-byte indicating how many this foods are ordered
 * 			   order_num[0] - 1-byte indicates the float-point
 * 			   order_num[1] - 1-byte indicates the fixed-point
 * food_len : 1-byte indicates the length of food name
 * food[len] : the food name value
 * taste_len : 1-byte indicates the length of taste preference
 * taste[len] : the taste preference   
 *******************************************************/
public class ReqPrintOrder2 extends ReqOrderPackage{
	
	/**
	 * The print order 2 request 
	 * @param reqOrder the order to be print
	 * @param prtConf the configuration parameter to the print, the meaning to each bit is as below.<br>
	 *                [0] - PRINT_SYNC<br>
	 *                [1] - PRINT_ORDER_2<br>
	 *                [2] - PRINT_ORDER_DETAIL_2<br>
	 *                [3] - PRINT_RECEIPT_2<br>
	 *                [4..7] - Not Used			
	 */
	public ReqPrintOrder2(int restaurantID, Order reqOrder, byte prtConf){
		makePackage(restaurantID, reqOrder, prtConf);
	}
	
	private void makePackage(int restaurantID, Order reqOrder, byte prtConf){
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_BILL_2;
		header.reserved = prtConf;
		int bodyLen = 4 + /* total price takes up 4-byte */
					4 + /* restaurant id takes up 4-byte */
					2 + /* table id takes up 2-byte */
					1 + /* custom number takes up 1-byte */
					1; /* food amount takes up 1-byte */

		for(int i = 0; i < reqOrder.foods.length; i++){
			bodyLen += 1; /* each food's id takes up 1-byte */
			bodyLen += reqOrder.foods[i].name.getBytes().length; /* each food's name takes up length-byte */
			bodyLen += 1; /* each food's taste id takes up 1-byte */
			bodyLen += reqOrder.foods[i].taste.preference.getBytes().length; /* each food's taste takes up length-byte */
		}
		
		header.length[0] = (byte)(bodyLen & 0x000000FF) ;
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for body
		body = new byte[bodyLen];
		
		//assign the total price
		int totalPrice = reqOrder.totalPrice;
		body[0] = (byte)(totalPrice & 0x000000FF);
		body[1] = (byte)((totalPrice >> 8) & 0x000000FF);
		body[2] = (byte)((totalPrice >> 16) & 0x000000FF);
		body[3] = (byte)((totalPrice >> 24) & 0x000000FF);
		
		//assign the restaurant id
		body[4] = (byte)(restaurantID & 0x000000FF);
		body[5] = (byte)((restaurantID >> 8) & 0x000000FF);
		body[6] = (byte)((restaurantID >> 16) & 0x000000FF);
		body[7] = (byte)((restaurantID >> 24) & 0x000000FF);
		
		//assign the table id
		body[8] = (byte)(reqOrder.tableID & 0x00FF);
		body[9] = (byte)((reqOrder.tableID & 0xFF00) >> 8);
		
		//assign the custom number
		body[10] = (byte)(reqOrder.customNum & 0x000000FF);
		
		//assign the number of foods
		body[11] = (byte)(reqOrder.foods.length & 0x000000FF);
		
		//assign each food item
		int index = 12;
		for(int i = 0; i < reqOrder.foods.length; i++){
			//assign the order count
			body[index] = (byte)(reqOrder.foods[i].count & 0x000000FF);
			body[index + 1] = (byte)((reqOrder.foods[i].count & 0x0000FF00) >> 8);
			index += 2;
			
			byte[] foodName = reqOrder.foods[i].name.getBytes();
			//assign the length of food name
			body[index] = (byte)(foodName.length & 0x000000FF);
			index++;
			
			//assign the food name
			System.arraycopy(foodName, 0, body, index, foodName.length);
			index += foodName.length;
			
			byte[] foodTaste = reqOrder.foods[i].taste.preference.getBytes();
			//assign the length of food taste
			body[index] = (byte)(foodTaste.length & 0x000000FF);
			index++;
			
			//assign the food taste
			System.arraycopy(foodTaste, 0, body, index, foodTaste.length);
			index += foodTaste.length;

		}	
	}
}
