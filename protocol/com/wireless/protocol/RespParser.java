package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class RespParser {	
	
	/**
	 * Parse the response associated with the query restaurant. 
	 * @param response The response containing the restaurant info.
	 * @return The restaurant info.
	 */
	public static Restaurant parseQueryRestaurant(ProtocolPackage response){
		Restaurant restaurant = new Restaurant();
		/******************************************************
		 * In the case query restaurant successfully, 
		 * design the query restaurant response looks like below
		 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
		 * <Header>
		 * mode - ORDER_BUSSINESS
		 * type - ACK
		 * seq - same as request
		 * reserved : 0x00
		 * pin[6] : same as request
		 * len[2] -  length of the <Body>
		 * <Body>
		 * len_1 : restaurant_name : len_2 : restaurant_info : len_3 : owner : len_4 : pwd : len_5 : pwd2 : len_6 : pwd3
		 * len_1 - 1-byte indicates the length of the restaurant name
		 * restaurant_name - restaurant name whose length equals "len_1"
		 * len_2 - 1-byte indicates the length of the restaurant info
		 * restaurant_info - restaurant info whose length equals "len_2"
		 * len_3 - 1-byte indicates the length of the terminal's owner name
		 * owner - the owner name of terminal
		 * len_4 : 1-byte indicates the length of the password
		 * pwd : the 1st password to this restaurant
		 * len_5 : 1-byte indicates the length of the password2
		 * pwd2 : the 2nd password to this restaurant
		 * len_6 : 1-byte indicates the length of the password3
		 * pwd3 : the 3rd password to this restaurant
		 *******************************************************/

		int offset = 0;
		try{
			//get the length of the restaurant name
			int length = response.body[0];
			offset++;
			//get the restaurant name
			if(length != 0){
				restaurant.name = new String(response.body, offset, length, "UTF-16BE");
			}
			
			//calculate the position of the length to restaurant info
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the restaurant info
			if(length != 0){
				restaurant.info = new String(response.body, offset, length, "UTF-16BE");
			}
			
			//calculate the position of the length to owner
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the owner name of the terminal 
			if(length != 0){
				restaurant.owner = new String(response.body, offset, length, "UTF-16BE");
			}
			
			//calculate the position of the length to 1st password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 1st password
			if(length != 0){
				restaurant.pwd = new String(response.body, offset, length);
			}
			
			//calculate the position of the length to 2nd password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 2nd password
			if(length != 0){
				restaurant.pwd3 = new String(response.body, offset, length);
			}
			
			//calculate the position of the length to 3rd password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 3rd password
			if(length != 0){
				restaurant.pwd5 = new String(response.body, offset, length);
			}
			
		}catch(UnsupportedEncodingException e){

		}
		return restaurant;
	}
	
	/**
	 * Parse the response associated with the query staff. 
	 * @param response The response containing the staff info.
	 * @return Return the array of staff info, null if no staff info.
	 */
	public static StaffTerminal[] parseQueryStaff(ProtocolPackage response){
		/******************************************************
		 * In the case query staff successfully, 
		 * design the query staff response looks like below
		 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
		 * <Header>
		 * mode - ORDER_BUSSINESS
		 * type - ACK
		 * seq - same as request
		 * reserved : 0x00
		 * pin[6] : same as request
		 * len[2] -  length of the <Body>
		 * <Body>
		 * nStaff[2] : <staff_1> : ... : <staff_n>
		 * nStaff[2] - the amount of staff
		 * <staff_n>
		 * len_1 : name : len_2 : pwd : pin[4]
		 * len_1 - the length to the staff name
		 * name - the value to staff name
		 * len_2 - the length to the staff password
		 * pwd - the value to the staff password
		 * pin[4] - 4-byte indicating the pin
		 *******************************************************/	
		StaffTerminal[] staffs = null;
		try{
			//get the amount to staff
			int nStaff = (response.body[0] & 0x000000FF) |
						 ((response.body[1] & 0x000000FF) << 8);
			
			if(nStaff > 0){
				staffs = new StaffTerminal[nStaff];
				
				int offset = 2;
				for(int i = 0; i < nStaff; i++){
					
					staffs[i] = new StaffTerminal();
					
					//get the length to this staff name
					int nameLen = response.body[offset];
					offset++;
					
					//get the value to this staff name
					if(nameLen > 0){
						staffs[i].name = new String(response.body, offset, nameLen, "UTF-16BE");
						offset += nameLen;
					}
					
					//get the length to staff password
					int pwdLen = response.body[offset];
					offset++;
					
					//get the value to this staff name
					if(pwdLen > 0){
						staffs[i].pwd = new String(response.body, offset, pwdLen);
						offset += pwdLen;
					}
					
					//get the pin to this staff
					staffs[i].pin = ((long)response.body[offset] & 0x00000000000000FF) |
						   	   		(((long)response.body[offset + 1] & 0x00000000000000FF) << 8) |
						   	   		(((long)response.body[offset + 2] & 0x00000000000000FF) << 16) |
						   	   		(((long)response.body[offset + 3] & 0x00000000000000FF) << 24);
					offset += 4;
				}		

			}			
			
		}catch(UnsupportedEncodingException e){
			
		}
		
		return staffs;
	}
	

	

}
