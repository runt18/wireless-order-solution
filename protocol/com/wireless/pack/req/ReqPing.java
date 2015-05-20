package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;

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
public class ReqPing extends RequestPackage{
	public ReqPing(){
		super(null);
		header.mode = Mode.DIAGNOSIS;
		header.type = Type.PING;
	}
}
