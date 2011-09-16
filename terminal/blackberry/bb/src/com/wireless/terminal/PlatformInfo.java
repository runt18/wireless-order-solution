package com.wireless.terminal;

import net.rim.device.api.ui.Screen;

public interface PlatformInfo {
	
	public final static int HIDE = 0;
	public final static int SHOW = 1;
	
	public String getModuleName();
	public String getVersionUrl();
	public String getOTAUrl();
	public String getHelpUrl();
	public void setVKeyBoard(Screen context, int visibility);
}
