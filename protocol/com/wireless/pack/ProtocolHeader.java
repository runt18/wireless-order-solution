package com.wireless.pack;

import java.util.Arrays;


/******************************************************
* Design the header of protocol looks like below
* mode : type : seq : reserved : pin[6] : len[2]
* mode : 1-byte indicating the mode
* type : 1-byte indicating the type
* seq : 1-byte indicating the sequence number
* reserved : 1-byte reserved 
* pin[6] : 6-bytes indicating the phone's id 
* len[2] : 2-bytes indicating the length of the body
*******************************************************/
public class ProtocolHeader {
	public final static int SIZE = 12;
	public byte mode = 0;
	public byte type = 0;
	public byte seq = 0;
	public byte reserved = 0;
	public byte[] pin = new byte[6];
	public byte[] length = new byte[2];
	
	public ProtocolHeader(){
		Arrays.fill(pin, (byte)0);
		Arrays.fill(length, (byte)0);
	}
} 

