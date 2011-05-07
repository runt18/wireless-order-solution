package com.wireless.order;

public class Table {
	
	public static final byte TABLE_IDLE = 0;
	public static final byte TABLE_BUSY = 1;
	
	public int restaurantID = 0;
	public short alias_id = 0;
	public short custom = 0;
	public short status = TABLE_IDLE;
}
