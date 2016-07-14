package com.wireless.pack.req;

import java.util.ArrayList;
import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.staffMgr.Staff;

public class ReqDailySettle extends RequestPackage{
	
	public ReqDailySettle(Staff staff){
		this(staff, null);
	}
	
	public ReqDailySettle(Staff staff, final List<Printer> orientedPrinters){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.DAILY_SETTLE;
		if(orientedPrinters != null){
			fillBody(orientedPrinters, Printer.PARCEL_PRINTER_SIMPLE);
		}else{
			fillBody(new ArrayList<Printer>(), Printer.PARCEL_PRINTER_SIMPLE);
		}
	}
	
}
