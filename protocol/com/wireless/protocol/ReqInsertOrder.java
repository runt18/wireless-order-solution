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
 * table[2] : custom_num : food_num : <Food1> : <Food2>... : original_table[2] 
 * table[2] - 2-byte indicates the table id 
 * custom_num - 1-byte indicating the custom number for this table
 * food_num - 1-byte indicating the number of foods
 * <Food>
 * food_id[2] : order_num[2] : taste_id : kitchen
 * food_id[2] - 2-byte indicating the food's id
 * order_num[2] - 2-byte indicating how many this foods are ordered
 * 			   order_num[0] - 1-byte indicates the float-point
 * 			   order_num[1] - 1-byte indicates the fixed-point
 * taste_id - 1-byte indicates the taste preference id
 * kitchen - the kitchen to this food
 * 
 * origianal_table[2] - 2-bytes indicates the original table id,
 *                      These two bytes are used for table transferred
 *  
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
		int bodyLen = 2 + /* table id takes up 2 bytes */
					1 + /* custom number takes up 1 byte */ 
					1 + /* food number takes up 1 byte */
					reqOrder.foods.length * 6 + /* each food takes up 6 bytes*/
					2; /* original table id takes up 2 bytes */
		header.length[0] = (byte)(bodyLen & 0x000000FF) ;
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		body = new byte[bodyLen];
		//assign the table id
		body[0] = (byte)(reqOrder.table_id & 0x00FF);
		body[1] = (byte)((reqOrder.table_id & 0xFF00) >> 8);

		//assign the custom number
		body[2] = (byte)(reqOrder.custom_num & 0x000000FF);
		
		//assign the number of foods
		body[3] = (byte)(reqOrder.foods.length & 0x000000FF);
		
		//assign each order food's id and count
		int offset = 4;
		for(int i = 0; i < reqOrder.foods.length; i++){
			body[offset] = (byte)(reqOrder.foods[i].alias_id & 0x000000FF);
			body[offset + 1] = (byte)((reqOrder.foods[i].alias_id & 0x0000FF00) >> 8);
			body[offset + 2] = (byte)(reqOrder.foods[i].count & 0x000000FF);
			body[offset + 3] = (byte)((reqOrder.foods[i].count & 0x0000FF00) >> 8);
			body[offset + 4] = (byte)(reqOrder.foods[i].taste.alias_id & 0x00FF);
			body[offset + 5] = (byte)(reqOrder.foods[i].kitchen);
			offset += 6;
		}		
		
		//assign the original table id
		body[offset] = (byte)(reqOrder.originalTableID & 0x000000FF);
		body[offset + 1] = (byte)((reqOrder.originalTableID & 0x0000FF00) >> 8);
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
