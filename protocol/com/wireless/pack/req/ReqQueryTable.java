package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.staffMgr.Staff;

public class ReqQueryTable extends RequestPackage {
	public ReqQueryTable(Staff staff){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_TABLE;
	}
}
