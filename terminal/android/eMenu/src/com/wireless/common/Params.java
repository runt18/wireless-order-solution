package com.wireless.common;

public final class Params {
	//The name to share preferences
	public final static String PREFS_NAME = "TermPadPref";
	//The name to Pin
	public final static String PIN = "Pin";
	//The name to IP address
	public final static String IP_ADDR = "IPAddr";
	//The name to IP port
	public final static String IP_PORT = "IPPort";
	//The pin to login staff
	public final static String STAFF_ID = "StaffPin";
	public final static String STAFF_FIXED = "isFixStaff";
	//The last pick food category
	public final static String LAST_PICK_CATE = "LastPickCate";
	// the table id
	public final static String TABLE_FIXED = "isTableFixed";
	public final static String TABLE_ID = "TableId";
	
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
	
	/** The default IP address */
	public final static String DEF_IP_ADDR = "e-tones.net";
	
	/* The default IP port */
	public final static int DEF_IP_PORT = 55555;
	
	/* The default staff pin */
	public final static long DEF_STAFF_ID = -1;

	
	/* The folder path to image */
	public final static String IMG_STORE_PATH = "/digi-e/images/";
	
	/* logo path */
	public final static String LOGO_PATH = "/digi-e/logo.png";
	
	public final static String FOOD_IMG_PROJECT_TBL = "FoodImgProj";
}
