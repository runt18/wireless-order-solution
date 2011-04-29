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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
    private Connection _dbCon = null;
    private Statement _stmt = null;
    private ResultSet _rs = null;
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
				        try {   
				        	Class.forName("com.mysql.jdbc.Driver");   
				        } catch (ClassNotFoundException e) { 
				        	e.printStackTrace();   
				        }   
						_dbCon = DriverManager.getConnection(WirelessSocketServer.url, 
								 							 WirelessSocketServer.user, 
								 							 WirelessSocketServer.password);   
						_stmt = _dbCon.createStatement(); 
						String sql = "SELECT id, pwd FROM " + WirelessSocketServer.database + ".restaurant WHERE account='" + user + "'";
						_rs = _stmt.executeQuery(sql);
						
						//check to see whether the account exist or not
						if(_rs.next()){	
							//check to see whether the password is matched or not
							if(pwd.equals(_rs.getString("pwd"))){
								
								int restaurantID = _rs.getInt("id");
								_rs.close();
								//get the related kitchen information 
								sql = "SELECT alias_id, name FROM " + WirelessSocketServer.database + ".kitchen WHERE restaurant_id=" + restaurantID;
								_rs = _stmt.executeQuery(sql);
								ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
								while(_rs.next()){
									kitchens.add(new Kitchen(_rs.getString("name"),
															 _rs.getShort("alias_id")));															
								}
								//respond with the related kitchen information
								send(_out, new RespPrintLogin(loginReq.header, kitchens.toArray(new Kitchen[kitchens.size()])));
								
								//put the restaurant id and the associated socket to the tree map's socket list
								synchronized(WirelessSocketServer.printerConnections){
									ArrayList<Socket> printerSockets = WirelessSocketServer.printerConnections.get(new Integer(restaurantID));
									//just add the new connection if other connections have been exist before 
									if(printerSockets != null){
										printerSockets.add(connection);									
									//create a new connection list if no connections exist before
									}else{
										printerSockets = new ArrayList<Socket>();
										printerSockets.add(connection);
										WirelessSocketServer.printerConnections.put(new Integer(restaurantID), printerSockets);									
									}
								}
							}else{
								throw new PrintLogicException("The password is not correct.", ErrorCode.PWD_NOT_MATCH);
							}						
						}else{
							throw new PrintLogicException("The user \"" + user + "\" doesn't exist.", ErrorCode.ACCOUNT_NOT_EXIST);						
						}
						
					//handle the printer OTA request to get the OTA host address and port
					}else if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_OTA){
						if(WirelessSocketServer.OTA_IP.length() == 0 || WirelessSocketServer.OTA_Port.length() == 0){
							send(_out, new RespNAK(loginReq.header));
						}else{
							send(_out, new RespOTAUpdate(loginReq.header, WirelessSocketServer.OTA_IP, WirelessSocketServer.OTA_Port));
						}
						
					}else{
						throw new PrintLogicException("The mode or type doesn't belong to print service");
					}					
					
				}catch(PrintLogicException e){
					if(loginReq != null){
						try{
							send(_out, new RespNAK(loginReq.header, e.errCode));
						}catch(IOException ex){
							ex.printStackTrace();
						}
					}
					e.printStackTrace();
					closeSocket(connection);
					
				}catch(IOException e){
					if(loginReq != null){
						try{
							send(_out, new RespNAK(loginReq.header));
						}catch(IOException ex){
							ex.printStackTrace();
						}
					}
					e.printStackTrace();
					closeSocket(connection);
					
				}catch(SQLException e){
					if(loginReq != null){
						try{
							send(_out, new RespNAK(loginReq.header));
						}catch(IOException ex){
							ex.printStackTrace();
						}
					}
					e.printStackTrace();
					closeSocket(connection);
					
				}finally{	
					try{
						if(_rs != null){
							_rs.close();
							_rs = null;
						}
						if(_stmt != null){
							_stmt.close();
							_stmt = null;
						}
						if(_dbCon != null){
							_dbCon.close();
							_dbCon = null;
						}
					}catch(SQLException e){
						e.printStackTrace();
					}					
				}
			}
		}catch(IOException e){
			e.printStackTrace();
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
