package com.wireless.Actions.payment;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.QueryTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqPrintOrder2;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class PrintOrderAction extends Action implements PinGen{

	private long _pin = 0;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		int tableID = 0;
		DBCon dbCon = new DBCon();
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			
			/**
			 * The parameters looks like below.
			 * 
			 * 1st example, print an order according to order id
			 * pin=1 & orderID=654 & printSync=1 & printOrder=1 & printDetail=0 & printReceipt=0 & printShift=0
			 * 
			 * 2nd example, print an order according to table id
			 * pin=11 & tableID=101 & printSync=1 & printOrder=1 & printDetail=0 & printReceipt=0 & printShift=0
			 * 
			 * 3rd example, print the shift record
			 * pin=1 & printShift=1 & onDuty=253654123 & offDuty=253169844
			 * 
			 * pin : the pin the this terminal
			 * 
			 * orderID : the order id to print
			 * 
			 * tableID : the order associated with this table to print
			 * 
			 * printSync : 1 means print in sync, 0 means print in async
			 * 
			 * printOrder : 1 means to print the order, 0 or null means NOT
			 * 
			 * printDetail : 1 means to print the order detail, 0 or null means NOT
			 * 
			 * printReceipt : 1 means to print the receipt, 0 or null means NOT
			 * 
			 * printShift : 1 means to print the shift, 0 or null means NOT
			 * 
			 * onDuty : the date time to be on duty which represented by milliseconds 
			 * 
			 * offDuty : the date time to be off duty which represented by milliseconds 
			 * 
			 */
			String pin = request.getParameter("pin");
			_pin = Long.parseLong(pin);
			
			int orderID = 0;
			if(request.getParameter("orderID") != null){
				orderID = Integer.parseInt(request.getParameter("orderID"));
				
			}else{				
				if(request.getParameter("tableID") != null){
					tableID = Integer.parseInt(request.getParameter("tableID"));
					dbCon.connect();
					Table table = QueryTable.exec(dbCon, _pin, Terminal.MODEL_STAFF, tableID);
					orderID = com.wireless.db.Util.getUnPaidOrderID(dbCon, table);
				}
			}
			
			
			short conf = 0;
			
			String param = request.getParameter("printSync");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_SYNC;
				}else{
					conf |= Reserved.PRINT_SYNC;
				}
			}else{
				conf &= ~Reserved.PRINT_SYNC;
			}
			
			param = request.getParameter("printOrder");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_ORDER_2;
				}else{
					conf |= Reserved.PRINT_ORDER_2;
				}
			}else{
				conf &= ~Reserved.PRINT_ORDER_2;
			}
			
			param = request.getParameter("printDetail");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_ORDER_DETAIL_2;
				}else{
					conf |= Reserved.PRINT_ORDER_DETAIL_2;
				}
			}else{
				conf &= ~Reserved.PRINT_ORDER_DETAIL_2;
			}
			
			param = request.getParameter("printReceipt");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_RECEIPT_2;
				}else{
					conf |= Reserved.PRINT_RECEIPT_2;
				}
			}else{
				conf &= ~Reserved.PRINT_RECEIPT_2;
			}
			
			param = request.getParameter("printShift");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_SHIFT_RECEIPT_2;
				}else{
					conf |= Reserved.PRINT_SHIFT_RECEIPT_2;
				}
			}else{
				conf &= ~Reserved.PRINT_SHIFT_RECEIPT_2;
			}
			
			param = request.getParameter("printTmpShift");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_TEMP_SHIFT_RECEIPT_2;
				}else{
					conf |= Reserved.PRINT_TEMP_SHIFT_RECEIPT_2;
				}
			}else{
				conf &= ~Reserved.PRINT_TEMP_SHIFT_RECEIPT_2;
			}
			
			long onDuty = 0;
			if(request.getParameter("onDuty") != null){
				onDuty = Long.parseLong(request.getParameter("onDuty"));				
			}
			
			long offDuty = 0;
			if(request.getParameter("offDuty") != null){
				offDuty = Long.parseLong(request.getParameter("offDuty"));				
			}
						
			ReqPackage.setGen(this);
			ReqPrintOrder2.ReqParam reqParam = new ReqPrintOrder2.ReqParam();
			reqParam.printConf = conf;
			reqParam.orderID = orderID;
			reqParam.onDuty = onDuty;
			reqParam.offDuty = offDuty;
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqPrintOrder2(reqParam));
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				if(request.getParameter("orderID") != null){
					jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印成功");
					
				}else if(request.getParameter("tableID") != null){
					jsonResp = jsonResp.replace("$(value)", tableID + "号餐台的账单打印成功");
					
				}else if(request.getParameter("printShift") != null || request.getParameter("printTmpShift") != null){
					jsonResp = jsonResp.replace("$(value)", "交班对账单打印成功");
					
				}else{
					jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印成功");					
				}
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
					jsonResp = jsonResp.replace("$(value)", orderID + "号账单不存在，请重新确认");					
				}else{
					jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印不成功，请重新检查网络是否连通");
				}
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印不成功，请重新检查网络是否连通");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.errCode == ErrorCode.TABLE_IDLE){				
				jsonResp = jsonResp.replace("$(value)", tableID + "号餐台是空闲状态，不存在此张餐台的账单信息，请重新确认");
			}else{
				jsonResp = jsonResp.replace("$(value)", "打印" + tableID + "号餐台的账单不成功");
			}
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "服务器请求不成功，请重新检查网络是否连通");
			
		}finally{
			dbCon.disconnect();
			//just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}
	
	@Override
	public long getDeviceId() {
		// TODO Auto-generated method stub
		return _pin;
	}

	@Override
	public short getDeviceType() {
		// TODO Auto-generated method stub
		return Terminal.MODEL_STAFF;
	}

}
