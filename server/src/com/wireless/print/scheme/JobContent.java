package com.wireless.print.scheme;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;

public class JobContent implements Content{

	private final Printer mPrinter;
	
	private final int mRepeat;

	private final PType mPrintType;
	
	private final long mPrintTime;
	
	private final Content mPrintContent;
	
	public JobContent(Printer printer, int repeat, PType printType, Content printContent){
		mPrinter = printer;
		mRepeat = repeat;
		mPrintType = printType;
		mPrintTime = System.currentTimeMillis();
		mPrintContent = printContent;
	}
	
	public Printer getPrinter(){
		return mPrinter;
	}
	
	public int getRepeat(){
		return mRepeat;
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
		
		private byte[] mBytesToContent;
		
		StringContent(String content){
			try{
				mBytesToContent = content.getBytes("GBK");
			}catch(UnsupportedEncodingException e){
				mBytesToContent = new byte[0];
			}
		}
		
		StringContent(Content content){
			mBytesToContent = content.toBytes();
		}
		
		@Override
		public byte[] toBytes() {
			byte[] result = new byte[2 + mBytesToContent.length];
			result[0] = (byte)(mBytesToContent.length & 0x000000FF);
			result[1] = (byte)((mBytesToContent.length & 0x0000FF00) >> 8);
			System.arraycopy(mBytesToContent, 0, result, 2, mBytesToContent.length);
			return result;
		}

		@Override
		public int getId() {
			return 0;
		}
	}
	
	/**
	 * Add a header front of actual content, as looks like below.
	 * <p>lenOfPrinterName : printerName : repeat : orderId[4] : printTime[8] : lenOfPrintType : printType : lenOfContent[2] : content
	 */
	@Override
	public byte[] toBytes() {
		
		Content repeatContent = new Content(){
			@Override
			public byte[] toBytes(){
				return new byte[]{ (byte)mRepeat };
			}

			@Override
			public int getId() {
				return 0;
			}
		};
		
		Content orderIdContent = new Content(){
			@Override
			public byte[] toBytes() {
				return new byte[4];
			}

			@Override
			public int getId() {
				return 0;
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

			@Override
			public int getId() {
				return 0;
			}
			
		};
		
		return new ContentCombinator().append(new StringContent(mPrinter.getName()))
									  .append(repeatContent)
				  					  .append(orderIdContent)
				  					  .append(printTimeContent)
				  					  .append(new StringContent(mPrintType.getDesc()))
				  					  .append(new StringContent(mPrintContent))
				  					  .toBytes();
	}
	
	@Override
	public String toString(){
		return null;
	}

	@Override
	public int getId() {
		return 0;
	}
	
}
