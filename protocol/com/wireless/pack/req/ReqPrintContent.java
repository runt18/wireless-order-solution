package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

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
public class ReqPrintContent extends RequestPackage{
	
	public static ReqPrintContent buildReqPrintMemberReceipt(Staff staff, int memberOperationId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_MEMBER_RECEIPT);
		Parcel p = new Parcel();
		p.writeInt(memberOperationId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintShiftReceipt(Staff staff, long onDuty, long offDuty, PType shiftType){
		if(shiftType != PType.PRINT_SHIFT_RECEIPT &&
		   shiftType != PType.PRINT_TEMP_SHIFT_RECEIPT &&
		   shiftType != PType.PRINT_DAILY_SETTLE_RECEIPT &&
		   shiftType != PType.PRINT_HISTORY_SHIFT_RECEIPT &&
		   shiftType != PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
			
			throw new IllegalArgumentException("The shift type(val = " + shiftType + ") is invalid.");
		}
		ReqPrintContent req = new ReqPrintContent(staff, shiftType);
		Parcel p = new Parcel();
		p.writeLong(onDuty);
		p.writeLong(offDuty);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintTransTbl(Staff staff, int orderId, Table srcTbl, Table destTbl){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_TRANSFER_TABLE);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		p.writeParcel(srcTbl, Table.TABLE_PARCELABLE_SIMPLE);
		p.writeParcel(destTbl, Table.TABLE_PARCELABLE_SIMPLE);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintReceipt(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_RECEIPT);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintDetail(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_DETAIL);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintSummary(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	private ReqPrintContent(Staff staff, PType printType){
		super(staff);
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_CONTENT;
		header.reserved = (byte)printType.getVal();
	}
	
}
