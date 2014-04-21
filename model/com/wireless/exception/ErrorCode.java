package com.wireless.exception;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public final class ErrorCode implements Parcelable{
	
	static class Key{
		private final ErrorType type;
		private final int code;
		Key(ErrorType type, int code){
			this.type = type;
			this.code = code;
		}
		
		@Override
		public int hashCode(){
			int result = 17;
			result = result * 31 + type.hashCode();
			result = result * 31 + code;
			return result;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj == null || !(obj instanceof Key)){
				return false;
			}else{
				return type == ((Key)obj).type && code == ((Key)obj).code;
			}
		}
	}
	
	public final static byte ER_PARCELABLE_SIMPLE = 0;
	public final static byte ER_PARCELABLE_COMPLEX = 1;

	private Key key;
	private String desc;
	private ErrorLevel level;
	
	private ErrorCode(){
		this.desc = null;
		this.level = ErrorLevel.DEBUG;
	}
	
	ErrorCode(ErrorType type, int code, String desc, ErrorLevel level){
		this.key = new Key(type, code);
		this.desc = desc;
		this.level = level;
	}
	
	public Key key(){
		return this.key;
	}
	
	public int getCode(){
		return this.key.code;
	}
	
	public String getDesc(){
		if(desc == null){
			return "";
		}
		return this.desc;
	}
	
	void setDesc(String desc){
		this.desc = desc;
	}
	
	@Override
	public int hashCode(){
		return this.key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof ErrorCode)){
			return false;
		}else{
			return key.equals(((ErrorCode)obj).key);
		}
	}
	
	@Override
	public String toString(){
		return ": " + this.key.type + 
			   ", code:" + this.key.code +
			   ", desc:" + this.desc +
			   ", " + this.level;
	}
	
	public ErrorLevel getLevel(){
		return this.level;
	}
	
	public ErrorType getType(){
		return this.key.type;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == ER_PARCELABLE_SIMPLE){
			dest.writeInt(this.key.type.getVal());
			dest.writeInt(this.key.code);
			dest.writeString(this.desc);
			dest.writeInt(this.level.getVal());
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		int flag = source.readByte();
		if(flag == ER_PARCELABLE_SIMPLE){
			ErrorType type = ErrorType.valueOf(source.readInt());
			int code = source.readInt();
			this.key = new Key(type, code);
			this.desc = source.readString();
			this.level = ErrorLevel.valueOf(source.readInt());
		}
	}
	
	public final static Parcelable.Creator<ErrorCode> CREATOR = new Parcelable.Creator<ErrorCode>() {
		
		public ErrorCode[] newInstance(int size) {
			return new ErrorCode[size];
		}
		
		public ErrorCode newInstance() {
			return new ErrorCode();
		}
	};
}
