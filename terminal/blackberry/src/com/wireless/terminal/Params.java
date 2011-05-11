package com.wireless.terminal;

import java.util.Vector;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

/**
 * This class represents a persistable net parameter object.  
 * It contains information such as the network address, port, APN, user name and password.
 * Classes to be persisted must implement interface Persistable and can only can contain members which
 * themselves implement Persistable or are inherently persistable. 
 */ 
public final class Params{
	private static Vector _params;
	
	public static final int NET_ADDR = 0;
	public static final int NET_PORT = 1;
	public static final int NET_APN = 2;
	public static final int NET_USER = 3;
	public static final int NET_PWD = 4;
	public static final int PRINT_ACTION = 5;
	public static final int CONN_TIME_OUT = 6;
	public static final int CONN_TYPE = 7;
	public static final int AUTO_STARTUP = 8;
	
    private static final long PERSISTENT_PARAM_ID = 0x330157d6843fDefEL;
    
    /**
     * Restore the parameters stored in persist storage if it has exist,
     * and initialize the parameters if not exist before.  
     */
	public static void init(){
		PersistentObject store = PersistentStore.getPersistentObject(PERSISTENT_PARAM_ID);
		synchronized(store){  
			//restore the persist storage
			_params = (Vector)store.getContents();
			//initialize the parameters if the storage not exist before
			if(_params == null){
				_params = new Vector(9);
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
				setParam(AUTO_STARTUP, Integer.toString(Params.OFF));
	        	store.setContents(_params);
	        	store.commit();
			}
		}
	}
	
	/**
	 * Store the parameters to persist storage
	 */
	public static void store(){
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
	public static void restore(){
		PersistentObject store = PersistentStore.getPersistentObject(PERSISTENT_PARAM_ID);
        synchronized(store){         
            // get the function parameter from the storage
        	_params = (Vector)store.getContents();
        } 
	}
	
	public static String getParam(int id){
		return (String)_params.elementAt(id);
	}
	
	public static void setParam(int id, String value){
		_params.setElementAt(value, id);
	}
	
	/**
	 * The connect type
	 * mobile network, WiFi
	 */
	public static final int CONN_MOBILE = 0;
	public static final int CONN_WIFI = 1;
	/**
	 * The connect timeout 
	 * 10s, 15s, 20s
	 */
	public static final int CONN_TIMEOUT_10 = 0;
	public static final int CONN_TIMEOUT_20 = 2;
	public static final int CONN_TIMEOUT_15 = 1;

	/**
	 * The print parameter
	 * synchronize or asynchronous
	 */
	public static final int PRINT_ASYNC = 0;
	public static final int PRINT_SYNC = 1;
	
	/**
	 * The options for check box field
	 */
	public static final int OFF = 0;
	public static final int ON = 1;
}