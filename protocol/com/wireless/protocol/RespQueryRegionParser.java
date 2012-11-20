package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class RespQueryRegionParser {
	/**
	 * Parse the response associated with the query region. 
	 * @param response The response containing the region info.
	 * @return Return the array of region info.
	 */
	public static Region[] parse(ProtocolPackage response){
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
		//get the amount to region
		int nRegion = response.body[0];
		
		Region[] regions = new Region[nRegion];
			
		int offset = 1;
		for(int i = 0; i < regions.length; i++){
			
			regions[i] = new Region();
			
			//get the length to this region name
			int nameLen = response.body[offset];
			offset++;
			
			//get the value to this region name
			if(nameLen > 0){
				try{
					regions[i].name = new String(response.body, offset, nameLen, "UTF-16BE");
				}catch(UnsupportedEncodingException e){
					
				}
				offset += nameLen;
			}
			
			//get the region alias
			regions[i].regionID = (short)(((int)(response.body[offset] & 0x0000000FF)) | 
								((int)(response.body[offset + 1] & 0x0000000FF) << 8));
			offset += 2;			
		}
		
		return regions;

	}
}
