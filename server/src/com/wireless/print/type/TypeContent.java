package com.wireless.print.type;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;

public abstract class TypeContent implements Content{

	static class StyleContent implements Content{
		
		private PStyle mStyle;
		private final short mRegionId;
		private final int mOrderId;
		private final Content mStyleBody;
		
		StyleContent(short regionId, int orderId, Content itemBody){
			this.mRegionId = regionId;
			this.mOrderId = orderId;
			this.mStyleBody = itemBody;
		}
		
		@Override
		public String toString(){
			return mStyleBody.toString();
		}
		
		/**
		 * Return the bytes to this style content.
		 * The format to bytes stream looks like below.
		 * <p>style : region_id : order_id[4] : lenOfDate : order_date : lenOfContent[2] : content</p>
		 * @return the bytes to this print item.
		 */
		@Override
		public byte[] toBytes(){
			byte[] bytesToDate;
			try {
				bytesToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				bytesToDate = new byte[0];
			}	
			
			byte[] bytesToBody = mStyleBody.toBytes();
			int len = 1 + /* style takes up 1 byte */
					  1 + /* region id takes up 1 byte */
					  4 + /* order id takes up 4 bytes */
					  1 + /* length of order date takes up 1 byte */
					  bytesToDate.length  + /* length of value to order date */
					  2  + /* length of print content takes up 2 bytes */
					  bytesToBody.length	/* length of bytes to item body */;
			
			byte[] bytesToItem = new byte[len];
			
			//assign the style
			bytesToItem[0] = (byte)(mStyle.getVal() & 0x000000FF);
			//assign the region
			bytesToItem[1] = (byte)(mRegionId);
			//assign the order id
			bytesToItem[2] = (byte)(mOrderId & 0x000000FF);
			bytesToItem[3] = (byte)((mOrderId & 0x0000FF00) >> 8);
			bytesToItem[4] = (byte)((mOrderId & 0x00FF0000) >> 16);
			bytesToItem[5] = (byte)((mOrderId & 0xFF000000) >> 24);
			//assign the length of order date
			bytesToItem[6] = (byte)bytesToDate.length;
			//assign the value of order date
			System.arraycopy(bytesToDate, 0, bytesToItem, 7, bytesToDate.length);
			//assign the length of item body
			bytesToItem[7 + bytesToDate.length] = (byte)(bytesToBody.length & 0x000000FF);
			bytesToItem[7 + bytesToDate.length + 1] = (byte)((bytesToBody.length >> 8) & 0x000000FF);
			//assign the value of item body
			System.arraycopy(bytesToBody, 0, bytesToItem, 7 + bytesToDate.length + 2, bytesToBody.length);
			
			return bytesToItem;
		}
	};
	
	private final PType mPrintType;
	
	TypeContent(PType printType){
		this.mPrintType = printType;
	}
	
	/**
	 * Subclass should override this method to create content of each style.
	 * @param style style the content created according to 
	 * @return the content to style
	 * @see PStyle
	 */
	protected abstract StyleContent createItem(PStyle style);
	
	/**
	 * Return the bytes to this print type content.
	 * It combines each content style created by {@link TypeContent#createItem(PStyle)}, as looks like. 
	 * <style_content_1> : <style_content_2> : ...
	 * @return the bytes to this print content
	 */
	@Override
	public byte[] toBytes(){
		ContentCombinator combinator = new ContentCombinator();
		for(PStyle style : PStyle.values()){
			StyleContent styleContent = createItem(style);
			if(styleContent != null){
				styleContent.mStyle = style;
				combinator.append(styleContent);
			}
		}
		
		return combinator.toBytes();
	}
	
	public PType getPrintType(){
		return mPrintType;
	}
}
