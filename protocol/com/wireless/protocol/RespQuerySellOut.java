package com.wireless.protocol;


public class RespQuerySellOut extends RespPackage{
	/******************************************************
	 * The response to query sell out foods is as below.
	 * mode : type : seq : reserved : pin[6] : len[2] : sell_out_amount[2] : sell_out_1[1] : sell_out_2[2] ... : sell_out_n[2]
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * sell_out_amount[2] : sell_out_1[2] : sell_out_2[2] ... : sell_out_n[2]
	 * sell_out_amount - 2-byte indicates the amount of 
	 * sell_out_1 - 2-byte indicates the alias id to sell out food
	 *******************************************************/		
	public RespQuerySellOut(ProtocolHeader reqHeader, Food[] sellOutFoods){
		super(reqHeader);
		header.type = Type.ACK;
		
		//Calculate the body length
		int bodyLen = 2 + 						/* the amount to sell out foods takes up 2-byte */ 
					  2 * sellOutFoods.length; 	/* each alias to sell out foods takes up 2-byte */
		
		//Allocate the memory to body
		body = new byte[bodyLen];
		
		int offset = 0;
		
		//Assign the amount of sell out foods to body
		body[offset] = (byte)(sellOutFoods.length & 0x000000FF);
		body[offset + 1] = (byte)((sellOutFoods.length & 0x0000FF00) >> 8);
		offset += 2;
		
		//Assign each alias id of sell out foods to body
		for(int i = 0; i < sellOutFoods.length; i++){
			body[offset] = (byte)(sellOutFoods[i].aliasID & 0x000000FF);
			body[offset + 1] = (byte)((sellOutFoods[i].aliasID & 0x0000FF00) >> 8);
			offset += 2;
		}
		
		//Assign the body length to the corresponding header's field.
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
	}
}
