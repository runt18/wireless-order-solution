package com.wireless.protocol;

import com.wireless.protocol.Reserved;

/******************************************************
 * Design the insert order request looks like below
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2]
 * mode - ORDER_BUSSINESS
 * type - INSERT_ORDER
 * seq - auto calculated and filled in
 * reserved - 0x00
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Order>
 * <Order>
 * table[2] : custom_num : food_num : <Food1> : <Food2>...
 * table[2] - 2-byte indicates the table id 
 * custom_num - 1-byte indicating the custom number for this table
 * food_num - 1-byte indicating the number of foods
 * <Food>
 * food_id[2] : order_num[2] : taste_id
 * food_id[2] - 2-byte indicating the food's id
 * order_num[2] - 2-byte indicating how many this foods are ordered
 * 			   order_num[0] - 1-byte indicates the float-point
 * 			   order_num[1] - 1-byte indicates the fixed-point
 * taste_id - 1-byte indicates the taste preference id
 *******************************************************/
public class ReqInsertOrder extends ReqOrderPackage {

	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reqConf indicates the request configuration, like sync or asyn print 
	 */
	public ReqInsertOrder(Order reqOrder, byte type, byte reqConf){
		makePackage(reqOrder, type, reqConf);
	}
	
	/**
	 * Make the insert or update order request package with default request configuration
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 */
	//public ReqInsertOrder(Order reqOrder, byte type){
	//	makePackage(reqOrder, type, Reserved.DEFAULT_CONF);
	//}  
	
	/**
	 * Make the insert order request package with default request configuration
	 * @param reqOrder the order detail information
	 */
	public ReqInsertOrder(Order reqOrder){
		makePackage(reqOrder, Type.INSERT_ORDER, Reserved.DEFAULT_CONF);
	}	
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reqConf indicates the request configuration, like sync or asyn print 
	 */
	private void makePackage(Order reqOrder, byte type, byte reqConf){
		if(type != Type.INSERT_ORDER && type != Type.UPDATE_ORDER)
			throw new IllegalArgumentException();
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = type;
		header.reserved = reqConf;
		//calculate the body's length
		int bodyLen = 2 + /* table id takes up 2-byte */
					1 + /* custom number takes up 1-byte */ 
					4 + /* price takes up 4-byte */ 
					1 + /* food number takes up 1-byte */
					reqOrder.foods.length * 5; /* each food takes up 5-byte*/
		header.length[0] = (byte)(bodyLen & 0x000000FF) ;
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		body = new byte[bodyLen];
		//assign the table id
		body[0] = (byte)(reqOrder.tableID & 0x00FF);
		body[1] = (byte)((reqOrder.tableID & 0xFF00) >> 8);

		//assign the custom number
		body[2] = (byte)(reqOrder.customNum & 0x000000FF);
		
		//assign the number of foods
		body[3] = (byte)(reqOrder.foods.length & 0x000000FF);
		
		//assign each order food's id and count
		int foodIndex = 5;
		for(int i = 0; i < reqOrder.foods.length; i++){
			body[foodIndex] = (byte)(reqOrder.foods[i].alias_id & 0x000000FF);
			body[foodIndex + 1] = (byte)((reqOrder.foods[i].alias_id & 0x0000FF00) >> 8);
			body[foodIndex + 2] = (byte)(reqOrder.foods[i].getCount() & 0x000000FF);
			body[foodIndex + 3] = (byte)((reqOrder.foods[i].getCount() & 0x0000FF00) >> 8);
			body[foodIndex + 4] = (byte)(reqOrder.foods[i].taste.alias_id & 0x00FF);
			foodIndex += 5;
		}		
	}
	
	/******************************************************
	 * In the case insert order successfully
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] - 0x00, 0x00
	 *******************************************************/
	
	/******************************************************
	 * In the case insert order not successfully
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - NAK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] - 0x00, 0x00
	 *******************************************************/
}
