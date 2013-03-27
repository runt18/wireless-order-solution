package com.wireless.exception;

import java.util.Collection;
import java.util.HashMap;

public abstract class ErrorEnum {

	private static class Key{
		private final ErrorType type;
		private final int code;
		private Key(ErrorType type, int code){
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
	
	public final static int UNKNOWN_CODE = 9998;
	public final static ErrorCode UNKNOWN = build(ErrorType.UNKNOWN, UNKNOWN_CODE, "unknown", ErrorLevel.DEBUG);
	
	private final static HashMap<Key, ErrorCode> mCodeList = new HashMap<Key, ErrorCode>();
	
	protected static ErrorCode build(ErrorType type, int code, String desc, ErrorLevel level){
		ErrorCode errCode = new ErrorCode(type, code, desc, level);
		mCodeList.put(new Key(type, code), errCode);
		return errCode;
	}

	public static Collection<ErrorCode> values(){
		return mCodeList.values();
	}
	
	public static ErrorCode valueOf(ErrorType type, int code){
		ErrorCode errCode = mCodeList.get(new Key(type, code));
		if(errCode != null){
			return errCode;
		}else{
			throw new IllegalArgumentException("The type(" + type + ") and code(" + code + ") is invalid.");
		}
	}
}
