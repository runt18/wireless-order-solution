package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

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
 * <Body>
 * print_type[2] : table[2] : table2[2] : category : custom_num : food_num : 
 * <Food1> : <Food2>... :
 * <TmpFood1> : <TmpFood2>... : 
 * original_table[2] 
 * 
 * print_type[2] - 2-byte indicates the print type
 * table[2] - 2-byte indicates the table id
 * table_2[2] - 2-byte indicates the 2nd table id  
 * category - 1-byte indicating the category to this order
 * custom_num - 1-byte indicating the custom number for this table
 * food_num - 1-byte indicating the number of foods
 * 
 * <Food>
 * is_temp(0) : food_id[2] : order_amount[2] : taste_id[2] : taste_id2[2] : taste_id3[2] : 
 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
 * kitchen : hang_status : is_hurried
 * is_temp(0) - "0" means this food is NOT temporary
 * food_id[2] - 2-byte indicating the food's id
 * order_amount[2] - 2-byte indicating how many this foods are ordered
 * taste_id[2] - 2-byte indicates the 1st taste preference id
 * taste_id2[2] - 2-byte indicates the 2nd taste preference id
 * taste_id3[2] - 2-byte indicates the 3rd taste preference id
 * len_tmp_taste - 1-byte indicates the length of temporary taste
 * tmp_taste[n] - the temporary taste value
 * tmp_taste_alias[2] - 2-byte indicates the alias id to this temporary taste
 * tmp_taste_price[4] - 4-byte indicates the price to this temporary taste
 * kitchen - the kitchen to this food
 * hang_status - the hang status to the food
 * is_hurried - indicates whether the food is hurried
 * 
 * <TmpFood>
 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : is_hurried : len : food_name[len] 
 * is_temp(1) - "1" means this food is temporary
 * food_id[2] - 2-byte indicating the food's id
 * order_amount[2] - 2-byte indicating how many this foods are ordered
 * unit_price[3] - 3-byte indicating the unit price to this food
 * hang_status - the hang status to the food
 * is_hurried - indicates whether the food is hurried
 * len - the length of food's name
 * food_name[len] - the value of the food name
 * 
 * origianal_table[2] - 2-bytes indicates the original table id,
 *                      These two bytes are used for table transferred
 *  
 *******************************************************/
public class ReqInsertOrder extends ReqPackage {

	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reqConf indicates the request configuration, like sync or asyn print 
	 */
	public ReqInsertOrder(Order reqOrder, byte type, short reqConf) throws UnsupportedEncodingException{
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
	public ReqInsertOrder(Order reqOrder) throws UnsupportedEncodingException{
		makePackage(reqOrder, Type.INSERT_ORDER, Reserved.DEFAULT_CONF);
	}	
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reqConf indicates the request configuration, like sync or asyn print 
	 */
	private void makePackage(Order reqOrder, byte type, int reqConf) throws UnsupportedEncodingException{
		if(type != Type.INSERT_ORDER && type != Type.UPDATE_ORDER)
			throw new IllegalArgumentException();
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = type;

		//calculate the amount of bytes that the food list needs
		int foodLen = 0;
		for(int i = 0; i < reqOrder.foods.length; i++){
			if(reqOrder.foods[i].isTemporary){
				/* is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : is_hurried : len : food_name[len] */
				foodLen += 1 + 2 + 2 + 3 + 1 + 1 + 1 + reqOrder.foods[i].name.getBytes("UTF-8").length;
			}else{
				/**
				 * is_temp(0) : food_id[2] : order_amount[2] : taste_id[2] : taste_id2[2] : taste_id3[2] : 
				 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
				 * kitchen : hang_status : is_hurried 
				 **/
				foodLen += 1 + /* is_temp(0) */
						   2 + /* food_id[2] */ 
						   2 + /* order_amount[2] */
						   2 + /* taste_id[2] */
						   2 + /* taste_id2[2] */
						   2 + /* taste_id3[2] */
						   1 + /* len_tmp_taste */
						   (reqOrder.foods[i].tmpTaste == null ? 0 : reqOrder.foods[i].tmpTaste.preference.getBytes("UTF-8").length) + /* tmp_taset */
						   2 + /* tmp_taste_alias[2] */
						   4 + /* tmp_taste_price[4] */
						   1 + /* kitchen */
						   1 + /* hang_status */
						   1;  /* is_hurried */ 
			}
		}
		
		//calculate the body's length
		int bodyLen = 2 + /* print type takes up 2 bytes */
					2 + /* table id takes up 2 bytes */
					2 + /* 2nd table id takes up 2 bytes */
					1 + /* category takes up 1 byte */
					1 + /* custom number takes up 1 byte */ 
					1 + /* food number takes up 1 byte */
					foodLen + /* the amount of bytes that food list needs */
					2; /* original table id takes up 2 bytes */
		header.length[0] = (byte)(bodyLen & 0x000000FF) ;
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		body = new byte[bodyLen];
		
		//assign the print type
		body[0] = (byte)(reqConf & 0x00FF);
		body[1] = (byte)((reqConf & 0xFF00) >> 8);
		
		//assign the table id
		body[2] = (byte)(reqOrder.destTbl.aliasID & 0x00FF);
		body[3] = (byte)((reqOrder.destTbl.aliasID & 0xFF00) >> 8);

		//assign the 2nd table id
		body[4] = (byte)(reqOrder.destTbl2.aliasID & 0x00FF);
		body[5] = (byte)((reqOrder.destTbl2.aliasID & 0xFF00) >> 8);
		
		//assign the category
		body[6] = (byte)(reqOrder.category & 0x00FF); 
		
		//assign the custom number
		body[7] = (byte)(reqOrder.custom_num & 0x000000FF);
		
		//assign the number of foods
		body[8] = (byte)(reqOrder.foods.length & 0x000000FF);
		
		//assign each order food's id and count
		int offset = 9;
		for(int i = 0; i < reqOrder.foods.length; i++){
			if(reqOrder.foods[i].isTemporary){
				byte[] nameBytes = reqOrder.foods[i].name.getBytes("UTF-8");
				/**
				 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : is_hurried : len : food_name[len]
				 */
				//assign the temporary flag
				body[offset] = 1;
				//assign the food id
				body[offset + 1] = (byte)(reqOrder.foods[i].aliasID & 0x000000FF);
				body[offset + 2] = (byte)((reqOrder.foods[i].aliasID & 0x0000FF00) >> 8);
				//assign the order amount
				body[offset + 3] = (byte)(reqOrder.foods[i].count & 0x000000FF);
				body[offset + 4] = (byte)((reqOrder.foods[i].count & 0x0000FF00) >> 8);
				//assign the unit price
				body[offset + 5] = (byte)(reqOrder.foods[i].price & 0x000000FF);
				body[offset + 6] = (byte)((reqOrder.foods[i].price & 0x0000FF00) >> 8);
				body[offset + 7] = (byte)((reqOrder.foods[i].price & 0x00FF0000) >> 16);
				//assign the hang status
				body[offset + 8] = (byte)reqOrder.foods[i].hangStatus;
				//assign the hurried flag
				body[offset + 9] = (byte)(reqOrder.foods[i].isHurried ? 1 : 0);
				//assign the amount of food name's byte
				body[offset + 10] = (byte)(nameBytes.length);
				//assign the value of food name
				for(int cnt = 0; cnt < nameBytes.length; cnt++){
					body[offset + 11 + cnt] = nameBytes[cnt];
				}
				
				offset += 1 + 2 + 2 + 3 + 1 + 1 + 1 + nameBytes.length;
				
			}else{
				/**
				 * is_temp(0) : food_id[2] : order_amount[2] : taste_id[2] : taste_id2[2] : taste_id3[2] :
				 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] 
				 * kitchen : hang_status : is_hurried
				 */
				//assign the temporary flag
				body[offset] = 0;
				offset += 1;
				
				//assign the food id
				body[offset] = (byte)(reqOrder.foods[i].aliasID & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].aliasID & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the order amount
				body[offset] = (byte)(reqOrder.foods[i].count & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].count & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the 1st taste id
				body[offset] = (byte)(reqOrder.foods[i].tastes[0].aliasID & 0x00FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].tastes[0].aliasID & 0xFF00) >> 8);
				offset += 2;
				
				//assign the 2nd taste id
				body[offset] = (byte)(reqOrder.foods[i].tastes[1].aliasID & 0x00FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].tastes[1].aliasID & 0xFF00) >> 8);
				offset += 2;
				
				//assign the 3rd taste id
				body[offset] = (byte)(reqOrder.foods[i].tastes[2].aliasID & 0x00FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].tastes[2].aliasID & 0xFF00) >> 8);
				offset += 2;
				
				if(reqOrder.foods[i].tmpTaste != null){
					//assign the length of temporary taste
					byte[] tmpTasteBytes = reqOrder.foods[i].tmpTaste.preference.getBytes("UTF-8");
					body[offset] = (byte)(tmpTasteBytes.length);
					offset += 1;

					//assign the temporary taste value
					for(int cnt = 0; cnt < tmpTasteBytes.length; cnt++){
						body[offset + cnt] = tmpTasteBytes[cnt];
					}
					offset += tmpTasteBytes.length;
					
					//assign the alias to temporary taste
					body[offset] = (byte)(reqOrder.foods[i].tmpTaste.aliasID & 0x00FF);
					body[offset + 1] = (byte)((reqOrder.foods[i].tmpTaste.aliasID & 0xFF00) >> 8);
					offset += 2;
					
					//assign the price to temporary taste
					body[offset] = (byte)(reqOrder.foods[i].tmpTaste.price & 0x000000FF);
					body[offset + 1] = (byte)((reqOrder.foods[i].tmpTaste.price & 0x0000FF00) >> 8);
					body[offset + 2] = (byte)((reqOrder.foods[i].tmpTaste.price & 0x00FF0000) >> 16);
					body[offset + 3] = (byte)((reqOrder.foods[i].tmpTaste.price & 0xFF000000) >> 24);
					offset += 4;
					
				}else{
					//assign the length of temporary taste value
					body[offset] = 0x00;
					//assign the temporary taste alias
					body[offset + 1] = 0x00;
					body[offset + 2] = 0x00;
					//assign the temporary taste price
					body[offset + 3] = 0x00;
					body[offset + 4] = 0x00;
					body[offset + 5] = 0x00;
					body[offset + 6] = 0x00;
					
					offset += 7;
				}
				
				//assign the kitchen alias
				body[offset] = (byte)(reqOrder.foods[i].kitchen.aliasID);
				offset += 1;
				
				//assign the hang status
				body[offset] = (byte)reqOrder.foods[i].hangStatus;
				offset += 1;
				
				//assign the hurried flag
				body[offset] = (byte)(reqOrder.foods[i].isHurried ? 1 : 0);
				offset += 1;
			}
		}		
		
		//assign the original table id
		body[offset] = (byte)(reqOrder.srcTbl.aliasID & 0x000000FF);
		body[offset + 1] = (byte)((reqOrder.srcTbl.aliasID & 0x0000FF00) >> 8);
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
