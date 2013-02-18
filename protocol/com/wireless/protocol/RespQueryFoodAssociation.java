package com.wireless.protocol;

import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.Type;
import com.wireless.pack.resp.RespPackage;


public class RespQueryFoodAssociation extends RespPackage{

	public RespQueryFoodAssociation(ProtocolHeader reqHeader, Food[] associatedFoods){
		super(reqHeader);
		header.type = Type.ACK;
		
		/******************************************************
		 * In the case query associated food successfully, 
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
		 * amount : associated_food_alias[2] ... associated_food_alias_x[2]
		 * amount - 1-byte indicates the amount to associated food
		 * associated_food_alias_x[2] - 2-byte indicates alias id to each associated food
		 *******************************************************/
		
		int bodyLen = 0;
		bodyLen += 1 + 	/* the amount takes up 1 byte */
				   associatedFoods.length * 2;	/* each alias to associated food takes 2-byte */
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		body = new byte[bodyLen];
		
		int offset = 0;
		
		//assign the amount to associated foods
		body[offset] = (byte)associatedFoods.length;
		offset += 1;
		
		//assign the alias to each associated foods
		for(int i = 0; i < associatedFoods.length; i++){
			body[offset] = (byte)(associatedFoods[i].getAliasId() & 0x00FF);
			body[offset + 1] = (byte)((associatedFoods[i].getAliasId() & 0xFF00) >> 8);
			offset += 2;
		}
		
	}
	
}
