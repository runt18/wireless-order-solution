package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqCancelOrder extends RequestPackage{
	/******************************************************
	* Design the cancel order request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - CANCEL_ORDER
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - length of the <Body>
	* <Table>
	* table[2]
	* table[2] - 2-byte indicates the table id 
	*******************************************************/
	public ReqCancelOrder(Staff staff, Table.Builder builder){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.CANCEL_ORDER;
		fillBody(builder.build(), Table.TABLE_PARCELABLE_SIMPLE);
	} 
	
}
