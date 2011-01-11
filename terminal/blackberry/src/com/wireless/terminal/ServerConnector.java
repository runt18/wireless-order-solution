package com.wireless.terminal;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;

import com.wireless.protocol.ProtocolHeader;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;

public class ServerConnector{
	private Vector _sessions = new Vector();
	public String _netAddr;
	public String _netPort;
	public String _netAPN;
	public String _netUser;
	public String _netPwd;
	private static ServerConnector _instance = new ServerConnector();
	

 	public ServerConnector(){
		//get the network parameters from the persistent storage
		PersistentObject store = PersistentStore.getPersistentObject(NetParam.PERSISTENT_NET_PARAM_ID);
		NetParam netParam = null;
		synchronized(store){
			netParam = (NetParam)store.getContents();
		}
		_netAddr = netParam.getParam(NetParam.NET_ADDR);
		_netPort = netParam.getParam(NetParam.NET_PORT);
		_netAPN = netParam.getParam(NetParam.NET_APN);
		_netUser = netParam.getParam(NetParam.NET_USER);
		_netPwd = netParam.getParam(NetParam.NET_PWD);
		
        new Thread(){
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
        			StreamConnection conn = null;
        			DataInputStream in = null;
        			DataOutputStream out = null;
        			try{
        				//open the socket connection
        				String url = "socket://" + _netAddr + ":" + _netPort + ";deviceside=true";
        				if(_netAPN.length() != 0){
        					url = url + ";apn=" + _netAPN;
        				}
        				if(_netUser.length() != 0){
        					url = url + ";tunnelauthusername=" + _netUser;
        				}
        				if(_netPwd.length() != 0){
        					url = url + ";tunnelauthpassword=" + _netPwd;
        				}
        				conn = (StreamConnection)Connector.open(url);
        				in = new DataInputStream(conn.openInputStream());
        				out = new DataOutputStream(conn.openOutputStream()); 
        	            
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
        					if(in != null)
        						in.close();
        					if(out != null)
        						out.close();
        					if(conn != null)
        						conn.close();        					
        				}catch(IOException e){
        					System.err.println(e.toString());  
        				}
        			}
        		}
        	}
        }.start();
	}
	
	public static ServerConnector instance(){
		return _instance;
	}
	
	public void setNetAddr(String netAddr){
		_netAddr = netAddr;
	}
	
	public void setNetPort(String netPort){
		_netPort = netPort;
	}
	
	public void setNetAPN(String netAPN){
		_netAPN = netAPN;
	}
	
	public void setNetUser(String name){
		_netUser = name;
	}
	
	public void setNetPwd(String pwd){
		_netPwd = pwd;
	}

		
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 **/
	public ProtocolPackage ask(ReqPackage req) throws IOException{		
		return ask(req, 10000);
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

/**
 * This class represents a persistable net parameter object.  
 * It contains information such as the network address, port, APN, user name and password.
 * Classes to be persisted must implement interface Persistable and can only can contain members which
 * themselves implement Persistable or are inherently persistable. 
 */ 
final class NetParam implements Persistable{
	private Vector _params;
	
	static final int NET_ADDR = 0;
	static final int NET_PORT = 1;
	static final int NET_APN = 2;
	static final int NET_USER = 3;
	static final int NET_PWD = 4;
	
    static final long PERSISTENT_NET_PARAM_ID = 0x230157d6843fDefEL;

	NetParam(){
		_params = new Vector(5);
		for(int i = 0; i < _params.capacity(); i++){
			_params.addElement(new String(""));
		}
	}
	
	String getParam(int id){
		return (String)_params.elementAt(id);
	}
	
	void setParam(int id, String value){
		_params.setElementAt(value, id);
	}
}
