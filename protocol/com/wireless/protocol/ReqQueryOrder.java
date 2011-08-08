package com.wireless.protocol;

public class ReqQueryOrder extends ReqPackage {
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
	public ReqQueryOrder(short tableID){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_ORDER;
		header.length[0] = 0x02;
		header.length[1] = 0x00;
		body = new byte[2];
		body[0] = (byte)(tableID & 0x00FF);
		body[1] = (byte)((tableID >> 8) & 0x00FF);
	} 

}
