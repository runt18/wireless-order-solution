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
	 * nStaff : <table_1> : ... : <table_2>
	 * nStaff - the amount of tables
	 * <table_n>
	 * len_1 : table_name : table_alias[2] : region : status : category : custom_num
	 * len_1 - the length to the table name
	 * table_name - the value to table name
	 * table_alias[2] - the alias id to this table
	 * region - the region alias id to this table
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
		int bodyLen = 1;	/* the amount of staff takes up 1-byte */
		
		for(int i = 0; i < tables.length; i++){
			byte[] name = tables[i].name.getBytes("UTF-16BE");
			bodyLen += 1 + 					/* the length takes up 1-byte */
					   name.length + 		/* the name to this table */
					   2 + 					/* the table alias takes up 2-byte */
					   1 + 					/* the region alias takes up 1-byte */
					   1 +					/* the status takes up 1-byte */
					   1 +					/* the category takes up 1-byte */
					   1 ;					/* the custom number takes up 1-byte */					   
		}
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		
		//assign the amount of table
		body[0] = (byte)(tables.length & 0x000000FF);
		
		int offset = 1;
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
