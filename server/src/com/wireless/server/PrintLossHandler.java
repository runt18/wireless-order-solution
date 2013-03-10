package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;

class PrintLossHandler implements Runnable{

	private Socket _sock;
	private int _restaurantID;
	
	PrintLossHandler(Socket sock, int restaurantID){
		_sock = sock;
		_restaurantID = restaurantID;
	}
	
	@Override
	public void run(){
		/**
		 * Copy the unprinted request to a temporary list
		 */
		LinkedList<ProtocolPackage> printReqs = new LinkedList<ProtocolPackage>();
		synchronized(WirelessSocketServer.printLosses){
			List<ProtocolPackage> printLosses = WirelessSocketServer.printLosses.get(_restaurantID);
			if(printLosses != null){
				printReqs.addAll(printLosses);
			}			
		}
		
		/**
		 * Just return if no unprinted request exist
		 */
		if(printReqs.isEmpty()){
			return;
		}
		
		/**
		 * Enumerate the list queue to send all the unprinted requests to client again.
		 */
		synchronized(_sock){
			
			try{
				
				while(printReqs.size() != 0){
					
					_sock.sendUrgentData(0);
					
					ProtocolPackage reqPrint = printReqs.peek();
						
					reqPrint.writeToStream(_sock.getOutputStream());					
					
					//_sock.sendUrgentData(0);
					
					printReqs.remove();	
					/**
					 * Receive the response within 10s timeout
					 */
					ProtocolPackage respPrint = new ProtocolPackage(); 
					respPrint.readFromStream(_sock.getInputStream(), 10 * 1000);
					/**
					 * Remove the unprinted request from the queue if receiving ACK
					 */
					if(respPrint.header.seq == reqPrint.header.seq){
						if(respPrint.header.mode == Mode.PRINT && respPrint.header.type == Type.ACK){
													
						}							
					}
				}
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		/**
		 * If succeed to send all the unprinted requests, then remove the corresponding restaurant.
		 * Otherwise append the remaining unprinted request to the print loss queue. 
		 */
		synchronized(WirelessSocketServer.printLosses){		
			WirelessSocketServer.printLosses.remove(_restaurantID);
			if(!printReqs.isEmpty()){
				WirelessSocketServer.printLosses.put(_restaurantID, printReqs);
			}
		}

	}	
}