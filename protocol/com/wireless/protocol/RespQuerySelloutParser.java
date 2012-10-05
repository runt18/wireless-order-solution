package com.wireless.protocol;

import java.util.Arrays;
import java.util.Comparator;

public class RespQuerySelloutParser {

	/**
	 * Parse the response to query sell out foods.
	 * In the mean time update the sell out status from source foods.
	 * @param response
	 * 			The response to be parsed.
	 * @param srcFoods
	 * 			The source foods which the sell out food would be picked from.
	 * 			Since using the binary search to select the food, the source food should be sorted 
	 * 			by alias id before passing to this method.
	 * @return The sell out foods.
	 */
	public static Food[] parse(ProtocolPackage response, Food[] srcFoods){
		
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
		 * sell_out_n - 2-byte indicates the alias id to sell out food
		 *******************************************************/	
		
		int offset = 0;
		//Get the amount of sell out foods.
		int nSellout = (response.body[offset] & 0x000000FF) |
					   ((response.body[offset + 1] & 0x000000FF ) << 8);
		offset += 2;
		
		//Reset all the foods sell out flag.
		for(int i = 0; i < srcFoods.length; i++){
			srcFoods[i].status &= ~Food.SELL_OUT;
		}
		
		Food tmp = new Food();
		//Get each sell out foods' alias id.
		Food[] sellOutFoods = new Food[nSellout];
		for(int i = 0; i < sellOutFoods.length; i++){
			tmp.aliasID = (response.body[offset] & 0x000000FF) | ((response.body[offset + 1]) << 8);
			int index = Arrays.binarySearch(srcFoods, tmp, new Comparator<Food>(){
				@Override
				public int compare(Food arg0, Food arg1) {
					if(arg0.aliasID > arg1.aliasID){
						return 1;
					}else if(arg0.aliasID < arg1.aliasID){
						return -1;
					}else{
						return 0;
					}
				}			
			});
			if(index != -1){
				srcFoods[index].status |= Food.SELL_OUT;
				sellOutFoods[i] = srcFoods[index];
			}
			offset += 2;
		}		
		
		return sellOutFoods;
	}
}
