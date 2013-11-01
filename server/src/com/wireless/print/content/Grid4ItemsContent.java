package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;

public class Grid4ItemsContent extends ConcreteContent {

	private String[] _items;
	private int[] _pos;
	
	protected Grid4ItemsContent(String[] items, int[] pos, PType printType, PStyle style) {
		super(printType, style);
		_items = items;
		_pos = pos;
	}

	/**
	 * Generate a single of the shift info as below.<br>
	 * -----------------------------------<br>
	 * Item_1   Item_2   Item_3   Item_4  <br>
	 *          |        |        |
	 *        pos_1    pos_2    pos_3 
	 * -----------------------------------<br>
	 * @return the grid with 4 items
	 */
	@Override
	public String toString(){
		
		try{
			StringBuilder space1 = new StringBuilder();
			int nSpace = _pos[0] - _items[0].getBytes("GBK").length;
			for(int i = 0; i < nSpace; i++){
				space1.append(" ");
			}
			
			StringBuilder space2 = new StringBuilder();
			nSpace = _pos[1] - _items[0].getBytes("GBK").length - space1.length() - _items[1].getBytes("GBK").length;
			for(int i = 0; i < nSpace; i++){
				space2.append(" ");
			}
			
			StringBuilder space3 = new StringBuilder();
			nSpace = _pos[2] - _items[0].getBytes("GBK").length - space1.length() - _items[1].getBytes("GBK").length - space2.length() - _items[2].getBytes("GBK").length;
			for(int i = 0; i < nSpace; i++){
				space3.append(" ");
			}
			
			return _items[0] + space1 + _items[1] + space2 + _items[2] + space3 + _items[3];
			
		}catch(UnsupportedEncodingException e){
			return "Unsupported Encoding";
		}
	}
	
}
