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
 * print_type[2] : src_tbl[2] : dest_tbl[2] : order_date[8] : category : custom_num : 
 * food_amount : <Food1> : <Food2>... : <TmpFood1> : <TmpFood2>... : 
 * 
 * print_type[2] - 2-byte indicates the print type
 * src_tbl[2] - 2-byte indicates the alias to source table
 * dest_tbl[2] - 2-byte indicates the alias to destination table
 * order_date[8] - 8-byte indicates the last modified order date
 * category - 1-byte indicates the category to this order  
 * custom_num - 1-byte indicating the custom number for this table
 * food_amount - 1-byte indicating the amount of foods
 * 
 * <Food>
 * is_temp(0) : food_alias[2] : order_amount[2] :
 * normal_taste_amount : <NormalTaste1> : <NormalTaste2>... : 
 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
 * kitchen_alias : hang_status : is_hurried : cancel_reason_id[4]
 * is_temp(0) - "0" means this food is NOT temporary
 * food_alias[2] - 2-byte indicating the alias id to food
 * order_amount[2] - 2-byte indicating how many this foods are ordered
 * normal_taste_amount - 1-byte indicates the amount to normal taste
 * <NormalTaste>
 * normal_taste_alias[2] : normal_taste_category
 * normal_taste_alias[2] - 2-byte indicates the taste alias id
 * normal_taste_category - 1-byte indicates the category to this normal taste
 * len_tmp_taste - 1-byte indicates the length of temporary taste
 * tmp_taste[n] - the temporary taste value
 * tmp_taste_alias[2] - 2-byte indicates the alias id to this temporary taste
 * tmp_taste_price[4] - 4-byte indicates the price to this temporary taste
 * kitchen_alias - the kitchen alias this food belongs to
 * hang_status - the hang status to the food
 * is_hurried - indicates whether the food is hurried
 * cancel_reason_id[4] - 4-byte indicates the id to cancel reason
 *
 * <TmpFood>
 * is_temp(1) : tmp_food_alias[2] : kitchen_alias[2] : order_amount[2] : unit_price[3] : 
 * hang_status : is_hurried : cancel_reason_id[4] : len : food_name[len] 
 * is_temp(1) - "1" means this food is temporary
 * tmp_food_alias[2] - 2-byte indicating the alias to temporary food
 * kitchen_alias[2] - 2-byte indicating the alias id to kitchen this temporary food belongs to
 * order_amount[2] - 2-byte indicating how many this foods are ordered
 * unit_price[3] - 3-byte indicating the unit price to this food
 * hang_status - the hang status to the food
 * is_hurried - indicates whether the food is hurried
 * cancel_reason_id[4] - 4-byte indicates the id to cancel reason
 * len - the length of food's name
 * food_name[len] - the value of the food name 
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
		if(type != Type.INSERT_ORDER && type != Type.UPDATE_ORDER){
			throw new IllegalArgumentException();
		}
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = type;

		//calculate the amount of bytes that the food list needs
		int foodLen = 0;
		for(int i = 0; i < reqOrder.foods.length; i++){
			if(reqOrder.foods[i].isTemporary){
				
				foodLen += 1 + 	/* is_temp(1) */
						   2 + 	/* tmp_food_alias[2] */
						   2 + 	/* kitchen_alias[2] */
						   2 +	/* order_amount[2] */
						   3 + 	/* unit_price[3] */
						   1 + 	/* hang_status */
						   1 + 	/* is_hurried */
						   4 +  /* cancel reason id */
						   1 + 	/* length to temporary food name */
						   reqOrder.foods[i].mName.getBytes("UTF-8").length; /* the name to temporary food */
			}else{

				foodLen += 1 + /* is_temp(0) */
						   2 + /* food_id[2] */ 
						   2 + /* order_amount[2] */
						   1 + /* normal_taste_amount */
						   (reqOrder.foods[i].hasNormalTaste() ? reqOrder.foods[i].mTasteGroup.getNormalTastes().length * 3 : 0) + /* each normal taste alias and category takes up 3-byte */
						   1 + /* len_tmp_taste */
						   (reqOrder.foods[i].hasTmpTaste() ? reqOrder.foods[i].mTasteGroup.mTmpTaste.preference.getBytes("UTF-8").length : 0) + /* the name to tmp_taste */
						   2 + /* tmp_taste_alias[2] */
						   4 + /* tmp_taste_price[4] */
						   1 + /* kitchen */
						   1 + /* hang_status */
						   1 + /* is_hurried */
						   4;  /* cancel reason id */ 
				
			}
		}
		
		//calculate the body's length
		int bodyLen = 2 + 		/* print type takes up 2 bytes */
					  2 +		/* alias to source table takes up 2 bytes */
					  2 + 		/* alias to destination takes up 2 bytes */
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
		body[offset] = (byte)(reqOrder.mCategory & 0x00FF);
		offset += 1;
		
		//assign the custom number
		body[offset] = (byte)(reqOrder.mCustomNum & 0x000000FF);
		offset += 1;
		
		//assign the number of foods
		body[offset] = (byte)(reqOrder.foods.length & 0x000000FF);
		offset += 1;
		
		//assign each order food's id and count
		for(int i = 0; i < reqOrder.foods.length; i++){
			
			int reasonId;
			if(reqOrder.foods[i].hasCancelReason()){
				reasonId = reqOrder.foods[i].getCancelReason().getId();
			}else{
				reasonId = CancelReason.NO_REASON;
			}
			
			if(reqOrder.foods[i].isTemporary){
				byte[] bytesToTmpFood = reqOrder.foods[i].mName.getBytes("UTF-8");

				//assign the temporary flag
				body[offset] = 1;
				offset += 1;
				//assign the alias id to this temporary food
				body[offset] = (byte)(reqOrder.foods[i].mAliasId & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].mAliasId & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the kitchen alias this temporary belongs to
				body[offset] = (byte)(reqOrder.foods[i].mKitchen.mAliasId & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].mKitchen.mAliasId & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the order amount
				int mCnt = reqOrder.foods[i].getCountInternal();
				body[offset] = (byte)(mCnt & 0x000000FF);
				body[offset + 1] = (byte)((mCnt & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the unit price
				body[offset] = (byte)(reqOrder.foods[i].mUnitPrice & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].mUnitPrice & 0x0000FF00) >> 8);
				body[offset + 2] = (byte)((reqOrder.foods[i].mUnitPrice & 0x00FF0000) >> 16);
				offset += 3;
				
				//assign the hang status
				body[offset] = (byte)reqOrder.foods[i].hangStatus;
				offset += 1;
				
				//assign the hurried flag
				body[offset] = (byte)(reqOrder.foods[i].isHurried ? 1 : 0);
				offset += 1;
				
				//assign the cancel reason id
				body[offset] = (byte)(reasonId & 0x000000FF);
				body[offset + 1] = (byte)(reasonId >> 8);
				body[offset + 2] = (byte)(reasonId >> 16);
				body[offset + 3] = (byte)(reasonId >> 24);
				offset += 4;
				
				//assign the amount of food name's byte
				body[offset] = (byte)(bytesToTmpFood.length);
				offset += 1;
				
				//assign the value of food name
				for(int j = 0; j < bytesToTmpFood.length; j++){
					body[offset + j] = bytesToTmpFood[j];
				}
				offset += bytesToTmpFood.length;				
				
			}else{

				//assign the temporary flag
				body[offset] = 0;
				offset += 1;
				
				//assign the food id
				body[offset] = (byte)(reqOrder.foods[i].mAliasId & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].mAliasId & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the order amount
				body[offset] = (byte)(reqOrder.foods[i].getCountInternal() & 0x000000FF);
				body[offset + 1] = (byte)((reqOrder.foods[i].getCountInternal() & 0x0000FF00) >> 8);
				offset += 2;
				
				
				//assign each alias id to normal tastes
				if(reqOrder.foods[i].hasNormalTaste()){
					
					Taste[] normalTastes = reqOrder.foods[i].mTasteGroup.getNormalTastes();
					
					//assign the amount to normal tastes
					body[offset] = (byte)normalTastes.length;
					offset += 1;
					
					for(int j = 0; j < normalTastes.length; j++){
						//assign alias id to each normal taste
						body[offset] = (byte)(normalTastes[j].aliasId & 0x00FF);
						body[offset + 1] = (byte)((normalTastes[j].aliasId & 0xFF00) >> 8);
						offset += 2;						
						//assign category to each normal taste
						body[offset] = (byte)normalTastes[j].category;
						offset += 1;
					}
				}else{
					body[offset] = 0;
					offset += 1;
				}		

				if(reqOrder.foods[i].hasTmpTaste()){
					
					Taste tmpTaste = reqOrder.foods[i].mTasteGroup.mTmpTaste;
					
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
					body[offset] = (byte)(tmpTaste.aliasId & 0x00FF);
					body[offset + 1] = (byte)((tmpTaste.aliasId & 0xFF00) >> 8);
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
				body[offset] = (byte)(reqOrder.foods[i].mKitchen.mAliasId);
				offset += 1;
				
				//assign the hang status
				body[offset] = (byte)reqOrder.foods[i].hangStatus;
				offset += 1;
				
				//assign the hurried flag
				body[offset] = (byte)(reqOrder.foods[i].isHurried ? 1 : 0);
				offset += 1;
				
				//assign the cancel reason id
				body[offset] = (byte)(reasonId & 0x000000FF);
				body[offset + 1] = (byte)(reasonId >> 8);
				body[offset + 2] = (byte)(reasonId >> 16);
				body[offset + 3] = (byte)(reasonId >> 24);
				offset += 4;
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
