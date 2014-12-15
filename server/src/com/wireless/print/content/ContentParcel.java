package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class ContentParcel implements Content, Parcelable{

	//private final static String SEP = "CTRL-ENTER";
	
	private byte[] content;
	
	private ContentParcel(){
		
	}
	
	public ContentParcel(Content content){
		this.content = content.toBytes();
	}
	
	@Override
	public byte[] toBytes() {
		return content;
	}

	@Override
	public String toString(){
		try {
			return new String(content, "GBK");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeBytes(content);
	}

	@Override
	public void createFromParcel(Parcel source) {
		content = source.readBytes();
	}

	public final static Parcelable.Creator<ContentParcel> CREATOR = new Parcelable.Creator<ContentParcel>(){

		@Override
		public ContentParcel newInstance() {
			return new ContentParcel();
		}
		
		@Override
		public ContentParcel[] newInstance(int size){
			return new ContentParcel[size];
		}
		
	};
}
