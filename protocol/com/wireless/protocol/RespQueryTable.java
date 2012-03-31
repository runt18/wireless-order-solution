package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public final class RespQueryTable extends RespPackage {
	/******************************************************
	 * In the case query table successfully, 
	 * design the query table response looks like below
	 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved : 0x00
	 * pin[6] : same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * nTable[2] : <table_1> : ... : <table_2>
	 * nTable[2] - the amount of tables
	 * <table_n>
	 * len_1 : table_name : table_alias[2] : region : service_rate[2] : minimum_cost[4] : status : category : custom_num
	 * len_1 - the length to the table name
	 * table_name - the value to table name
	 * table_alias[2] - the alias id to this table
	 * region - the region alias id to this table
	 * service_rate[2] - the service rate to this table
	 * minimum_cost[4] - the minimum cost to this table
	 * status - the status to this table
	 * category - the category to this table
	 * custom_num - the custom number to this table
	 *******************************************************/
	public RespQueryTable(ProtocolHeader reqHeader, Table[] tables) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		/**
		 * calculate the body length
		 */		
		int bodyLen = 1;	/* the amount of tables takes up 1-byte */
		
		for(int i = 0; i < tables.length; i++){
			byte[] name = tables[i].name.getBytes("UTF-16BE");
			bodyLen += 2 + 					/* the length takes up 2-byte */
					   name.length + 		/* the name to this table */
					   2 + 					/* the table alias takes up 2-byte */
					   1 + 					/* the region alias takes up 1-byte */
					   2 +					/* the service rate takes up 2-byte */
					   4 +					/* the minimum cost takes up 4-byte */
					   1 +					/* the status takes up 1-byte */
					   1 +					/* the category takes up 1-byte */
					   1 ;					/* the custom number takes up 1-byte */					   
		}
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		
		//assign the amount of table
		body[0] = (byte)(tables.length & 0x000000FF);
		body[1] = (byte)((tables.length & 0x0000FF00) >> 8);
		
		int offset = 2;
		for(int i = 0; i < tables.length; i++){
			byte[] name = tables[i].name.getBytes("UTF-16BE");
			//assign the length of table name to body
			body[offset] = (byte)name.length;
			offset++;
			
			//assign the table name to body
			System.arraycopy(name, 0, body, offset, name.length);
			offset += name.length;
			
			//assign the table alias 
			body[offset] = (byte)(tables[i].aliasID & 0x000000FF);
			body[offset + 1] = (byte)((tables[i].aliasID & 0x0000FF00) >> 8);
			offset += 2;
			
			//assign the region alias
			body[offset] = (byte)(tables[i].regionID);
			offset++;
			
			//assign the service rate
			body[offset] = (byte)(tables[i].serviceRate & 0x000000FF);
			body[offset + 1] = (byte)((tables[i].serviceRate & 0x0000FF00) >> 8);
			offset += 2;
			
			//assign the minimum cost
			body[offset] = (byte)(tables[i].minimumCost & 0x000000FF);
			body[offset + 1] = (byte)((tables[i].minimumCost & 0x0000FF00) >> 8);
			body[offset + 2] = (byte)((tables[i].minimumCost & 0x00FF0000) >> 16);
			body[offset + 3] = (byte)((tables[i].minimumCost & 0xFF000000) >> 24);
			offset += 4;
			
			//assign the status
			body[offset] = (byte)(tables[i].status);
			offset++;
			
			//assign the category
			body[offset] = (byte)(tables[i].category);
			offset++;
			
			//assign the custom number
			body[offset] = (byte)(tables[i].custom_num);
			offset++;
		}
	}
}
