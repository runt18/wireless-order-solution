package com.wireless.protocol;

/******************************************************
 * In the case printer login successfully, 
 * design the response looks like below
 * mode : type : seq : reserved : pin[6] : len[2] : template1 : template2...
 * <Header>
 * mode - PRINT
 * type - ACK
 * seq - same as request
 * reserved - 0x00
 * pin[6] - same as request
 * len[2] -  length of the <Body>
 * <Body>
 * func[4] : len[2] : template
 * func[4] - 4-byte indicates the function code
 * len[2] - 2-byte indicates the length of the template content
 * template - the template content whose length equals to len[2]
 *******************************************************/
public class RespPrintLogin extends RespPackage{
	public RespPrintLogin(ProtocolHeader reqHeader, byte[] respBody){
		super(reqHeader);
		header.mode = Mode.PRINT;
		header.type = Type.ACK;
		header.length[0] = (byte)(body.length & 0x000000FF);
		header.length[1] = (byte)((body.length & 0x0000FF00) >> 8);
		body = respBody;
	}
}
