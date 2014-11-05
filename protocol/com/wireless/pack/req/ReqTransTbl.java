package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqTransTbl extends RequestPackage {
	
	public ReqTransTbl(Staff staff, Table.TransferBuilder builder){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.TRANS_TABLE;
		fillBody(builder, 0);
	} 
}
