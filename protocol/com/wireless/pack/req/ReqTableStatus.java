package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqTableStatus extends RequestPackage {
	/******************************************************
	* Design the query table status request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_TABLE_STATUS
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Table>
	* table[2]
	* table[2] - 2-byte indicating the table id
	*******************************************************/
	public ReqTableStatus(Staff staff, Table tblToQuery){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_TABLE_STATUS;
		fillBody(tblToQuery, Table.TABLE_PARCELABLE_SIMPLE);
	} 
}
