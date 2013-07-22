package com.wireless.print.scheme;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
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
	
	private static class StringContent implements Content{
		
		private final String mContent;
		
		StringContent(String content){
			mContent = content;
		}
		
		@Override
		public byte[] toBytes() {
			byte[] bytesToContent;
			try{
				bytesToContent = mContent.getBytes("GBK");
			}catch(UnsupportedEncodingException e){
				bytesToContent = new byte[0];
			}
			
			byte[] result = new byte[2 + bytesToContent.length];
			result[0] = (byte)(bytesToContent.length & 0x000000FF);
			result[1] = (byte)((bytesToContent.length & 0x0000FF00) >> 8);
			System.arraycopy(bytesToContent, bytesToContent.length, result, 2, bytesToContent.length);
			return result;
		}
	}
	
	/**
	 * Add a header front of actual content, as looks like below.
	 * <p>lenOfPrinterName : printerName : orderId[4] : printTime[8] : lenOfPrintType : printType : lenOfContent[2] : content
	 */
	@Override
	public byte[] toBytes() {
		
		Content orderIdContent = new Content(){
			@Override
			public byte[] toBytes() {
				return new byte[4];
			}
		};
		
		Content printTimeContent = new Content(){
			@Override
			public byte[] toBytes() {
				byte[] bytesToPrintTime = new byte[8];
				bytesToPrintTime[0] = (byte)(mPrintTime & 0x00000000000000FFL);
				bytesToPrintTime[1] = (byte)((mPrintTime & 0x000000000000FF00L) >> 8);
				bytesToPrintTime[2] = (byte)((mPrintTime & 0x0000000000FF0000L) >> 16);
				bytesToPrintTime[3] = (byte)((mPrintTime & 0x00000000FF000000L) >> 24);
				bytesToPrintTime[4] = (byte)((mPrintTime & 0x000000FF00000000L) >> 32);
				bytesToPrintTime[5] = (byte)((mPrintTime & 0x0000FF0000000000L) >> 40);
				bytesToPrintTime[6] = (byte)((mPrintTime & 0x00FF000000000000L) >> 48);
				bytesToPrintTime[7] = (byte)((mPrintTime & 0xFF00000000000000L) >> 56);
				return bytesToPrintTime;
			}
			
		};
		
		return new ContentCombinator().append(new StringContent(mPrinter.getName()))
				  					  .append(orderIdContent)
				  					  .append(printTimeContent)
				  					  .append(new StringContent(mPrintType.getDesc()))
				  					  .append(mPrintContent)
				  					  .toBytes();
	}
	
	@Override
	public String toString(){
		return null;
	}
	
}
