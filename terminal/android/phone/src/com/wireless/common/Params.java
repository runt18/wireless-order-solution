package com.wireless.common;

public final class Params {
	//the name to share preferences
	public final static String PREFS_NAME = "TermPref";
	//the name to IP address
	public final static String IP_ADDR = "IPAddr";
	//the name to IP port
	public final static String IP_PORT = "IPPort";
	//the backup server
	public final static String BACKUP_CONNECTOR = "BackupConnector";
	//the id to staff has login before
	public final static String STAFF_LOGIN_ID = "StaffPin";
	//the last pick food category
	public final static String LAST_PICK_CATE = "LastPickCate";
	
	/* the time out value */
	public final static int TIME_OUT_10s = 0;		//10s 
	public final static int TIME_OUT_15s = 1;		//15s
	public final static int TIME_OUT_20s = 2;		//20s
	
	/* the last pick food category */
	public final static int PICK_BY_NUMBER = 0;		//±àºÅµã²Ë
	public final static int PICK_BY_KITCHEN = 1;	//·Ö³øµã²Ë
	public final static int PICK_BY_PINYIN = 2;		//Æ´Òôµã²Ë
	
	/* the default IP address */
	public final static String DEF_IP_ADDR = "e-tones.net";

	public final static String DEF_BACKUP_CONNECTOR = "bk.e-tones.net:55555";
	
	/* the default IP port */
	public final static int DEF_IP_PORT = 55555;
	
	/* the default staff pin */
	public final static long DEF_STAFF_LOGIN_ID = -1;
	
}
