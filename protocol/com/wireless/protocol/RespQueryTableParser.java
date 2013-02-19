package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

import com.wireless.pack.ProtocolPackage;
import com.wireless.util.PinyinUtil;

public class RespQueryTableParser {

	/**
	 * Parse the response associated with the query table. 
	 * @param response The response containing the table info.
	 * @return Return the array of table info.
	 */
	public static Table[] parse(ProtocolPackage response){
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

		//get the amount to tables
		int nTable = (response.body[0] & 0x000000FF) |
					 ((response.body[1] & 0x000000FF) << 8);
		
		Table[] tables = new Table[nTable];
		
		int offset = 2;
		for(int i = 0; i < tables.length; i++){
			
			tables[i] = new Table();
			
			//get the length to this table name
			int nameLen = response.body[offset];
			offset++;
			
			//get the value to this table name
			if(nameLen > 0){
				try{
					tables[i].mName = new String(response.body, offset, nameLen, "UTF-16BE");
					tables[i].mPinyin = PinyinUtil.cn2Spell(tables[i].mName);
					tables[i].mPinyinShortcut = PinyinUtil.cn2FirstSpell(tables[i].mName);
				}catch(UnsupportedEncodingException e){
					
				}
				offset += nameLen;
			}
			
			//get the table alias
			tables[i].mAliasId = ((int)(response.body[offset] & 0x000000FF)) | 
								((int)(response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			
			//get the region alias
			tables[i].regionID = response.body[offset];
			offset++;
			
			//get the service rate
			tables[i].mServiceRate = (response.body[offset] & 0x000000FF) |
								    ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			
			//get the minimum cost
			tables[i].mMinimumCost = (response.body[offset] & 0x000000FF) | 
									((response.body[offset + 1] & 0x000000FF) << 8) |
									((response.body[offset + 2] & 0x000000FF) << 16) |
									((response.body[offset + 3] & 0x000000FF) << 24);
			offset += 4;
			
			//get the status
			tables[i].mStatus = response.body[offset];
			offset++;
			
			//get the category
			tables[i].mCategory = response.body[offset];
			offset++;
			
			//get the custom number;
			tables[i].mCustomNum = response.body[offset];
			offset++;
		}
		
		return tables;
	}

}
