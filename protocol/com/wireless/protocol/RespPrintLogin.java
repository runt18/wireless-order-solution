package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

/******************************************************
 * In the case printer login successfully, 
 * design the response looks like below
 * mode : type : seq : reserved : pin[6] : len[2] : nKitchen : <kitchen_1> : ... : <kitchen_n> : 
 * restaurant_len : restaurant_name
 * <Header>
 * mode - PRINT
 * type - ACK
 * seq - same as request
 * reserved - 0x00
 * pin[6] - same as request
 * len[2] -  length of the <Body>
 * <Body>
 * nKitchen : <kitchen_1> : ... : <kitchen_n> : nRegion : <region_1> : ... : <region_n> : restaurant_len : restaurant_name
 * nKitchen - the number of kitchens
 * <kitchen_n>
 * alias_id : len_name : name
 * alias_id - the alias id to this kitchen  
 * len_name - the length of the kitchen name
 * name - the name to kitchen
 * nRegion - the number of regions
 * <region_n>
 * alias_id : len_name : name
 * alias_id - the alias id to this region
 * len_name - the length of the region name
 * name - the name to region
 * restaurant_len - the length of the user name
 * restaurant_name - the name to user
 *******************************************************/
public class RespPrintLogin extends RespPackage{
	public RespPrintLogin(ProtocolHeader reqHeader, Kitchen[] kitchens, Region[] regions, String restaurant){
		super(reqHeader);
		header.mode = Mode.PRINT;
		header.type = Type.ACK;

		//calculate the length of body
		int len = 0;
		//the number of kitchens takes up 1-byte
		len += 1;
		
		//the byte array to hold the name of kitchens
		byte[][] kitchenBytes = new byte[kitchens.length][];
		
		for(int i = 0; i < kitchenBytes.length; i++){
			//the alias id takes up 1-byte
			len += 1;
			//the length of the name takes up 1-byte
			len += 1;
			try{
				kitchenBytes[i] = kitchens[i].name.getBytes("GBK");
			}catch(UnsupportedEncodingException e){
				
			}
			//the name of kitchen takes up the byte arrays' length
			len += kitchenBytes[i].length;
		}
		
		//the number of regions takes up 1-byte
		len += 1;
		//the byte array to hold the name of regions
		byte[][] regionBytes = new byte[regions.length][];
		for(int i = 0; i < regionBytes.length; i++){
			//the alias_id takes up 1-byte
			len += 1;
			//the length of name takes up 1-byte
			len += 1;
			try{
				regionBytes[i] = regions[i].name.getBytes("GBK");
			}catch(UnsupportedEncodingException e){
				
			}
			//the name of region takes up the byte arrays' length
			len += regionBytes[i].length;
		}
		
		byte[] restaurantBytes = new byte[0];
		//the length of restaurant name
		len += 1;
		try{
			restaurantBytes = restaurant.getBytes("GBK");
			len += restaurantBytes.length;
		}catch(UnsupportedEncodingException e){
			
		}
		
		//allocate the memory for the body
		body = new byte[len];
		
		int offset = 0;
		//assign the number of kitchens
		body[offset] = (byte)kitchens.length;
		offset++;
		
		for(int i = 0; i < kitchenBytes.length; i++){
			//assign the alias id
			body[offset] = (byte)kitchens[i].kitchenID;
			//assign the length to kitchen name
			body[offset + 1] = (byte)kitchenBytes[i].length;
			//assign the name of kitchen
			System.arraycopy(kitchenBytes[i], 0, body, offset + 2, kitchenBytes[i].length);
			offset += 1 + /* the alias id takes up 1-byte */ 
					  1 + /* the length of name takes up 1-byte */
					  kitchenBytes[i].length; /* the name of kitchen takes up the byte arrays' length */
		}
		
		//assign the number of regions
		body[offset] = (byte)regions.length;
		offset++;
		for(int i = 0; i < regionBytes.length; i++){
			//assign the alias id to region
			body[offset] = (byte)regions[i].regionID;
			//assign the length of region name
			body[offset + 1] = (byte)regionBytes[i].length;
			//assign the value of region name
			System.arraycopy(regionBytes[i], 0, body, offset + 2, regionBytes[i].length);
			offset += 1 +	/* the alias id takes up 1-byte */
					  1 +	/* the length of name takes up 1-byte */
					  regionBytes[i].length;	/* the name of region takes up the byte arrays' length */
		}
		
		//assign the length of restaurant
		body[offset] = (byte)restaurantBytes.length;
		//assign the restaurant value
		System.arraycopy(restaurantBytes, 0, body, offset + 1, restaurantBytes.length);
		
		header.length[0] = (byte)(body.length & 0x000000FF);
		header.length[1] = (byte)((body.length & 0x0000FF00) >> 8);
	}
}
