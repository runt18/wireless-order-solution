package com.wireless.protocol;

/******************************************************
* Design the ping request looks like below
* <Header>
* mode : type : seq : reserved : pin[6] : len[2]
* mode - TEST
* type - PING
* seq - auto calculated and filled in
* reserved - 0x00
* pin[6] - auto calculated and filled in
* len[2] - 0x00, 0x00
******************************************************/
public class ReqPing extends ReqOrderPackage{
	public ReqPing(){
		header.mode = Mode.TEST;
		header.type = Type.PING;
		header.reserved = 0;
		header.length[0] = 0x00;
		header.length[1] = 0x00;
	}
}
