package com.wireless.startup;

import net.rim.device.api.ui.Screen;

import com.wireless.terminal.PlatformInfo;
import com.wireless.terminal.WirelessOrder;

public class Startup {
	public static void main(String[] args) {
        new WirelessOrder(args, new PlatformInfo(){
        	
        	public String getModuleName(){
        		return "WirelessOrderTerminal_BB45";
        	}
        	
			public String getVersionUrl() {
				return "/ota/bb45/version.php";
			}

			public String getOTAUrl() {
				return "/ota/bb45/ota/WirelessOrderTerminal_BB45.jad";
			}

			public String getHelpUrl() {
				return "/help/bb45/bb8100.html";
			}

			public void setVKeyBoard(Screen context, int visibility) {				
				// TODO Auto-generated method stub
				
			}

        	
        });
	}
}
