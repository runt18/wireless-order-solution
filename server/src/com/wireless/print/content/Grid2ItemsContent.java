package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.print.PStyle;
import com.wireless.print.PType;


public class Grid2ItemsContent extends ConcreteContent {

	private String _item1;
	private int _pos;
	private String _item2;
	
	public Grid2ItemsContent(String item1, int pos, String item2, PType printType, PStyle style) {
		super(printType, style);
		_item1 = item1;
		_pos = pos;
		_item2 = item2;
	}
	
	public Grid2ItemsContent(String item1, String item2, PType printType, PStyle style){
		super(printType, style);
		_item1 = item1;
		_item2 = item2;
		_pos = Integer.MIN_VALUE;		
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
			if(_pos < 0){
				nSpace = _len - 
						 _item1.getBytes("GBK").length - 
						 _item2.getBytes("GBK").length;
			}else{
				nSpace = _pos - _item1.getBytes("GBK").length;
			}

			StringBuffer space = new StringBuffer();
			for(int i = 0; i < nSpace; i++){
				space.append(" ");
			}
			
			return _item1 + space + _item2;
			
		}catch(UnsupportedEncodingException e){
			return "Unsupported Encoding";
		}
	}

}
