package com.wireless.sccon;

import java.io.IOException;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.req.RequestPackage;

public class ServerConnector{
	private String _netAddr = null;
	private int _netPort = 0;
	private int _timeout = 10000;
	
	private static ServerConnector _instance = new ServerConnector();
	
 	private ServerConnector(){
		
	}
	
	public static ServerConnector instance(){
		return _instance;
	}
	
	public void setNetAddr(String netAddr){
		_netAddr = netAddr;
	}
	
	public void setNetPort(int netPort){
		_netPort = netPort;
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 **/
	public ProtocolPackage ask(RequestPackage req) throws IOException{
		return ask(req, _timeout);
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 **/
	public ProtocolPackage ask(RequestPackage req, int timeout) throws IOException{
		return new Session(req, timeout).execute(_netAddr, _netPort);
	}
}
