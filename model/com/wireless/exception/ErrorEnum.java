package com.wireless.exception;

import java.util.Collection;
import java.util.HashMap;

import com.wireless.exception.ErrorCode.Key;

public abstract class ErrorEnum {

	public final static int UNKNOWN_CODE;
	public final static ErrorCode UNKNOWN;
	private final static HashMap<Key, ErrorCode> mCodeList;
	
	//Put these object to static initialize block to assure the dependence among them.
	static{
		 mCodeList = new HashMap<Key, ErrorCode>();
		 UNKNOWN_CODE = 9998;
		 UNKNOWN = build(ErrorType.UNKNOWN, UNKNOWN_CODE, "unknown", ErrorLevel.DEBUG);
	}
	
	protected static ErrorCode build(ErrorType type, int code, String desc, ErrorLevel level){
		ErrorCode errCode = new ErrorCode(type, code, desc, level);
		mCodeList.put(errCode.key(), errCode);
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
