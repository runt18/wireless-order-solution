package com.wireless.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.db.QueryRegion;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Mode;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPing;
import com.wireless.protocol.RespNAK;
import com.wireless.protocol.RespOTAUpdate;
import com.wireless.protocol.RespPrintLogin;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;

/**
 * In the printer login handler, there're two request to handle.
 * 1 - Listening on print port to wait for a login connection. 
 *     Once a printer server on client side requests to 
 *     connect to the server, firstly extract the user name and password to check
 * 	   if it's valid by accessing the database. If valid, put the restaurant id 
 *     and this connection socket to the tree map.
 * 2 - Listening on print port to wait for a OTA request.
 *     Once receiving this request, it would send back the OTA host IP and port
 *     so that let the printer server access the OTA server to check the new version  
 */
public class PrinterLoginHandler extends Handler implements Runnable{
	
	private boolean _isRunning = false;
    private ServerSocket _server = null;
    
    void kill(){
    	if(_server != null){
    		try{
    			_server.close();
    		}catch(IOException e){}
    	}
    	_server = null;
    	_isRunning = false;
    }
    
	public void run(){
		try{
			//listened on port for the print login service
			_server = new ServerSocket(WirelessSocketServer.printer_listen);
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		
		Socket sock = null;
		InputStream in = null;
		OutputStream out = null;
		_isRunning = true;
		
		while(_isRunning){
			ProtocolPackage loginReq = null;
			DBCon dbCon = new DBCon();
			try{
				sock = _server.accept();
				sock.setKeepAlive(true);
				sock.setTcpNoDelay(true);
				//connection.setSendBufferSize(10);
				in = sock.getInputStream();
				out = sock.getOutputStream();

				/**
				 * The login request is as below
				 * mode : type : seq : reserved : pin[6] : len[2] : len1 : user : len2 : pwd
				 * <Header>
				 * mode - PRINT
				 * type - ACK
				 * seq - same as request
				 * reserved - 0x00
				 * pin[6] - same as request
				 * len[2] -  length of the <Body>
				 * <Body>
				 * len1 - 1-byte indicates the length of the user name
				 * user - the user name
				 * len2 - 1-byte indicates the length of the password
				 * pwd - the password related to the user 
				 */	
				
				//read the content from input stream
				loginReq = recv(in, 100000);
				
				//handle the printer login request					
				if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_LOGIN){
					//get the user name and password from the body
					int len = loginReq.body[0];
					String user = new String(loginReq.body, 1, len);
					len = loginReq.body[len + 1];
					//String pwd = new String(loginReq.body, loginReq.body[0] + 2, len);
					
					//access the database to get the password and restaurant id according to the user
					dbCon.connect();
					String sql = "SELECT id, pwd, restaurant_name FROM " + Params.dbName + ".restaurant WHERE account='" + user + "'";
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					
					//check to see whether the account exist or not
					if(dbCon.rs.next()){	
						//check to see whether the password is matched or not
						//if(pwd.equals(dbCon.rs.getString("pwd"))){
							
							int restaurantID = dbCon.rs.getInt("id");
							String restaurantName = dbCon.rs.getString("restaurant_name");
							dbCon.rs.close();
							
							Terminal term = new Terminal();
							term.restaurantID = restaurantID;
							
							//respond with the related kitchen information
							send(out, new RespPrintLogin(loginReq.header, 
														  QueryMenu.queryDepartments(dbCon, "AND DEPT.restaurant_id=" + restaurantID, null),
														  QueryMenu.queryKitchens(dbCon, "AND KITCHEN.restaurant_id=" + restaurantID, null),
														  QueryRegion.exec(dbCon, term),
														  restaurantName));
							
							//put the restaurant id and the associated socket to the tree map's socket list
							synchronized(WirelessSocketServer.printerConnections){
								List<Socket> printerSockets = WirelessSocketServer.printerConnections.get(new Integer(restaurantID));
								//just add the new connection if other connections have been exist before 
								if(printerSockets != null){
									/**
									 * Before adding the new socket connection,
									 * check other sockets to see if connected or NOT.
									 * Remove the sockets to this restaurant if NOT valid any more.
									 */
									Iterator<Socket> iterSock = printerSockets.iterator();
									while(iterSock.hasNext()){
										Socket printerSock = iterSock.next();
										try{
											send(printerSock.getOutputStream(), new ReqPing());
											recv(printerSock.getInputStream(), 3 * 1000);
											//conn.sendUrgentData(0);
										}catch(IOException e){
											try{
												printerSock.close();
											}catch(IOException ex){
												
											}finally{
												iterSock.remove();
											}
										}
									}
									/**
									 * Add the new socket connection
									 */
									printerSockets.add(sock);		
									
								
								}else{
									/**
									 * Create a new connection list if no connections exist before.
									 */
									printerSockets = new ArrayList<Socket>();
									printerSockets.add(sock);
									WirelessSocketServer.printerConnections.put(new Integer(restaurantID), printerSockets);									
								}
							}
							
							/**
							 * Perform to print the loss receipt
							 */
							WirelessSocketServer.threadPool.execute(new PrintLossHandler(sock, restaurantID));
							
						//}else{
						//	throw new BusinessException("The password is not correct.", ErrorCode.PWD_NOT_MATCH);
						//}						
					}else{
						throw new BusinessException("The user \"" + user + "\" doesn't exist.", ErrorCode.ACCOUNT_NOT_EXIST);						
					}
					
				//handle the printer OTA request to get the OTA host address and port
				}else if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_OTA){
					if(WirelessSocketServer.OTA_IP.length() == 0 || WirelessSocketServer.OTA_Port.length() == 0){
						send(out, new RespNAK(loginReq.header));
					}else{
						send(out, new RespOTAUpdate(loginReq.header, WirelessSocketServer.OTA_IP, WirelessSocketServer.OTA_Port));
					}
					
				}else{
					throw new BusinessException("The mode or type doesn't belong to print service");
				}					
				
			}catch(BusinessException e){
				e.printStackTrace();
				dealWithFailure(in, out, sock, loginReq);
				
			}catch(IOException e){
				e.printStackTrace();
				dealWithFailure(in, out, sock, loginReq);
				
			}catch(SQLException e){
				e.printStackTrace();
				dealWithFailure(in, out, sock, loginReq);
				
			}catch(Exception e){
				e.printStackTrace();
				dealWithFailure(in, out, sock, loginReq);
				
			}finally{						
				dbCon.disconnect();
			}
		}
	
	}
	
	/**
	 * Deal with the failure while printer login.
	 * Firstly, response with NAK to client.
	 * Secondly, release the socket and associated input and output stream.
	 * @param in 
	 * 			the input stream resource associated with the socket
	 * @param out
	 * 			the output stream resource associated with the socket
	 * @param sock
	 * 			the socket resource
	 * @param loginReq
	 * 			the login request package
	 */
	private void dealWithFailure(InputStream in, OutputStream out, Socket sock, ProtocolPackage loginReq){
		try{
			send(out, new RespNAK(loginReq.header));
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(in != null){
					in.close();
					in = null;
				}
				if(out != null){
					out.close();
					out = null;
				}
				if(sock != null){
					sock.close();
					sock = null;
				}
			}catch(IOException e){
				e.printStackTrace();
			}				
		}
	}

}

class PrintLossHandler extends Handler implements Runnable{

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
			LinkedList<ProtocolPackage> printLosses = WirelessSocketServer.printLosses.get(_restaurantID);
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
						
					send(_sock.getOutputStream(), reqPrint);					
					
					//_sock.sendUrgentData(0);
					
					printReqs.remove();	
					/**
					 * Receive the response within 10s timeout
					 */
					ProtocolPackage respPrint = recv(_sock.getInputStream(), 10 * 1000);
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

