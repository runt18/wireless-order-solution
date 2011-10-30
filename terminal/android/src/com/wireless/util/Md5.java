package com.wireless.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

	/**
	 * Convert the md5 byte to hex string.
	 * @param md5Msg the md5 byte value
	 * @return the hex string to this md5 byte value
	 */
	public static String toHexString(byte[] md5Msg){
		StringBuffer hexString = new StringBuffer();
		for (int i=0; i < md5Msg.length; i++) {
			if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
				hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
			}else{
				hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
			}
		}
		return hexString.toString();
	}
	
	/*
	 * ¼ÓÃÜ·½·¨
	 * 
	 * */
	public static byte[]  md5(String pwr){
		MessageDigest digester;
		try {
			digester = MessageDigest.getInstance("MD5");
		    digester.update(pwr.getBytes(), 0, pwr.getBytes().length); 
		    byte[]  digest = digester.digest(); 
		    return digest;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	
	}
}
