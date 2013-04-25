package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;

public class ReqOTAUpdate extends RequestPackage{

	/******************************************************
	* Design the OTA update request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - OTA
	* type - GET_HOST
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x00, 0x00
	*******************************************************/
	public ReqOTAUpdate(PinGen gen){
		super(gen);
		header.mode = Mode.OTA;
		header.type = Type.GET_HOST;
	}
}
