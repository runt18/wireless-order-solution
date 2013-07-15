package com.wireless.print.scheme;

import java.util.Date;

import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.print.content.Content;

public class JobContent implements Content{

	private final Printer mPrinter;

	private final PType mPrintType;
	
	private final long mPrintTime;
	
	private final Content mPrintContent;
	
	public JobContent(Printer printer, PType printType, Content printContent){
		mPrinter = printer;
		mPrintType = printType;
		mPrintTime = new Date().getTime();
		mPrintContent = printContent;
	}
	
	public Printer getPrinter(){
		return mPrinter;
	}
	
	public PType getPrintType(){
		return mPrintType;
	}
	
	public long getPrintTime(){
		return mPrintTime;
	}
	
	public Content getPrintContent(){
		return mPrintContent;
	}
	
	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString(){
		return null;
	}
	
}
