package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

/******************************************************
 * Design the insert order request looks like below
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2]
 * mode - ORDER_BUSSINESS
 * type - INSERT_ORDER
 * seq - auto calculated and filled in
 * reserved - 0x00
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * print_type[2] : src_tbl[2] : dest_tbl[2] : dest_tbl_2[2] : order_date[8] : category : custom_num : 
 * food_amount : <Food1> : <Food2>... : <TmpFood1> : <TmpFood2>... : 
 * 
 * print_type[2] - 2-byte indicates the print type
 * src_tbl[2] - 2-byte indicates the alias to source table
 * dest_tbl[2] - 2-byte indicates the alias to destination table
 * dest_tbl_2[2] - 2-byte indicates the alias to 2nd destination table
 * order_date[8] - 8-byte indicates the last modified order date
 * category - 1-byte indicates the category to this order  
 * custom_num - 1-byte indicating the custom number for this table
 * food_amount - 1-byte indicating the amount of foods
 * 
 * <Food>
 * is_temp(0) : food_id[2] : order_num[2] :
 * normal_taste_amount : normal_taste_alias[2] : normal_taste_alias2[2]... : 
 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
 * kitchen : hang_status : is_hurried
 * is_temp(0) - "0" means this food is NOT temporary
 * food_id[2] - 2-byte indicating the food's id
 * order_num[2] - 2-byte indicating how many this foods are ordered
 * 			   order_num[0] - 1-byte indicates the float-point
 * 			   order_num[1] - 1-byte indicates the fixed-point
 * normal_taste_amount - 1-byte indicates the amount to normal taste
 * normal_taste_alias[2] - 2-byte indicates the taste alias id
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
 * food_name[len] - the value of the food name *
 *******************************************************/
public class ReqInsertOrder extends ReqPackage {

	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reqConf indicates the request configuration, like sync or asyn print 
	 */
	public ReqInsertOrder(Order reqOrder, byte type) throws UnsupportedEncodingException{
		makePackage(reqOrder, type);
	}	
	
	/**
	 * Make the insert order request package with default request configuration
	 * @param reqOrder the order detail information
	 */
	public ReqInsertOrder(Order reqOrder) throws UnsupportedEncodingException{
		makePackage(reqOrder, Type.INSERT_ORDER);
	}	
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 */
	private void makePackage(Order reqOrder, byte type) throws UnsupportedEncodingException{
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
				 * is_temp(0) : food_id[2] : order_amount[2] : normal_taste_amount : normal_taste_alias[2] ... normal_taste_alias2[2] : 
				 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
				 * kitchen : hang_status : is_hurried 
				 **/
				Taste[] normalTastes = reqOrder.foods[i].tasteGroup == null ? null : reqOrder.foods[i].tasteGroup.mNormalTastes;
				Taste tmpTaste = reqOrder.foods[i].tasteGroup == null ? null : reqOrder.foods[i].tasteGroup.mTmpTaste;
				foodLen += 1 + /* is_temp(0) */
						   2 + /* food_id[2] */ 
						   2 + /* order_amount[2] */
						   1 + /* normal_taste_amount */
						   (normalTastes == null ? 0 : normalTastes.length * 2) + /* each normal taste alias takes up 2-byte */
						   1 + /* len_tmp_taste */
						   (tmpTaste == null ? 0 : tmpTaste.preference.getBytes("UTF-8").length) + /* the name to tmp_taste */
						   2 + /* tmp_taste_alias[2] */
						   4 + /* tmp_taste_price[4] */
						   1 + /* kitchen */
						   1 + /* hang_status */
						   1;  /* is_hurried */ 
			}
		}
		
		//calculate the body's length
		int bodyLen = 2 + 		/* print type takes up 2 bytes */
					  2 +		/* alias to source table takes up 2 bytes */
					  2 + 		/* alias to destination takes up 2 bytes */
					  2 + 		/* alias to 2nd destination table id takes up 2 bytes */
					  8 + 		/* last modified date takes up 8-byte */
					  1 + 		/* category takes up 1 byte */
					  1 + 		/* custom number takes up 1 byte */ 
					  1 + 		/* food number takes up 1 byte */
					  foodLen; 	/* the amount of bytes that food list needs */
		header.length[0] = (byte)(bodyLen & 0x000000FF) ;
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		body = new byte[bodyLen];

		int offset = 0;

		//assign the print type
		body[offset] = 0x00;
		body[offset + 1] = 0x00;
		offset += 2;
		
		//assign the alias to source table
		body[offset] = (byte)(reqOrder.srcTbl.aliasID & 0x00FF);
		body[offset + 1] = (byte)((reqOrder.srcTbl.aliasID & 0xFF00) >> 8);
		offset += 2;
		
		//assign the alias to destination table
		body[offset] = (byte)(reqOrder.destTbl.aliasID & 0x00FF);
		body[offset + 1] = (byte)((reqOrder.destTbl.aliasID & 0xFF00) >> 8);
		offset += 2;

		//assign the alias to 2nd destination table
		body[offset] = (byte)(reqOrder.destTbl2.aliasID & 0x00FF);
		body[offset + 1] = (byte)((reqOrder.destTbl2.aliasID & 0xFF00) >> 8);
		offset += 2;
		
		//assign the last modified date
		body[offset] = (byte)(reqOrder.orderDate & 0x00000000000000FFL);
		body[offset + 1] = (byte)((reqOrder.orderDate & 0x000000000000FF00L) >> 8);
		body[offset + 2] = (byte)((reqOrder.orderDate & 0x0000000000FF0000L) >> 16);
		body[offset + 3] = (byte)((reqOrder.orderDate & 0x00000000FF000000L) >> 24);
		body[offset + 4] = (byte)((reqOrder.orderDate & 0x000000FF00000000L) >> 32);
		body[offset + 5] = (byte)((reqOrder.orderDate & 0x0000FF0000000000L) >> 40);
		body[offset + 6] = (byte)((reqOrder.orderDate & 0x00FF000000000000L) >> 48);
		body[offset + 7] = (byte)((reqOrder.orderDate & 0xFF00000000000000L) >> 56);
		offset += 8;
		
		//assign the category
		body[offset] = (byte)(reqOrder.category & 0x00FF);
		offset += 1;
		
		//assign the custom number
		body[offset] = (byte)(reqOrder.customNum & 0x000000FF);
		offset += 1;
		
		//assign the number of foods
		body[offset] = (byte)(reqOrder.foods.length & 0x000000FF);
		offset += 1;
		
		//assign each order food's id and count
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
				
				
				//assign each alias id to normal tastes
				if(reqOrder.foods[i].hasNormalTaste()){
					
					Taste[] normalTastes = reqOrder.foods[i].tasteGroup.mNormalTastes;
					
					//assign the amount to normal tastes
					body[offset] = (byte)normalTastes.length;
					offset += 1;
					
					for(int j = 0; j < normalTastes.length; j++){
						body[offset] = (byte)(normalTastes[j].aliasID & 0x00FF);
						body[offset + 1] = (byte)((normalTastes[j].aliasID & 0xFF00) >> 8);
						offset += 2;
					}
				}else{
					body[offset] = 0;
					offset += 1;
				}		

				if(reqOrder.foods[i].hasTmpTaste()){
					
					Taste tmpTaste = reqOrder.foods[i].tasteGroup.mTmpTaste;
					
					//assign the length of temporary taste
					byte[] bytesToTmpTaste = tmpTaste.preference.getBytes("UTF-8");
					body[offset] = (byte)(bytesToTmpTaste.length);
					offset += 1;

					//assign the temporary taste value
					for(int j = 0; j < bytesToTmpTaste.length; j++){
						body[offset + j] = bytesToTmpTaste[j];
					}
					offset += bytesToTmpTaste.length;
					
					//assign the alias to temporary taste
					body[offset] = (byte)(tmpTaste.aliasID & 0x00FF);
					body[offset + 1] = (byte)((tmpTaste.aliasID & 0xFF00) >> 8);
					offset += 2;
					
					//assign the price to temporary taste
					body[offset] = (byte)(tmpTaste.price & 0x000000FF);
					body[offset + 1] = (byte)((tmpTaste.price & 0x0000FF00) >> 8);
					body[offset + 2] = (byte)((tmpTaste.price & 0x00FF0000) >> 16);
					body[offset + 3] = (byte)((tmpTaste.price & 0xFF000000) >> 24);
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
