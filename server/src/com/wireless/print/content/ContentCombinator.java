package com.wireless.print.content;

import java.util.ArrayList;
import java.util.List;

import com.wireless.print.PStyle;

public class ContentCombinator extends Content {

	private List<Content> mContents = new ArrayList<Content>();
	
	public ContentCombinator() {
		super(PStyle.PRINT_STYLE_UNKNOWN);
	}

	public ContentCombinator append(Content content){
		mContents.add(content);
		return this;
	}
	
	public ContentCombinator append(List<Content> contents){
		mContents.addAll(contents);
		return this;
	}
	
	@Override
	public String toString(){
		return "";
	}
	
	/**
	 * Return the combined bytes to each content.
	 */
	@Override
	public byte[] toBytes(){
		byte[] bytesToResult = new byte[0];
		
		for(Content content : mContents){
			byte[] bytesToCombine = content.toBytes();
			byte[] tmp = bytesToResult;
			bytesToResult = new byte[bytesToResult.length + bytesToCombine.length];
			
			//Append the bytes to result.
			System.arraycopy(tmp, 0, bytesToResult, 0, tmp.length);
			System.arraycopy(bytesToCombine, 0, bytesToResult, tmp.length, bytesToCombine.length);
			
			bytesToCombine = null;
			tmp = null;
		}
		
		return bytesToResult;
	}
}
