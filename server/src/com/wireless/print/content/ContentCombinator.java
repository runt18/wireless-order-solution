package com.wireless.print.content;

import java.util.ArrayList;
import java.util.List;

public class ContentCombinator implements Content {

	private List<Content> mContents = new ArrayList<Content>();
	
	public ContentCombinator() {
		
	}

	public ContentCombinator(List<? extends Content> contents){
		mContents.addAll(contents);
	}
	
	public ContentCombinator append(Content content){
		mContents.add(content);
		return this;
	}
	
	public ContentCombinator append(List<? extends Content> contents){
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

	@Override
	public int getId() {
		return 0;
	}
}
