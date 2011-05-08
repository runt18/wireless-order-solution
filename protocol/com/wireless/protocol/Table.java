package com.wireless.protocol;

public class Table {
	
	public static final byte TABLE_IDLE = 0;
	public static final byte TABLE_BUSY = 1;
	
	//the alias id to this table
	public short alias_id = 0;
	//the number of the custom to this table
	public short custom_num = 0;
	//the status to this table
	public short status = TABLE_IDLE;
}
