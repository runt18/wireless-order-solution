package com.wireless.print.connector;

public class PrintSocketException extends Exception {

	private static final long serialVersionUID = -6154911203564672070L;
	
	public PrintSocketException(String errMsg){
		super(errMsg);
	}

	public PrintSocketException(){
	}
}
