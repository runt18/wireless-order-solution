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

/**
 * This class represents a persistable net parameter object.  
 * It contains information such as the network address, port, APN, user name and password.
 * Classes to be persisted must implement interface Persistable and can only can contain members which
 * themselves implement Persistable or are inherently persistable. 
 */ 
final class Params{
	private static Vector _params;
	
	static final int NET_ADDR = 0;
	static final int NET_PORT = 1;
	static final int NET_APN = 2;
	static final int NET_USER = 3;
	static final int NET_PWD = 4;
	static final int PRINT_ACTION = 5;
	static final int CONN_TIME_OUT = 6;
	static final int CONN_TYPE = 7;
	
    private static final long PERSISTENT_PARAM_ID = 0x230157d6843fDefEL;
    
    /**
     * Restore the parameters stored in persist storage if it has exist,
     * and initialize the parameters if not exist before.  
     */
	static void init(){
		PersistentObject store = PersistentStore.getPersistentObject(PERSISTENT_PARAM_ID);
		synchronized(store){  
			//restore the persist storage
			_params = (Vector)store.getContents();
			//initialize the parameters if the storage not exist before
			if(_params == null){
				_params = new Vector(8);
				for(int i = 0; i < _params.capacity(); i++){
					_params.addElement(null);
				}
				setParam(NET_ADDR, "58.248.9.158");
				setParam(NET_PORT, "55555");
				setParam(NET_APN, "cmnet");
				setParam(NET_USER, "");
				setParam(NET_PWD, "");
				setParam(PRINT_ACTION, Integer.toString(Params.PRINT_ASYNC));
				setParam(CONN_TIME_OUT, Integer.toString(Params.CONN_TIMEOUT_10));
				setParam(CONN_TYPE, Integer.toString(Params.CONN_MOBILE));	
	        	store.setContents(_params);
	        	store.commit();
			}
		}
	}
	
	/**
	 * Store the parameters to persist storage
	 */
	static void store(){
		PersistentObject store = PersistentStore.getPersistentObject(PERSISTENT_PARAM_ID);
        // Synchronize on the PersistentObject so that no other object can
        // acquire the lock before we finish our commit operation.     
        synchronized(store){         
        	store.setContents(_params);
        	store.commit();
        } 
	}
	
	/**
	 * Restore the parameters from persist storage
	 */
	static void restore(){
		PersistentObject store = PersistentStore.getPersistentObject(PERSISTENT_PARAM_ID);
        synchronized(store){         
            // get the function parameter from the storage
        	_params = (Vector)store.getContents();
        } 
	}
	
	static String getParam(int id){
		return (String)_params.elementAt(id);
	}
	
	static void setParam(int id, String value){
		_params.setElementAt(value, id);
	}
	
	/**
	 * The connect type
	 * mobile network, WiFi
	 */
	static final int CONN_MOBILE = 0;
	static final int CONN_WIFI = 1;
	/**
	 * The connect timeout 
	 * 10s, 15s, 20s
	 */
	static final int CONN_TIMEOUT_10 = 0;
	static final int CONN_TIMEOUT_20 = 2;
	static final int CONN_TIMEOUT_15 = 1;

	/**
	 * The print parameter
	 * synchronize or asynchronous
	 */
	static final int PRINT_ASYNC = 0;
	static final int PRINT_SYNC = 1;
} 


