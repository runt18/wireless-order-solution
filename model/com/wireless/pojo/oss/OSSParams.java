package com.wireless.pojo.oss;

public final class OSSParams {
	public final String ACCESS_OSS_ID;
	public final String ACCESS_OSS_KEY;
	public final String OSS_INNER_POINT;
	public final String OSS_OUTER_POINT;
    
	private static OSSParams mInstance;
	
    private OSSParams(String accessId, String accessKey, String innerPoint, String outerPoint){
    	this.ACCESS_OSS_ID = accessId;
    	this.ACCESS_OSS_KEY = accessKey;
    	this.OSS_INNER_POINT = innerPoint;
    	this.OSS_OUTER_POINT = outerPoint;
    }
    
    public static OSSParams init(String accessId, String accessKey, String innerPoint, String outerPoint){
    	mInstance = new OSSParams(accessId, accessKey, innerPoint, outerPoint);
    	return mInstance;
    }
    
}
