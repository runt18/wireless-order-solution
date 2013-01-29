package com.wireless.sccon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;

public class ServerConnector{
	private LinkedList<Session> _sessions = new LinkedList<Session>();
	private String _netAddr = null;
	private int _netPort = 0;
	private int _timeout = 10000;
	
	private static ServerConnector _instance = new ServerConnector();
	
 	public ServerConnector(){
		
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
        				session = (Session)_sessions.get(0);
        				_sessions.remove(0);
        			}
        			Socket conn = null;
        			DataInputStream in = null;
        			DataOutputStream out = null;
        			try{
        				//open the socket connection
        				conn = new Socket();
        				conn.connect(new InetSocketAddress(_netAddr, _netPort), _timeout);
        				in = new DataInputStream(conn.getInputStream());
        				out = new DataOutputStream(conn.getOutputStream()); 
        	            
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
        }.start();
	}
	
	public static ServerConnector instance(){
		return _instance;
	}
	
	public void setNetAddr(String netAddr){
		_netAddr = netAddr;
	}
	
	public void setNetPort(int netPort){
		_netPort = netPort;
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
			_sessions.add(session);
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
		if(!session.isMatchSeq()){
			throw new IOException("应答数据包的序列号不匹配");
			
		}else if(!session.isMatchLength()){
			throw new IOException("应答数据包长度不匹配，请重新提交操作");
			
		}else{		
			return session.response;
		}
	}
}
