package com.wireless.protocol;
 
public class ProtocolPackage {
	public ProtocolHeader header = null;			//the header of the package
	public byte[] body = null;						//the body of the package
	public final static byte[] EOP = {'\r', '\n'};	//the flag indicating the end of the package
	
	public ProtocolPackage(){
		header = new ProtocolHeader();
		body = new byte[0];
	}
	
	public ProtocolPackage(ProtocolHeader _header, byte[] _body){
		header = _header;
		body = _body;
	}
}
