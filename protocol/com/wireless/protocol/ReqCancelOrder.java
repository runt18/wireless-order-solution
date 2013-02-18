package com.wireless.protocol;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;

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
	public ReqCancelOrder(int tableID){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.CANCEL_ORDER;
		header.length[0] = 0x02;
		header.length[1] = 0x00;
		body = new byte[2];
		body[0] = (byte)(tableID & 0x00FF);
		body[1] = (byte)((tableID >> 8) & 0x00FF);
	} 
	
	/******************************************************
	 * In the case cancel order successfully
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] - 0x00, 0x00
	 *******************************************************/
	
	/******************************************************
	 * In the case cancel order not successfully
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - NAK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] - 0x00, 0x00
	 *******************************************************/
}
