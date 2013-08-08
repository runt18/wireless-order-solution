package com.wireless.common;

public final class Params {
	//The name to share preferences
	public final static String PREFS_NAME = "TermPref";
	//The name to Pin
	public final static String PIN = "Pin";
	//The name to "后厨打印"
	public final static String PRINT_SETTING = "PrintSetting";
	//The name to "连接超时"
	public final static String CONN_TIME_OUT = "ConnTimeout";
	//The name to IP address
	public final static String IP_ADDR = "IPAddr";
	//The name to IP port
	public final static String IP_PORT = "IPPort";
	//The id to staff has login before
	public final static String STAFF_LOGIN_ID = "StaffPin";
	//The last pick food category
	public final static String LAST_PICK_CATE = "LastPickCate";
	
	/* The setting value to print setting */
	public final static int PRINT_SYNC = 0;			//同步
	public final static int PRINT_ASYNC = 1;		//异步
	
	/* The time out value */
	public final static int TIME_OUT_10s = 0;		//10s 
	public final static int TIME_OUT_15s = 1;		//15s
	public final static int TIME_OUT_20s = 2;		//20s
	
	/* The last pick food category */
	public final static int PICK_BY_NUMBER = 0;		//编号点菜
	public final static int PICK_BY_KITCHEN = 1;	//分厨点菜
	public final static int PICK_BY_PINYIN = 2;		//拼音点菜
	
	/* The default IP address */
	public final static String DEF_IP_ADDR = "e-tones.net";
	
	/* The default IP port */
	public final static int DEF_IP_PORT = 55555;
	
	/* The default staff pin */
	public final static long DEF_STAFF_LOGIN_ID = -1;
	
}
