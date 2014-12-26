package com.wireless.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class OrderReqHandler implements Runnable{
	
    private boolean _isRunning = false;
    private ServerSocket _server = null;
    
    void kill(){
    	if(_server != null){
    		try{
    			_server.close();
    		}catch(IOException e){}
    	}
    	_isRunning = false;
    }
    
	public void run(){
		try{
			_server = new ServerSocket(WirelessSocketServer.socket_listen);
			Socket connection = null;
			_isRunning = true;
			while(_isRunning){
				try{
					connection = _server.accept();
					WirelessSocketServer.threadPool.execute(new OrderHandler(connection));
				}catch(IOException e){
					if(connection != null){
						try{
							connection.close();
						}catch(IOException igonred){}
					}
				}
			}
		}catch(IOException e){
			System.err.println(e.toString());
		}
	}
}
