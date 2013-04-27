package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.regionMgr.Table;

public class ReqQueryOrderByTable extends RequestPackage {
	/******************************************************
	* Design the query order request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_ORDER
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Table>
	* table[2]
	* table[2] - 2-byte indicating the table id
	*******************************************************/
	public ReqQueryOrderByTable(PinGen gen, int tableAlias){
		super(gen);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_ORDER_BY_TBL;
		fillBody(new Table(tableAlias), Table.TABLE_PARCELABLE_SIMPLE);
	} 

}
