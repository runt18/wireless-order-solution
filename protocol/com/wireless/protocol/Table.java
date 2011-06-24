package com.wireless.protocol;

public class Table {
	
	public static final byte TABLE_IDLE = 0;
	public static final byte TABLE_BUSY = 1;

	//the restaurant id that this table is attached to
	public int restaurant_id = 0;
	//the alias id to this table
	public int alias_id = 0;
	//the alias name to this table
	public String name;
	//the number of the custom to this table
	public short custom_num = 0;
	//the status to this table
	public short status = TABLE_IDLE;
	//the category to this table
	public short category = Order.CATE_NORMAL;
	/**
	 * The value of minimum cost to this table, ranges from 99999.99 through 0.00
	 * Since the 8100 doesn't support float, we instead to use 0 through 9999999.
	 * So the real price should be divided 100 at last. 
	 */
	public int minimum_cost = 0;
}
