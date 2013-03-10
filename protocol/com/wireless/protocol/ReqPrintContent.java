package com.wireless.protocol;

import com.wireless.pack.Mode;
import com.wireless.pack.Reserved;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;
import com.wireless.protocol.parcel.Parcel;

/******************************************************
 * Design the print order 2 request looks like below
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2] : print_content
 * mode - PRINT
 * type - PRINT_BILL_2
 * seq - auto calculated and filled in
 * reserved - 0x00	
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * print_type[4] : order_id[4] : ori_tbl[2] : new_tbl[2] : on_duty[8] : off_duty[8]
 * print_type[4] - 4-byte indicates the print type
 * order_id[4] - 4-byte indicating the order id to print
 * ori_tbl[2] - 2-byte indicating the original table id
 * new_tbl[2] - 2-byte indicating the new table id
 * on_duty[8] - 8-byte indicating the on duty
 * off_duty[8] - 8-byte indicating the off duty
 *******************************************************/
public class ReqPrintContent extends ReqPackage{
	
	public static ReqPrintContent buildReqPrintShiftReceipt(long onDuty, long offDuty, byte typeToShift){
		ReqPrintContent req = new ReqPrintContent(typeToShift);
		Parcel p = new Parcel();
		p.writeLong(onDuty);
		p.writeLong(offDuty);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintTransTbl(int orderId, Table srcTbl, Table destTbl){
		ReqPrintContent req = new ReqPrintContent(Reserved.PRINT_TRANSFER_TABLE);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		p.writeParcel(srcTbl, Table.TABLE_PARCELABLE_SIMPLE);
		p.writeParcel(destTbl, Table.TABLE_PARCELABLE_SIMPLE);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintReceipt(int orderId){
		ReqPrintContent req = new ReqPrintContent(Reserved.PRINT_RECEIPT);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintDetail(int orderId){
		ReqPrintContent req = new ReqPrintContent(Reserved.PRINT_ORDER_DETAIL);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintSummary(int orderId){
		ReqPrintContent req = new ReqPrintContent(Reserved.PRINT_ORDER);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	private ReqPrintContent(byte printCategory){
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_CONTENT;
		header.reserved = printCategory;
	}
	
//	/**
//	 * 
//	 * @author Ying.Zhang
//	 *
//	 */
//	public static class ReqParam{
//		public int printConf = Reserved.DEFAULT_CONF;		//the print configuration parameter  
//		public int orderID;									//the order id to print
//		public int srcTblID;								//the id to original table 
//		public int destTblID;								//the id to new table 
//		public long onDuty;									//the on duty 
//		public long offDuty;								//the off duty
//	}
//	
//	/**
//	 * Construct the print protocol.
//	 * @param param
//	 */
//	public ReqPrintContent(ReqParam param){
//		header.mode = Mode.PRINT;
//		header.type = Type.PRINT_CONTENT;
//		
//		int bodyLen = 4 + /* print_type takes up 4 bytes */
//				  	  4 + /* order id takes up 4 bytes */
//				  	  2 + /* original table id takes up 2 bytes */
//				  	  2 + /* new table id takes up 2 bytes */
//				  	  8 + /* on duty takes up 8 bytes */
//				  	  8 ; /* off duty takes up 8 bytes */
//	
//		//assign the length of the body
//		header.length[0] = (byte)(bodyLen & 0x000000FF);
//		header.length[1] = (byte)((bodyLen >> 8) & 0x000000FF);
//		
//		body = new byte[bodyLen];
//		
//		//assign the print type
//		body[0] = (byte)(param.printConf & 0x000000FF);
//		body[1] = (byte)((param.printConf & 0x0000FF00) >> 8);
//		body[2] = (byte)((param.printConf & 0x00FF0000) >> 16);
//		body[3] = (byte)((param.printConf & 0xFF000000) >> 24);
//		
//		//assign the order id
//		body[4] = (byte)(param.orderID & 0x000000FF);
//		body[5] = (byte)((param.orderID & 0x0000FF00) >> 8);
//		body[6] = (byte)((param.orderID & 0x00FF0000) >> 16);
//		body[7] = (byte)((param.orderID & 0xFF000000) >> 24);
//		
//		//assign the original table id
//		body[8] = (byte)(param.srcTblID & 0x000000FF);
//		body[9] = (byte)((param.srcTblID & 0x0000FF00) >> 8);
//		
//		//assign the new table id
//		body[10] = (byte)(param.destTblID & 0x000000FF);
//		body[11] = (byte)((param.destTblID & 0x0000FF00) >> 8);
//		
//		//assign the on duty
//		body[12] = (byte)(param.onDuty & 0x00000000000000FFL);
//		body[13] = (byte)((param.onDuty & 0x000000000000FF00L) >> 8);
//		body[14] = (byte)((param.onDuty & 0x0000000000FF0000L) >> 16);
//		body[15] = (byte)((param.onDuty & 0x00000000FF000000L) >> 24);
//		body[16] = (byte)((param.onDuty & 0x000000FF00000000L) >> 32);
//		body[17] = (byte)((param.onDuty & 0x0000FF0000000000L) >> 40);
//		body[18] = (byte)((param.onDuty & 0x00FF000000000000L) >> 48);
//		body[19] = (byte)((param.onDuty & 0xFF00000000000000L) >> 56);
//
//		//assign the off duty
//		body[20] = (byte)(param.offDuty & 0x00000000000000FFL);
//		body[21] = (byte)((param.offDuty & 0x000000000000FF00L) >> 8);
//		body[22] = (byte)((param.offDuty & 0x0000000000FF0000L) >> 16);
//		body[23] = (byte)((param.offDuty & 0x00000000FF000000L) >> 24);
//		body[24] = (byte)((param.offDuty & 0x000000FF00000000L) >> 32);
//		body[25] = (byte)((param.offDuty & 0x0000FF0000000000L) >> 40);
//		body[26] = (byte)((param.offDuty & 0x00FF000000000000L) >> 48);
//		body[27] = (byte)((param.offDuty & 0xFF00000000000000L) >> 56);
//	}
}
