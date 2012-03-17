package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public final class RespQueryRegion extends RespPackage {

	/******************************************************
	 * In the case query region successfully, 
	 * design the query region response looks like below
	 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved : 0x00
	 * pin[6] : same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * nRegion : <region_1> : ... : <region_n>
	 * nRegion - the amount of regions
	 * <region_n>
	 * len_1 : region_name : region_alias[2] 
	 * len_1 - the length to the region name
	 * region_name - the value to region name
	 * region_alias[2] - the alias id to this region
	 *******************************************************/
	public RespQueryRegion(ProtocolHeader reqHeader, Region[] regions) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		/**
		 * calculate the body length
		 */		
		int bodyLen = 1;	/* the amount of tables takes up 1-byte */
		
		for(int i = 0; i < regions.length; i++){
			byte[] name = regions[i].name.getBytes("UTF-16BE");
			bodyLen += 1 + 				/* the length of region name takes up 1-byte */
					   name.length +	/* the value of region name */
					   2;				/* the regions alias takes up 2-byte */ 
		}
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		
		//assign the amount of table
		body[0] = (byte)(regions.length & 0x000000FF);
		
		int offset = 1;
		for(int i = 0; i < regions.length; i++){
			byte[] name = regions[i].name.getBytes("UTF-16BE");
			//assign the length of region name to body
			body[offset] = (byte)name.length;
			offset++;
			
			//assign the region name to body
			System.arraycopy(name, 0, body, offset, name.length);
			offset += name.length;
			
			//assign the region alias 
			body[offset] = (byte)(regions[i].regionID & 0x000000FF);
			body[offset + 1] = (byte)((regions[i].regionID & 0x0000FF00) >> 8);
			offset += 2;
		}
	}
}
