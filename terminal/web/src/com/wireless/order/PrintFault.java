package com.wireless.order;

public class PrintFault extends Exception {

	private static final long serialVersionUID = 501576021850646485L;
	
	public final static int UNKNOWN = 0;
	public final static int ORDER_NOT_EXIST = 1;
	public final static int PIN_NOT_EXIST = 2;
	public final static int DB_ERROR = 3;
	public final static int SOCKET_ERROR = 4;
	
	public int errType = UNKNOWN;
	
	public PrintFault(int err){
		super();
		errType = err;
	}
	
}
