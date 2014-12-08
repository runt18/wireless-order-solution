package com.wireless.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.printScheme.PrintLossDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespPrintLogin;
import com.wireless.pojo.printScheme.PrintLoss;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.print.content.Content;

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
public class PrinterLoginHandler implements Runnable{
	
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
			ProtocolPackage loginReq = new ProtocolPackage();
			DBCon dbCon = null;
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
				loginReq.readFromStream(in, 100000);

				//handle the printer login request					
				if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_LOGIN){
					//get the user name and password from the body
					int len = loginReq.body[0];
					String user = new String(loginReq.body, 1, len);
					len = loginReq.body[len + 1];
					//String pwd = new String(loginReq.body, loginReq.body[0] + 2, len);

					dbCon = new DBCon();
					
					//access the database to get the password and restaurant id according to the user
					dbCon.connect();
					Restaurant restaurant = RestaurantDao.getByAccount(dbCon, user);
					
					//check to see whether the password is matched or not
					//if(pwd.equals(dbCon.rs.getString("pwd"))){
						
						Staff staff = new Staff();
						staff.setRestaurantId(restaurant.getId());
						
						//respond with the related kitchen information
						new RespPrintLogin(loginReq.header, 
										   restaurant.getName(),
										   WirelessSocketServer.OTA_IP,
										   Integer.parseInt(WirelessSocketServer.OTA_Port)).writeToStream(out);
						
						//put the restaurant id and the associated socket to the socket list
						PrinterConnections.instance().scan(restaurant);
						PrinterConnections.instance().add(restaurant, sock);
						
						//Perform to print the lost receipt in case of NOT empty
						List<Content> lostContents = new ArrayList<Content>();
						for(final PrintLoss loss : PrintLossDao.getByCond(dbCon, staff, null)){
							lostContents.add(new Content(){
								@Override
								public byte[] toBytes() {
									return loss.getContent();
								}
							});
							PrintLossDao.deleteById(dbCon, staff, loss.getId());
						}
						
						if(!lostContents.isEmpty()){
							new PrintHandler(staff, lostContents).fireAsync();
						}
					//}else{
					//	throw new BusinessException("The password is not correct.", ErrorCode.PWD_NOT_MATCH);
					//}						
					
				//handle the printer OTA request to get the OTA host address and port
				}else if(loginReq.header.mode == Mode.PRINT && loginReq.header.type == Type.PRINTER_OTA){
					//FIXME the code below should have been removed
					if(WirelessSocketServer.OTA_IP.length() == 0 || WirelessSocketServer.OTA_Port.length() == 0){
						new RespNAK(loginReq.header).writeToStream(out);
					}else{
						new RespOTAUpdate(loginReq.header, WirelessSocketServer.OTA_IP, WirelessSocketServer.OTA_Port).writeToStream(out);
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
				if(dbCon != null){
					dbCon.disconnect();
				}
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
			new RespNAK(loginReq.header).writeToStream(out);
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
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
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}

