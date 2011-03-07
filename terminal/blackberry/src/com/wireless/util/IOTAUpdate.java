package com.wireless.util;

public interface IOTAUpdate {
	public void preOTAUpdate();
	public void passOTAUpdate(String latestVer, String url4OTA);
	public void failOTAUpdate(String errMsg);
	public void postOTAUpdate();
}
