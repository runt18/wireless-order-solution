package com.wireless.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import com.wireless.protocol.ProtocolHeader;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.terminal.Params;

public class ServerConnector{
	private Vector _sessions = new Vector();
	private String _netAddr = null;
	private String _netPort = null;
	private String _netAPN = null;
	private String _netUser = null;
	private String _netPwd = null;
	private String _url = null;
	
	private int _timeout = 10000;
	private int _connType = Params.CONN_MOBILE;
	
	private static ServerConnector _instance = new ServerConnector();
	

 	public ServerConnector(){
		
        Thread connectThread = new Thread(){
        	public void run(){
        		
        		while(true){
        			//wait until new session request 
        			while(_sessions.size() == 0){
        				synchronized(_sessions){        				
        					try{ _sessions.wait(); }
        					catch(InterruptedException e){}
        				}
        			}
        			
        			Session session = null;
        			//get the first session and remove it from the sessions vector 
        			synchronized(_sessions){
        				session = (Session)_sessions.firstElement();
        				_sessions.removeElementAt(0);
        			}
        			SocketConnection conn = null;
        			InputStream in = null;
        			OutputStream out = null;
        			try{
        				//open the socket connection


        				conn = (SocketConnection)Connector.open(_url, Connector.READ_WRITE, true);
        				in = conn.openInputStream();
        				out = conn.openOutputStream(); 
        	            
        				//make the request and send it to server
        				byte[] req_buf = new byte[ProtocolHeader.SIZE +
        				                          session.request.body.length +
        				                          ProtocolPackage.EOP.length];
        				//assign the header to request buffer
        				req_buf[0] = session.request.header.mode;
        				req_buf[1] = session.request.header.type;
        				req_buf[2] = session.request.header.seq;
        				req_buf[3] = session.request.header.reserved;
        				req_buf[4] = session.request.header.pin[0];
        				req_buf[5] = session.request.header.pin[1];
        				req_buf[6] = session.request.header.pin[2];
        				req_buf[7] = session.request.header.pin[3];
        				req_buf[8] = session.request.header.pin[4];
        				req_buf[9] = session.request.header.pin[5];
        				req_buf[10] = session.request.header.length[0];
        				req_buf[11] = session.request.header.length[1];
        				//assign the body to request buffer
        				for(int i = 0; i < session.request.body.length; i++){
        					req_buf[ProtocolHeader.SIZE + i] = session.request.body[i];
        				}
        				//assign the EOP to request buffer
        				for(int i = 0; i < ProtocolPackage.EOP.length; i++){
        					req_buf[ProtocolHeader.SIZE + session.request.body.length + i] = ProtocolPackage.EOP[i];
        				}
        				//send the buffer
        				out.write(req_buf, 0, req_buf.length);
        				
        				//check if receiving the response every 200ms
        				//use the session's timeout to control the loop back times
        				Vector reqBuf = new Vector();
        				boolean isReachEOP = false;
            			boolean isTimeout = true;
        				for(int i = 0; i <= session.timeout / 200; i++){
        					int bytes_avail = in.available();
        					if(bytes_avail == 0){
        						sleep(200);
        					}else{
        						isTimeout = false;
        						byte[] rec_buf = new byte[bytes_avail];
        						final int bytes_read = in.read(rec_buf);
        						reqBuf.addElement(rec_buf);
        						isReachEOP = true;
        						//check if receiving the EOP from the response
        						for(i = 0; i < ProtocolPackage.EOP.length; i++){
        							if(rec_buf[bytes_read - i - 1] != ProtocolPackage.EOP[ProtocolPackage.EOP.length - i - 1]){
        								isReachEOP = false;
        								break;
        							}
        						}					
        						if(isReachEOP){
        							break;
        						} 
        					}
        				}
        				if(isTimeout){
        					session.promptMsg = "连接超时，请检查网络信号或重新连接";
        					
        				}else if(isReachEOP){
        					//in the case receive the response with the EOP,
        					//means the response is complete
        					byte[] req = null;
        					if(reqBuf.size() == 1){
        						//in the case there is only one fragment,
        						//just get it from the request vector
        						req = (byte[])reqBuf.elementAt(0);
        					}else{
        						//in the case there is more than one fragments,
        						//need to incorporate these fragments into one request buffer
        						int index = 0;
        						Enumeration e = reqBuf.elements();
        						//calculate the length of the request
        						while(e.hasMoreElements()){
        							index += ((byte[])e.nextElement()).length;
        						}
        						//allocate the memory for request buffer
        						req = new byte[index];
        						//assign each request fragment into one request buffer
        						e = reqBuf.elements();
        						index = 0;
        						while(e.hasMoreElements()){
        							byte[] tmp = (byte[])e.nextElement();
        							System.arraycopy(tmp, 0, req, index, tmp.length);
        							index += tmp.length;
        						}
        					}
        					
            				//parse the request buffer into protocol header
            				session.response.header.mode = req[0];
            				session.response.header.type = req[1];
            				session.response.header.seq = req[2];
            				session.response.header.reserved = req[3];
            				session.response.header.pin[0] = req[4];
            				session.response.header.pin[1] = req[5];
            				session.response.header.pin[2] = req[6];
            				session.response.header.pin[3] = req[7];
            				session.response.header.pin[4] = req[8];
            				session.response.header.pin[5] = req[9];
            				session.response.header.length[0] = req[10];
            				session.response.header.length[1] = req[11];             				
            				
            				//check if the received data matches the request asked
            				//if so, then notify the request thread to continue
            				if(session.isMatchHeader()){
            					//parse the request buffer into the body
            					//note that the body exclude the EOP
            					session.response.body = new byte[req.length - ProtocolHeader.SIZE - ProtocolPackage.EOP.length];
            					for(int cnt = 0; cnt < session.response.body.length; cnt++){
            						session.response.body[cnt] = req[ProtocolHeader.SIZE + cnt];
            					}
            				}else{
            					throw new Exception("回复的序列号不匹配");
            				}
        				}
            			
        			}catch(IOException e){
        				if(session.detailMsg.length() == 0)
        					session.detailMsg = e.toString();
        				if(session.promptMsg.length() == 0)
        					session.promptMsg = "请求未成功，请检查网络信号或重新连接";
        				
        			}catch(Exception e){
        				if(session.detailMsg.length() == 0)
        					session.detailMsg = e.toString();
        				if(session.promptMsg.length() == 0)
        					session.promptMsg = "请求未成功，请检查网络信号或重新连接";
        				
        			}finally{
        				//notify the ask function 
    					synchronized(session){
    						session.notify();
    					}
        				//close the socket connection finally
        				try{
        					if(in != null){
        						in.close();
        						in = null;
        					}
        					if(out != null){
        						out.close();
        						out = null;
        					}
        					if(conn != null){
        						conn.close();
        						conn = null;
        					}
        				}catch(IOException e){
        					System.err.println(e.toString());  
        				}
        			}
        		}
        	}
        };
        
        connectThread.start();
	}
	
	public static ServerConnector instance(){
		return _instance;
	}
	
	private void concateURL(){
		_url = "socket://" + _netAddr + ":" + _netPort;
		if(_connType == Params.CONN_MOBILE){
			_url = _url + ";deviceside=true";
			if(_netAPN != null){
				_url = _url + ";apn=" + _netAPN;
			}
			if(_netUser != null){
				_url = _url + ";tunnelauthusername=" + _netUser;
			}
			if(_netPwd != null){
				_url = _url + ";tunnelauthpassword=" + _netPwd;
			}
		}else if(_connType == Params.CONN_WIFI){
			_url = _url + ";interface=wifi";
		}else{
			_url = _url + ";interface=wifi";
		}
	}
	
	public void setNetAddr(String netAddr){
		_netAddr = netAddr;
		concateURL();
	}
	
	public void setNetPort(String netPort){
		_netPort = netPort;
		concateURL();
	}
	
	public void setNetAPN(String netAPN){
		_netAPN = netAPN;
		concateURL();
	}
	
	public void setNetUser(String name){
		_netUser = name;
		concateURL();
	}
	
	public void setNetPwd(String pwd){
		_netPwd = pwd;
		concateURL();
	}
	
	public void setTimeout(int timeout){
		if(timeout == Params.CONN_TIMEOUT_10){
			_timeout = 10000;
		}else if(timeout == Params.CONN_TIMEOUT_15){
			_timeout = 15000;
		}else if(timeout == Params.CONN_TIMEOUT_20){
			_timeout = 20000;
		}else{
			_timeout = 10000;
		}
		concateURL();
	}
	
	public void setConnType(int type){
		_connType = type;
		concateURL();
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 **/
	public ProtocolPackage ask(ReqPackage req) throws IOException{
		return ask(req, _timeout);
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 **/
	public ProtocolPackage ask(ReqPackage req, long timeout) throws IOException{
		Session session = new Session(req, timeout);
		synchronized(_sessions){
			//add this request into the end of the session vector and wait for the response
			_sessions.insertElementAt(session, _sessions.size());
			//notify the receive thread to handle the new session request
			_sessions.notify();
		}	
		
		//synchronized to wait for the response to the new session
		synchronized(session){
			try{
				session.wait();
			}catch(InterruptedException e){}
		}
		
		//check the req's member variable to see if receive the response
		//if not, indicates the request timeout
		if(!session.isMatchHeader()){
			throw new IOException(session.promptMsg);
		}else if(!session.isMatchLength()){
			throw new IOException("应答数据包长度不匹配，请重新提交操作");
		}else{		
			return session.response;
		}
	}
}

class Session{
	ReqPackage request = null;
	ProtocolPackage response = null;
	long timeout = 10000; 
	String promptMsg = "";	//in the case the session is not complete, prompt user with this stirng
	String detailMsg = "";	//in the case the session is not complete, save the detail err msg with this string
	
	Session(){
		response = new ProtocolPackage();
		request = new ReqPackage();
	};
	
	Session(ReqPackage _req, long _timeout){
		response = new ProtocolPackage();
		request = _req;
		timeout = _timeout;
	}

	/**
	 * Check if the response's header matches the request's header.
	 * If so, that means the response is valid.
	 */
	boolean isMatchHeader(){
		if(request.header.mode == response.header.mode &&
				request.header.seq == response.header.seq){
			return true;
		}else{
			return false;
		}	
	}
	
	/**
	 * Check the response's length field equals the real length of the body.
	 * If so, that means the response's body is valid.
	 */
	boolean isMatchLength(){
		int bodyLen = (response.header.length[0] & 0x000000FF) | ((response.header.length[1] & 0x000000FF) << 8);
		if(bodyLen == response.body.length){
			return true;
		}else{
			return false;
		}
	}
}
