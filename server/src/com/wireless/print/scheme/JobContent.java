package com.wireless.print.scheme;

import java.util.Date;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.print.content.ConcreteContent;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;

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
	
	/**
	 * Add a header front of actual content, as looks like below.
	 * <p>lenOfPrinterName : printerName : orderId[4] : printTime[4] : lenOfPrintType : printType : lenOfContent[2] : content
	 */
	@Override
	public byte[] toBytes() {
		
		Content printerNameContent = new ConcreteContent(mPrintType, PStyle.PRINT_STYLE_UNKNOWN){
			@Override
			public String toString(){
				return mPrinter.getName();
			}
		};
		
		Content orderIdContent = new Content(){
			@Override
			public byte[] toBytes() {
				return new byte[4];
			}
		};
		
		Content printTimeContent = new Content(){
			@Override
			public byte[] toBytes() {
				byte[] bytesToPrintTime = new byte[4];
				bytesToPrintTime[0] = (byte)(mPrintTime & 0x000000FF);
				bytesToPrintTime[1] = (byte)((mPrintTime & 0x0000FF00) >> 8);
				bytesToPrintTime[2] = (byte)((mPrintTime & 0x00FF0000) >> 16);
				bytesToPrintTime[3] = (byte)((mPrintTime & 0xFF000000) >> 24);
				return bytesToPrintTime;
			}
			
		};
		
		Content printTypeContent = new ConcreteContent(mPrintType, PStyle.PRINT_STYLE_UNKNOWN){
			@Override
			public String toString(){
				return mPrintType.getDesc();
			}
		};
		
		return new ContentCombinator().append(printerNameContent)
				  					  .append(orderIdContent)
				  					  .append(printTimeContent)
				  					  .append(printTypeContent)
				  					  .append(mPrintContent)
				  					  .toBytes();
	}
	
	@Override
	public String toString(){
		return null;
	}
	
}
