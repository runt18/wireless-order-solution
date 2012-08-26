package com.wireless.protocol;

public class ReqTransTbl extends ReqPackage {
	/******************************************************
	* Design the table transfer request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - TRANS_TABLE
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Table>
	* srcTbl[2] : destTbl[2]
	* srcTbl[2] - 2-byte indicating table alias to source table
	* destTbl[2] - 2-byte indicating table alias to destination table
	*******************************************************/
	public ReqTransTbl(Table srcTbl, Table destTbl){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.TRANS_TABLE;
		header.length[0] = 0x02;
		header.length[1] = 0x00;
		body = new byte[4];
		body[0] = (byte)(srcTbl.aliasID & 0x00FF);
		body[1] = (byte)((srcTbl.aliasID >> 8) & 0x00FF);
		body[2] = (byte)(destTbl.aliasID & 0x00FF);
		body[3] = (byte)((destTbl.aliasID >> 8) & 0x00FF);
	} 
}
