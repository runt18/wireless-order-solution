package com.wireless.pack.resp;

import java.io.UnsupportedEncodingException;

import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.Type;

/******************************************************
 * In the case printer login successfully, 
 * design the response looks like below
 * mode : type : seq : reserved : pin[6] : len[2] : 
 * legacy[3] : lenOfRestaurant : restaurantName : lenOfOtaAddr : otaAddr : otaPort[2]
 *******************************************************/
public class RespPrintLogin extends RespPackage{
	public RespPrintLogin(ProtocolHeader reqHeader, String restaurant, String otaAddr, int otaPort){
		super(reqHeader);
		header.mode = Mode.PRINT;
		header.type = Type.ACK;

		byte[] bytesToRestaurant;
		try{
			bytesToRestaurant = restaurant.getBytes("GBK");
		}catch(UnsupportedEncodingException e){
			bytesToRestaurant = new byte[0];
		}
		
		byte[] bytesToOtaAddr;
		try{
			bytesToOtaAddr = otaAddr.getBytes("GBK");
		}catch(UnsupportedEncodingException e){
			bytesToOtaAddr = new byte[0];
		}
		
		int len = 3 +	/* legacy value takes 3-byte */
				  1 +	/* length of restaurant takes 1-byte */
				  bytesToRestaurant.length +	/* value of restaurant name */
				  1 +	/* length of ota address takes 1-byte */
				  bytesToOtaAddr.length +		/* value of ota address */
				  2;	/* ota port takes 1-byte */
		
		//allocate the memory for the body
		body = new byte[len];
		
		int offset = 0;
		
		body[offset] = 0x00;
		body[offset + 1] = 0x00;
		body[offset + 2] = 0x00;
		offset += 3;
		
		//assign the length of restaurant
		body[offset] = (byte)bytesToRestaurant.length;
		offset++;
		//assign the restaurant value
		System.arraycopy(bytesToRestaurant, 0, body, offset, bytesToRestaurant.length);
		offset += bytesToRestaurant.length;
		
		//assign the length of OTA address
		body[offset] = (byte)bytesToOtaAddr.length;
		offset++;
		//assign the OTA address value
		System.arraycopy(bytesToOtaAddr, 0, body, offset, bytesToOtaAddr.length);
		offset += bytesToOtaAddr.length;
		
		body[offset] = (byte)(otaPort & 0x000000FF);
		body[offset + 1] = (byte)((otaPort & 0x0000FF00) >> 8);
		offset += 2;
		
		header.length[0] = (byte)(body.length & 0x000000FF);
		header.length[1] = (byte)((body.length & 0x0000FF00) >> 8);
	}
}
