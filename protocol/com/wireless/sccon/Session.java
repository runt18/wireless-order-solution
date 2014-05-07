package com.wireless.sccon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.req.RequestPackage;

public class Session{

	private final int _timeout; 
	
	private final RequestPackage _request;
	private final ProtocolPackage _response;
	
	public Session(RequestPackage request, int timeout){
		this._response = new ProtocolPackage();
		this._request = request;
		this._timeout = timeout;
	}

	public ProtocolPackage execute(String addr, int port) throws IOException{
		Socket sock = null;
		DataInputStream in = null;
		DataOutputStream out = null;
		try{
			//open the socket connection
			sock = new Socket();
			sock.connect(new InetSocketAddress(addr, port), _timeout);
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream()); 
            
			//send the request
			_request.writeToStream(out);
			
			//wait to receive the response
			_response.readFromStream(in, _timeout);
			
			//Check if the sequence no to the protocol header is the same.
			if(_request.header.seq == _response.header.seq){
				return _response;
			}else{
				throw new IOException("应答数据包的序列号不匹配");
			}
			
		}catch(IOException e){
			e.printStackTrace();
			throw new IOException("请求未成功，请检查网络信号或重新连接");
			
		}finally{
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