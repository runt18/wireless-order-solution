package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqPrintContent extends RequestPackage{
	
	public static ReqPrintContent buildWxReceipt(Staff staff, Order.PayBuilder payBuilder){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_WX_RECEIT);
		Parcel p = new Parcel();
		p.writeParcel(payBuilder, 0);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildMemberReceipt(Staff staff, int memberOperationId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_MEMBER_RECEIPT);
		Parcel p = new Parcel();
		p.writeInt(memberOperationId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildShiftReceipt(Staff staff, DutyRange range, PType shiftType){
		return buildShiftReceipt(staff, range, Region.RegionId.REGION_NULL, shiftType);
	}
	
	public static ReqPrintContent buildShiftReceipt(Staff staff, DutyRange range, Region.RegionId regionId, PType shiftType){
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
	
	public static ReqPrintContent buildTransFood(Staff staff, Order.TransferBuilder transferBuilder){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_TRANSFER_FOOD);
		Parcel p = new Parcel();
		p.writeParcel(transferBuilder, 0);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildTransTbl(Staff staff, int orderId, Table.Builder srcTbl, Table.Builder destTbl){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_TRANSFER_TABLE);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		p.writeParcel(srcTbl.build(), Table.TABLE_PARCELABLE_SIMPLE);
		p.writeParcel(destTbl.build(), Table.TABLE_PARCELABLE_SIMPLE);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildReceipt(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_RECEIPT);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildDetail(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_DETAIL);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildSummary(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildSummaryPatch(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_PATCH);
		Parcel p = new Parcel();
		p.writeInt(orderId);
		req.body = p.marshall();
		return req;
	}
	
	public static ReqPrintContent buildDetailPatch(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_DETAIL_PATCH);
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
