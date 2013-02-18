package com.wireless.protocol;

import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.Type;
import com.wireless.pack.resp.RespPackage;

public class RespQueryFoodGroup extends RespPackage{
	
	/******************************************************
	 * In the case query food group successfully, 
	 * design the query order response looks like below
	 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved : 0x00
	 * pin[6] : same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * amount_to_pager[2] : <Pager> ... <Pager>
	 * <Pager>
	 * amount_to_large_food : large_food_alias[2] ... large_food_alias_x[2]
	 * amount_to_medium_food : medium_food_alias[2] ... medium_food_alias[2]
	 * amount_to_small_food : small_food_alias[2] ... small_food_alias[2]
	 * amount_to_text_food : text_food_alias[2] ... text_food_alias[2]
	 * captain_food_alias[2]
	 *******************************************************/
	public RespQueryFoodGroup(ProtocolHeader reqHeader, Pager[] pagers){
		super(reqHeader);
		header.type = Type.ACK;
		
		int bodyLen = 0;
		
		//amount to pagers takes up 1-bye
		bodyLen += 1;
		
		for(Pager eachPage : pagers){
			bodyLen += 2 +									/* amount to large foods takes up 2-byte */
					   eachPage.mLargeFoods.length * 2 + 	/* each alias to large foods takes up 2-byte */
					   1 +									/* amount to medium foods */
					   eachPage.mMediumFoods.length * 2 +	/* each alias to medium foods takes up 2-byte */
					   1 +									/* amount to small foods */
					   eachPage.mSmallFoods.length * 2 +	/* each alias to small foods takes up 2-byte */
					   1 +									/* amount to text foods */
					   eachPage.mTextFoods.length * 2 +		/* each alias to text foods takes up 2-byte */
					   2;									/* captain food alias takes up 2-byte */
		}
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		body = new byte[bodyLen];
		
		int offset = 0;
		
		//assign the amount to pagers
		body[offset] = (byte)pagers.length;
		body[offset + 1] = (byte)((pagers.length & 0xFF00) >> 8);
		offset += 2;
		
		for(Pager eachPage : pagers){
			//assign the amount to large foods
			body[offset] = (byte)eachPage.mLargeFoods.length;
			offset += 1;
			
			//assign each large food's alias
			for(Food largeFood : eachPage.mLargeFoods){
				body[offset] = (byte)(largeFood.getAliasId() & 0x00FF);
				body[offset + 1] = (byte)((largeFood.getAliasId() & 0xFF00) >> 8);
				offset += 2;
			}
			
			//assign the amount to medium foods
			body[offset] = (byte)eachPage.mMediumFoods.length;
			offset += 1;
			
			//assign each medium food's alias
			for(Food mediumFood : eachPage.mMediumFoods){
				body[offset] = (byte)(mediumFood.getAliasId() & 0x00FF);
				body[offset + 1] = (byte)((mediumFood.getAliasId() & 0xFF00) >> 8);
				offset += 2;
			}
			
			//assign the amount to small foods
			body[offset] = (byte)eachPage.mSmallFoods.length;
			offset += 1;
			
			//assign each small food's alias
			for(Food smallFood : eachPage.mSmallFoods){
				body[offset] = (byte)(smallFood.getAliasId() & 0x00FF);
				body[offset + 1] = (byte)((smallFood.getAliasId() & 0xFF00) >> 8);
				offset += 2;
			}
			
			//assign the amount to text foods
			body[offset] = (byte)eachPage.mTextFoods.length;
			offset += 1;
			
			//assign each text food's alias
			for(Food textFood : eachPage.mTextFoods){
				body[offset] = (byte)(textFood.getAliasId() & 0x00FF);
				body[offset + 1] = (byte)((textFood.getAliasId() & 0xFF00) >> 8);
				offset += 2;
			}
			
			//assign the captain food's alias
			body[offset] = (byte)(eachPage.mCaptainFood.getAliasId() & 0x00FF);
			body[offset + 1] = (byte)((eachPage.mCaptainFood.getAliasId() & 0xFF00) >> 8);
			offset += 2;
		}
		
	}
	
}
