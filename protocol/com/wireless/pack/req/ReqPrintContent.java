package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqPrintContent extends RequestPackage{
	
	public static ReqPrintContent buildReqPrintMemberReceipt(Staff staff, int memberOperationId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_MEMBER_RECEIPT);
		Parcel p = new Parcel();
		p.writeInt(memberOperationId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReqPrintShiftReceipt(Staff staff, DutyRange range, PType shiftType){
		return buildReqPrintShiftReceipt(staff, range, Region.RegionId.REGION_1, shiftType);
	}
	
	public static ReqPrintContent buildReqPrintShiftReceipt(Staff staff, DutyRange range, Region.RegionId regionId, PType shiftType){
		if(!shiftType.isShift()){
			throw new IllegalArgumentException("The shift type(val = " + shiftType + ") is invalid.");
		}
		ReqPrintContent req = new ReqPrintContent(staff, shiftType);
		Parcel p = new Parcel();
		p.writeLong(range.getOnDuty());
		p.writeLong(range.getOffDuty());
		p.writeShort(regionId.getId());
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
