package com.wireless.startup;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.VirtualKeyboard;

import com.wireless.terminal.PlatformInfo;
import com.wireless.terminal.WirelessOrder;

public class Startup {
	public static void main(String[] args) {
        new WirelessOrder(args, new PlatformInfo() {
			
			public void setVKeyBoard(Screen context, int visibility) {
				if(visibility == SHOW){
					context.getVirtualKeyboard().setVisibility(VirtualKeyboard.SHOW);
				}else if(visibility == HIDE){
					context.getVirtualKeyboard().setVisibility(VirtualKeyboard.HIDE);
				}
			}
			
        	public String getModuleName(){
        		return "WirelessOrderTerminal_BB50";
        	}
			
			public String getVersionUrl() {
				return "/ota/bb50/version.php";
			}
			
			public String getOTAUrl() {
				return "/ota/bb50/WirelessOrderTerminal_BB50.jad";
			}
			
			public String getHelpUrl() {
				return "/help/bb45/bb8100.html";
			}
		});
	}
}
