package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;


public class Grid2ItemsContent extends ConcreteContent {

	private final String mItem1;
	private final int mPos;
	private final String mItem2;
	
	public Grid2ItemsContent(String item1, int pos, String item2, PStyle style) {
		super(PType.PRINT_UNKNOWN, style);
		mItem1 = item1;
		mPos = pos;
		mItem2 = item2;
	}
	
	public Grid2ItemsContent(String item1, String item2, PStyle style){
		super(PType.PRINT_UNKNOWN, style);
		mItem1 = item1;
		mItem2 = item2;
		mPos = Integer.MIN_VALUE;		
	}
	
	/**
	 * Generate a single of the shift info as below.<br>
	 * -----------------------------------<br>
	 * Item_1      Item_2             	  <br>
	 *            |
	 *           pos 
	 * -----------------------------------<br>
	 * @return the gird with 2 items
	 */
	@Override
	public String toString(){
		try{
			
			int nSpace;
			if(mPos < 0){
				nSpace = mLen - 
						 mItem1.getBytes("GBK").length - 
						 mItem2.getBytes("GBK").length;
			}else{
				nSpace = mPos - mItem1.getBytes("GBK").length;
			}

			StringBuffer space = new StringBuffer();
			for(int i = 0; i < nSpace; i++){
				space.append(" ");
			}
			
			return mItem1 + space + mItem2;
			
		}catch(UnsupportedEncodingException e){
			return "Unsupported Encoding";
		}
	}

}
