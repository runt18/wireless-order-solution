package com.wireless.print.connector;

import java.util.ArrayList;
import java.util.List;

import com.wireless.print.content.Content;

public final class PrinterConnector {

	private static final PrinterConnector mInstance = new PrinterConnector();

	private final List<Content> mContents = new ArrayList<Content>();
	
	private PrinterConnector(){
		
	}
	
	public PrinterConnector addContent(Content content){
		if(content != null){
			mContents.add(content);
		}
		return this;
	}
	
	
	
	public static PrinterConnector instance(){
		return mInstance;
	}
	
	
}
