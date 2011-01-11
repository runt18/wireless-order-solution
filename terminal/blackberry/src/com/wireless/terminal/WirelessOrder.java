package com.wireless.terminal;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.*;
import java.util.*;

/**
 * This sample enables client/server communication using a simple implementation 
 * of TCP sockets. The client application allows the user to select direct TCP as
 * the connection method.  If direct TCP is not selected, a proxy TCP connection
 * is opened using the BlackBerry MDS Connection Service. The server application 
 * can be found in com/rim/samples/server/socketdemo. 
 */
public class WirelessOrder extends UiApplication{

	public static Vector FoodMenu = new Vector();
	
	static{
		PersistentObject store = PersistentStore.getPersistentObject(NetParam.PERSISTENT_NET_PARAM_ID);                       

		// Synchronize on the PersistentObject so that no other object can
		// acquire the lock before we finish our commit operation.     
		synchronized(store){         
			// If the PersistentObject is empty, initialize it
			if(store.getContents() == null){
				store.setContents(new NetParam());
				PersistentObject.commit(store);
			}            
		}  
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        WirelessOrder wireless_order = new WirelessOrder();
        wireless_order.enterEventDispatcher();
	}

	public WirelessOrder(){
		pushScreen(new OrderMainScreen());
	}
	

} 


