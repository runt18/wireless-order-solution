package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintOrder;
import com.wireless.pack.req.RequestPackage;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.print.content.Content;

/**
 * The print handler is used for handling the print request.
 * Usually, the print request would be sent after inserting or being paid order,
 * it can run in synchronized or asynchronous mode.
 * In the synchronized mode, the order request must wait until the print request is done,
 * and responds with an ACK or NAK to terminal, so that let the terminal know whether succeed in printing or not.
 * While in the asynchronous mode, the order request returns immediately regardless of the print request,
 * that means the terminal doesn't care the status of print request any more.
 * And the synchronized and asynchronous mode can be chosen by setting the reserved byte of the protocol header. 
 */
public class PrintHandler implements Runnable{
	
	private final Staff _term;
	private final List<Content> _contents = new ArrayList<Content>();
	
	public PrintHandler(Staff term){
		if(term == null){
			throw new IllegalArgumentException("The terminal passed can NOT be NULL.");
		}
		_term = term;
	}
	
	public PrintHandler(Staff term, Content content){
		this(term);
		if(content != null){
			_contents.add(content);
		}
	}
	
	public PrintHandler(Staff term, List<Content> contents){
		this(term);
		_contents.addAll(contents);
	}
	
	public PrintHandler addContent(Content content){
		if(content != null){
			_contents.add(content);
		}
		return this;
	}
	
	/**
	 * Fire to execute print action in thread pool.
	 */
	public final void fireAsync(){
		if(!_contents.isEmpty()){
			WirelessSocketServer.threadPool.execute(this);
		}
	}
	
	/**
	 * Fire to execute print handler in the thread of caller.
	 */
	public final void fireSync(){
		run();
	}
	
	/**
	 * In this method, the print handler gets the template according to the print
	 * function code, send the print request and waits for an ACK (means successfully)
	 * or a NAK (means failed). If an IO error occurs, we close this connection and remove
	 * it from the printer connections.  
	 */
	public void run(){
		
		for(Content content : _contents){
			
			boolean isPrintOk = false;
			
			RequestPackage reqPrint = new ReqPrintOrder(content.toBytes());
			
			for(Socket sock : PrinterConnections.instance().get(_term.getRestaurantId())){
				if(sock == null){
					continue;
				}
				try{						
					/**
					 * Since socket would be shared with the print actions to the same restaurant,
					 * make the communication to the socket in synchronized mode.
					 */
					synchronized(sock){
						
						/**
						 * Check to see whether the printer connection is connected before performing the print action.
						 * Remove the socket if the connection has been disconnected.
						 */
						sock.sendUrgentData(0);
						
						//send the print request
						reqPrint.writeToStream(sock.getOutputStream());
						
						//_socks[i].sendUrgentData(0);
						
						//any socket works means print OK
						isPrintOk = true;							
						
						//receive the response and timeout after 10s
						ProtocolPackage respPrint = new ProtocolPackage(); 
						respPrint.readFromStream(sock.getInputStream(), 10 * 1000);
						
						//check whether the response's sequence equals to the request's
						if(respPrint.header.seq == reqPrint.header.seq){
							if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
								throw new IOException("The response from printer server is not an ACK.");
							}							
						}
					}
					
				}catch(IOException e){
					e.printStackTrace();
					
					try{
						sock.close();
						
					}catch(IOException e2){
						
					}finally{
						//remove the invalid socket
						PrinterConnections.instance().remove(_term.getRestaurantId(), sock);
						sock = null;
					}		
				}
			}	
			
			if(!isPrintOk){
				PrinterLosses.instance().add(_term.getRestaurantId(), content);
			}
		}
	}
	
}




