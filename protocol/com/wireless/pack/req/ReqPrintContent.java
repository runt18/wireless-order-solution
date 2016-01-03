package com.wireless.pack.req;

import java.util.ArrayList;
import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class ReqPrintContent{

	private final Staff staff;
	private final PType printType;
	private final Parcel parcel = new Parcel();
	private final List<Printer> oriented = new ArrayList<Printer>();
	
	public ReqPrintContent setPrinters(final List<Printer> printers){
		this.oriented.clear();
		this.oriented.addAll(printers);
		return this;
	}
	
	public ReqPrintContent addPrinter(int printerId){
		this.oriented.add(new Printer(printerId));
		return this;
	}
	
	public RequestPackage build(){
		Parcel p = new Parcel();
		p.writeParcelList(oriented, Printer.PARCEL_PRINTER_SIMPLE);
		for(byte data : parcel.marshall()){
			p.writeByte(data);
		}
		
		RequestPackage request = new RequestPackage(staff);
		request.header.mode = Mode.PRINT;
		request.header.type = Type.PRINT_CONTENT;
		request.header.reserved = (byte)printType.getVal();
		request.body = p.marshall();
		
		return request;
	}
	
	public static ReqPrintContent build2ndDisplay(Staff staff, float display){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_2ND_DISPLAY);
		req.parcel.writeFloat(display);
		return req;
	}
	
	public static ReqPrintContent buildWxOrder(Staff staff, int wxOrderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_WX_ORDER);
		req.parcel.writeInt(wxOrderId);
		return req;
	}
	
	public static ReqPrintContent buildWxReceipt(Staff staff, Order.PayBuilder payBuilder, String codeUrl){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_WX_RECEIT);
		req.parcel.writeParcel(payBuilder, 0);
		req.parcel.writeString(codeUrl);
		for(Printer printer : payBuilder.getPrinters()){
			req.oriented.add(printer);
		}
		return req;
	}
	
	public static ReqPrintContent buildBook(Staff staff, int bookId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_BOOK);
		req.parcel.writeInt(bookId);
		return req;
	}
	
	public static ReqPrintContent buildMemberReceipt(Staff staff, int memberOperationId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_MEMBER_RECEIPT);
		req.parcel.writeInt(memberOperationId);
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
		req.parcel.writeLong(range.getOnDuty());
		req.parcel.writeLong(range.getOffDuty());
		req.parcel.writeShort(regionId.getId());
		return req;
	}
	
	public static ReqPrintContent buildTransTbl(Staff staff, int orderId, Table.Builder srcTbl, Table.Builder destTbl){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_TRANSFER_TABLE);
		req.parcel.writeInt(orderId);
		req.parcel.writeParcel(srcTbl.build(), Table.TABLE_PARCELABLE_SIMPLE);
		req.parcel.writeParcel(destTbl.build(), Table.TABLE_PARCELABLE_SIMPLE);
		return req;
	}
	
	public static ReqPrintContent buildReceipt(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_RECEIPT);
		req.parcel.writeInt(orderId);
		return req;
	}
	
	public static ReqPrintContent buildDetail(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_DETAIL);
		req.parcel.writeInt(orderId);
		return req;
	}
	
	public static ReqPrintContent buildSummary(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER);
		req.parcel.writeInt(orderId);
		return req;
	}
	
	public static ReqPrintContent buildSummaryPatch(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_PATCH);
		req.parcel.writeInt(orderId);
		return req;
	}
	
	public static ReqPrintContent buildDetailPatch(Staff staff, int orderId){
		ReqPrintContent req = new ReqPrintContent(staff, PType.PRINT_ORDER_DETAIL_PATCH);
		req.parcel.writeInt(orderId);
		return req;
	}
	
	private ReqPrintContent(Staff staff, PType printType){
		this.staff = staff;
		this.printType = printType;
	}
	
}
