package com.wireless.terminal;

import net.rim.device.api.ui.*;
import java.util.*;

import com.wireless.protocol.PinGen;
import com.wireless.protocol.ReqOrderPackage;
import com.wireless.util.ServerConnector;

/**
 * This sample enables client/server communication using a simple implementation 
 * of TCP sockets. The client application allows the user to select direct TCP as
 * the connection method.  If direct TCP is not selected, a proxy TCP connection
 * is opened using the BlackBerry MDS Connection Service. The server application 
 * can be found in com/rim/samples/server/socketdemo. 
 */
public class WirelessOrder extends UiApplication{

	public static Vector FoodMenu = new Vector();
	
	/**
	 * First of all, the program would restore the parameter from persist storage,
	 * and set the parameter for server connector
	 */
	static{
		Params.init();
		//get the network parameters from the persistent storage
 		ServerConnector.instance().setNetAddr(Params.getParam(Params.NET_ADDR));
 		ServerConnector.instance().setNetPort(Params.getParam(Params.NET_PORT));
 		ServerConnector.instance().setNetAPN(Params.getParam(Params.NET_APN));
 		ServerConnector.instance().setNetUser(Params.getParam(Params.NET_USER));
		ServerConnector.instance().setNetPwd(Params.getParam(Params.NET_PWD));
		ServerConnector.instance().setTimeout(Integer.parseInt(Params.getParam(Params.CONN_TIME_OUT)));
		ServerConnector.instance().setConnType(Integer.parseInt(Params.getParam(Params.CONN_TYPE)));
		//set device id generator
		ReqOrderPackage.setGen(new com.wireless.protocol.PinGen(){
			public int getDeviceId(){
				return net.rim.device.api.system.DeviceInfo.getDeviceId();
			}
			public short getDeviceType(){
				return PinGen.BLACK_BERRY;
			}
		});
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        WirelessOrder wireless_order = new WirelessOrder();
        wireless_order.enterEventDispatcher();
	}

	public WirelessOrder(){
		invokeLater(new Runnable(){
			public void run(){
				pushGlobalScreen(new StartupScreen(), 1, UiEngine.GLOBAL_MODAL);				
			}
		});		
	}
	

} 


