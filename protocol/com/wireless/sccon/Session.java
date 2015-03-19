package com.wireless.sccon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.wireless.exception.BusinessException;
import com.wireless.exception.IOError;
import com.wireless.exception.ProtocolError;
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

	public ProtocolPackage execute(String addr, int port) throws IOException, BusinessException{
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
				throw new BusinessException("应答数据包的序列号不匹配", ProtocolError.PACKAGE_SEQ_NO_NOT_MATCH);
			}
			
		}catch(IOException e){
			throw new IOException(IOError.IO_ERROR.getDesc());
			
		}finally{
			//close the socket connection finally
			try{
				if(in != null){
					in.close();
				}
				if(out != null){
					out.close();
				}
				if(sock != null){
					sock.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
}