package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;

public class ReqQueryBackup extends RequestPackage{
	public ReqQueryBackup(){
		super(null);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_BACKUP_SERVER;
	}
}
