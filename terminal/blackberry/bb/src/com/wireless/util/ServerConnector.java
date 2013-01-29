package com.wireless.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

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
        	            
        				//send the request
        				session.request.writeToStream(out);
        				
        				//wait to receive the response
        				session.response.readFromStream(in, session.timeout);
            			
        			}catch(IOException e){
        				session.detailMsg = e.toString();
        				session.promptMsg = "请求未成功，请检查网络信号或重新连接";
        				
        			}catch(Exception e){
       					session.detailMsg = e.toString();
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
		}	
		
		//synchronized to wait for the response to the new session
		synchronized(session){
			//notify the receive thread to handle the new session request
			synchronized(_sessions){
				_sessions.notify();
			}
			try{
				session.wait();
			}catch(InterruptedException e){}
		}
		
		//check the req's member variable to see if receive the response
		//if not, indicates the request timeout
		if(!session.isMatchHeader()){
			throw new IOException("应答数据包的序列号不匹配");
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
	String promptMsg;	//in the case the session is not complete, prompt user with this stirng
	String detailMsg;	//in the case the session is not complete, save the detail err msg with this string
	
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
