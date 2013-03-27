package com.wireless.Actions.payment;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.QueryTable;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Reserved;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPackage;
import com.wireless.protocol.ReqPrintContent;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
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
			 * pin=1 & printShift=1 & onDuty=2012-4-9 8:00:00 & offDuty=2012-4-9 14:00:00
			 * 
			 * 4th example, print daily settle to today
			 * pin=1 & printDailySettle=1 
			 * 
			 * 5th example, print the history shift record
			 * pin=1 & printHistoryShift=1 & onDuty=2012-4-9 8:00:00 & offDuty=2012-4-9 14:00:00
			 * 
			 * 6th example, print the history daily settle record
			 * pin=1 & printHistoryDailySettle=1 & onDuty=2012-4-9 8:00:00 & offDuty=2012-4-9 14:00:00
			 * 
			 * pin : the pin the this terminal
			 * 
			 * orderID : the order id to print
			 * 
			 * tableID : the order associated with this table to print
			 * 
			 * printSync : 1 means print in sync, 0 means print in async
			 * 
			 * printOrder : 1 means to print the order, 0 or NULL means NOT
			 * 
			 * printDetail : 1 means to print the order detail, 0 or NULL means NOT
			 * 
			 * printReceipt : 1 means to print the receipt, 0 or NULL means NOT
			 * 
			 * printShift : 1 means to print the shift receipt, 0 or NULL means NOT
			 * 
			 * printDailySettle : 1 means to print the daily settle receipt, 0 or NULL means NOT
			 * 
			 * printTmpShift : 1 means to print the temporary daily settle receipt, 0 or NULL means NOT
			 * 
			 * printHistoryShift : 1 means to print the history shift receipt, 0 or NULL means NOT
			 * 
			 * printHistoryDailySettle : 1 means to print the history daily settle receipt, 0 or NULL means NOT
			 * 
			 * onDuty : the date time to be on duty
			 * 
			 * offDuty : the date time to be off duty
			 * 
			 */
			String pin = request.getParameter("pin");
			_pin = Long.parseLong(pin);
			
			int orderId = 0;
			if(request.getParameter("orderID") != null){
				orderId = Integer.parseInt(request.getParameter("orderID"));
				
			}else{				
				if(request.getParameter("tableID") != null){
					tableID = Integer.parseInt(request.getParameter("tableID"));
					dbCon.connect();
					Table table = QueryTable.exec(dbCon, _pin, Terminal.MODEL_STAFF, tableID);
					orderId = com.wireless.db.orderMgr.QueryOrderDao.getOrderIdByUnPaidTable(dbCon, table)[0];
				}
			}

	
			ReqPrintContent reqPrintContent = null;
			
			String param = request.getParameter("printOrder");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintSummary(orderId);
			}
			
			param = request.getParameter("printDetail");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintDetail(orderId);
			}
			
			param = request.getParameter("printReceipt");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintReceipt(orderId);
			}
			
			long onDuty = 0;
			if(request.getParameter("onDuty") != null){
				onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(request.getParameter("onDuty")).getTime();
			}
			
			long offDuty = 0;
			if(request.getParameter("offDuty") != null){
				offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(request.getParameter("offDuty")).getTime();
			}
			
			param = request.getParameter("printShift");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_SHIFT_RECEIPT);
			}
			
			param = request.getParameter("printTmpShift");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_TEMP_SHIFT_RECEIPT);
			}
			
			param = request.getParameter("printDailySettle");
			if(param != null && Byte.parseByte(param) == 1){			
				reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_DAILY_SETTLE_RECEIPT);
			}
			
			param = request.getParameter("printHistoryShift");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_HISTORY_SHIFT_RECEIPT);
			}
						
			param = request.getParameter("printHistoryDailySettle");
			if(param != null && Byte.parseByte(param) == 1){
				reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT);
			}
			
			if(reqPrintContent != null){
				ReqPackage.setGen(this);
				ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent);
				if(resp.header.type == Type.ACK){
					jsonResp = jsonResp.replace("$(result)", "true");
					if(request.getParameter("orderID") != null){
						jsonResp = jsonResp.replace("$(value)", orderId + "号账单打印成功");
						
					}else if(request.getParameter("tableID") != null){
						jsonResp = jsonResp.replace("$(value)", tableID + "号餐台的账单打印成功");
						
					}else if(request.getParameter("printShift") != null || 
							 request.getParameter("printTmpShift") != null || 
							 request.getParameter("printHistoryShift") != null){
						jsonResp = jsonResp.replace("$(value)", "交班对账单打印成功");
						
					}else if(request.getParameter("printDailySettle") != null || request.getParameter("printHistoryDailySettle") != null){
						jsonResp = jsonResp.replace("$(value)", "日结表打印成功");
						
					}else{
						jsonResp = jsonResp.replace("$(value)", orderId + "号账单打印成功");					
					}
					
				}else if(resp.header.type == Type.NAK){
					jsonResp = jsonResp.replace("$(result)", "false");
					if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
						jsonResp = jsonResp.replace("$(value)", orderId + "号账单不存在，请重新确认");					
					}else{
						jsonResp = jsonResp.replace("$(value)", orderId + "号账单打印不成功，请重新检查网络是否连通");
					}
					
				}else{
					jsonResp = jsonResp.replace("$(result)", "false");
					jsonResp = jsonResp.replace("$(value)", orderId + "号账单打印不成功，请重新检查网络是否连通");
				}
			}
		}catch(ParseException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getMessage());
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.getErrCode() == ProtocolError.TABLE_IDLE){				
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
		return _pin;
	}

	@Override
	public short getDeviceType() {
		return Terminal.MODEL_STAFF;
	}

}
