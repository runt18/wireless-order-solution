package com.wireless.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Mode;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.RespNAK;
import com.wireless.protocol.RespOTAUpdate;
import com.wireless.protocol.RespPrintLogin;
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
	private InputStream _in = null;
	private OutputStream _out = null;
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

			//listened on port for the print login service
			_server = new ServerSocket(WirelessSocketServer.printer_listen);
			Socket connection = null;
			_isRunning = true;
			while(_isRunning){
				ProtocolPackage loginReq = null;
				DBCon dbCon = new DBCon();
				try{
					connection = _server.accept();
					_in = new BufferedInputStream(new DataInputStream(connection.getInputStream()));
					_out = new BufferedOutputStream(new DataOutputStream(connection.getOutputStream()));

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
					loginReq = recv(_in, 100000);
					
					//handle the printer login request					
					if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_LOGIN){
						//get the user name and password from the body
						int len = loginReq.body[0];
						String user = new String(loginReq.body, 1, len);
						len = loginReq.body[len + 1];
						String pwd = new String(loginReq.body, loginReq.body[0] + 2, len);
						
						//access the database to get the password and restaurant id according to the user
						dbCon.connect();
						String sql = "SELECT id, pwd, restaurant_name FROM " + WirelessSocketServer.database + ".restaurant WHERE account='" + user + "'";
						dbCon.rs = dbCon.stmt.executeQuery(sql);
						
						//check to see whether the account exist or not
						if(dbCon.rs.next()){	
							//check to see whether the password is matched or not
							if(pwd.equals(dbCon.rs.getString("pwd"))){
								
								int restaurantID = dbCon.rs.getInt("id");
								String restaurantName = dbCon.rs.getString("restaurant_name");
								dbCon.rs.close();
								//get the related kitchen information 
								sql = "SELECT alias_id, name FROM " + WirelessSocketServer.database + ".kitchen WHERE restaurant_id=" + restaurantID;
								dbCon.rs = dbCon.stmt.executeQuery(sql);
								ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
								while(dbCon.rs.next()){
									kitchens.add(new Kitchen(dbCon.rs.getString("name"),
															 dbCon.rs.getShort("alias_id")));															
								}
								dbCon.rs.close();
								//respond with the related kitchen information
								send(_out, new RespPrintLogin(loginReq.header, kitchens.toArray(new Kitchen[kitchens.size()]), restaurantName));
								
								//put the restaurant id and the associated socket to the tree map's socket list
								synchronized(WirelessSocketServer.printerConnections){
									ArrayList<Socket> printerSockets = WirelessSocketServer.printerConnections.get(new Integer(restaurantID));
									//just add the new connection if other connections have been exist before 
									if(printerSockets != null){
										/**
										 * Before adding the new socket connection,
										 * check other sockets to see if connected or NOT.
										 * Remove the sockets to this restaurant if NOT valid any more.
										 */
										Iterator<Socket> iter = printerSockets.iterator();
										while(iter.hasNext()){
											Socket conn = iter.next();
											try{
												conn.sendUrgentData(0);
											}catch(IOException e){
												try{
													conn.close();
												}finally{
													iter.remove();
												}
											}
										}
										/**
										 * Add the new socket connection
										 */
										printerSockets.add(connection);		
										
									
									}else{
										/**
										 * Create a new connection list if no connections exist before.
										 */
										printerSockets = new ArrayList<Socket>();
										printerSockets.add(connection);
										WirelessSocketServer.printerConnections.put(new Integer(restaurantID), printerSockets);									
									}
								}
								
								/**
								 * Perform to print the loss receipt
								 */
								WirelessSocketServer.threadPool.execute(new PrintLossHandler(connection, restaurantID));
								
							}else{
								throw new BusinessException("The password is not correct.", ErrorCode.PWD_NOT_MATCH);
							}						
						}else{
							throw new BusinessException("The user \"" + user + "\" doesn't exist.", ErrorCode.ACCOUNT_NOT_EXIST);						
						}
						
					//handle the printer OTA request to get the OTA host address and port
					}else if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_OTA){
						if(WirelessSocketServer.OTA_IP.length() == 0 || WirelessSocketServer.OTA_Port.length() == 0){
							send(_out, new RespNAK(loginReq.header));
						}else{
							send(_out, new RespOTAUpdate(loginReq.header, WirelessSocketServer.OTA_IP, WirelessSocketServer.OTA_Port));
						}
						
					}else{
						throw new BusinessException("The mode or type doesn't belong to print service");
					}					
					
				}catch(BusinessException e){
					respNAK(loginReq, e.errCode);
					e.printStackTrace();
					closeSocket(connection);
					
				}catch(IOException e){
					respNAK(loginReq, ErrorCode.UNKNOWN);
					e.printStackTrace();
					closeSocket(connection);
					
				}catch(SQLException e){
					respNAK(loginReq, ErrorCode.UNKNOWN);
					e.printStackTrace();
					closeSocket(connection);
					
				}catch(Exception e){
					respNAK(loginReq, ErrorCode.UNKNOWN);
					e.printStackTrace();
					closeSocket(connection);
					
				}finally{	
					
					dbCon.disconnect();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}		
	}
	
	/**
	 * Response with NAK to client.
	 * @param loginReq
	 * 			the login request
	 * @param errCode
	 * 			the error code
	 */
	private void respNAK(ProtocolPackage loginReq, byte errCode){
		if(loginReq != null){
			try{
				send(_out, new RespNAK(loginReq.header));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Release the socket resources in the case the printer login failed .
	 * @param con The socket connection between the printer and remote server.
	 * @throws IOException Throws if the close socket failed
	 */
	private void closeSocket(Socket con) throws IOException{
		if(con != null){
			con.close();
			con = null;
		}
		if(_in != null){
			_in.close();
			_in = null;
		}
		if(_out != null){
			_out.close();
			_out = null;
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
				_sock.sendUrgentData(0);
				
				InputStream in = new BufferedInputStream(_sock.getInputStream());
				OutputStream out = new BufferedOutputStream(_sock.getOutputStream());
				
				while(printReqs.size() != 0){
					ProtocolPackage reqPrint = printReqs.peek();
						
					send(out, reqPrint);	
					/**
					 * Receive the response within 10s timeout
					 */
					ProtocolPackage respPrint = recv(in, 10 * 1000);
					/**
					 * Remove the unprinted request from the queue if receiving ACK
					 */
					if(respPrint.header.seq == reqPrint.header.seq){
						if(respPrint.header.mode == Mode.PRINT && respPrint.header.type == Type.ACK){
							printReqs.remove();
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
