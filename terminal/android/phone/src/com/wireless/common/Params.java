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
	//the accent to language
	public final static String ACCENT_LANGUAGE = "AccentLanguage";
	
	/* the last pick food category */
	public final static int PICK_BY_NUMBER = 0;		//编号点菜
	public final static int PICK_BY_KITCHEN = 1;	//分厨点菜
	public final static int PICK_BY_PINYIN = 2;		//拼音点菜
	
	/* the default IP address */
	public final static String DEF_IP_ADDR = "e-tones.net";
	/* the default backup IP address */
	public final static String DEF_BACKUP_CONNECTOR = "wx.e-tones.net:55555";
	
	/* the default IP port */
	public final static int DEF_IP_PORT = 55555;
	
	/* the default staff pin */
	public final static long DEF_STAFF_LOGIN_ID = -1;
	
	public static enum Accent{
		MANDARIN("mandarin", "普通话"),
		CANTONESE("cantonese", "广东话");
		
		public final String val;
		public final String desc;
		
		Accent(String val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Accent valueOf(String val, int i){
			for(Accent accent : values()){
				if(val.equalsIgnoreCase(accent.val)){
					return accent;
				}
			}
			throw new IllegalArgumentException("The val(val = " + val + ") passed is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
}
