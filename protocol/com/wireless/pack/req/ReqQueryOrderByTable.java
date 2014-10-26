package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqQueryOrderByTable extends RequestPackage {
	public ReqQueryOrderByTable(Staff staff, Table.AliasBuilder builder){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_ORDER_BY_TBL;
		fillBody(builder.build(), Table.TABLE_PARCELABLE_SIMPLE);
	} 

}
