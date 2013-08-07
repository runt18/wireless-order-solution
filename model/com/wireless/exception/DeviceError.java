package com.wireless.exception;

public class DeviceError extends ErrorEnum{
	
	/* cord range : 7559 - 7599 */
	public static final ErrorCode DEVICE_NOT_EXIST = build(7559, "查找的终端设备不存在.");
	public static final ErrorCode DEVICE_ID_DUPLICATE = build(7560, "已存在此终端设备ID");
	
	private DeviceError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.DEVICE, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.DEVICE, code);
	}
}
