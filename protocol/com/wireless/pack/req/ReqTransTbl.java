package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqTransTbl extends RequestPackage {
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
	
	/**
	 * The request to transfer two tables.
	 * @param tblPairToTrans
	 * 		The 1st element means source table.<br>
	 * 		The 2nd element means destination table.
	 */
	public ReqTransTbl(Staff staff, Table[] tblPairToTrans){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.TRANS_TABLE;
		fillBody(tblPairToTrans, Table.TABLE_PARCELABLE_SIMPLE);
	} 
}
