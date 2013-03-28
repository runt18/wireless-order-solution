package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.protocol.Table;

public class ReqCancelOrder extends ReqPackage{
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
	public ReqCancelOrder(int tableAlias){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.CANCEL_ORDER;
		fillBody(new Table(0, tableAlias, 0), Table.TABLE_PARCELABLE_SIMPLE);
	} 
	
}
