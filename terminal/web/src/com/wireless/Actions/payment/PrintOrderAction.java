package com.wireless.Actions.payment;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.QueryTable;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Reserved;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPackage;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.protocol.PTable;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.DateUtil;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class PrintOrderAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		int tableID = 0;
		DBCon dbCon = new DBCon();
		try {
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
			
			final Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			int orderId = 0;
			if(request.getParameter("orderID") != null){
				orderId = Integer.parseInt(request.getParameter("orderID"));
			}else{				
				if(request.getParameter("tableID") != null){
					tableID = Integer.parseInt(request.getParameter("tableID"));
					dbCon.connect();
					PTable table = QueryTable.exec(dbCon, term.pin, Terminal.MODEL_STAFF, tableID);
					orderId = com.wireless.db.orderMgr.QueryOrderDao.getOrderIdByUnPaidTable(dbCon, table)[0];
				}
			}

			long onDuty = 0;
			if(request.getParameter("onDuty") != null){
				onDuty = DateUtil.parseDate(request.getParameter("onDuty"));
			}
			long offDuty = 0;
			if(request.getParameter("offDuty") != null){
				offDuty = DateUtil.parseDate(request.getParameter("offDuty"));
			}
			
			ReqPrintContent reqPrintContent = null;
			String pt = request.getParameter("printType");
			int printType = Integer.valueOf(pt);
			switch(printType){
				case 1:
					reqPrintContent = ReqPrintContent.buildReqPrintSummary(orderId);
					break;
				case 2:
					reqPrintContent = ReqPrintContent.buildReqPrintDetail(orderId);
					break;
				case 3:
					reqPrintContent = ReqPrintContent.buildReqPrintReceipt(orderId);
					break;
				case 4:
					reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_SHIFT_RECEIPT);
					break;
				case 5:
					reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_TEMP_SHIFT_RECEIPT);
					break;
				case 6:
					reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_DAILY_SETTLE_RECEIPT);
					break;
				case 7:
					reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_HISTORY_SHIFT_RECEIPT);
					break;
				case 8:
					reqPrintContent = ReqPrintContent.buildReqPrintShiftReceipt(onDuty, offDuty, Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT);
					break;
				default:
					reqPrintContent = null;
					break;
			}
			
			if(reqPrintContent != null){
//				ReqPackage.setGen(this);
				ReqPackage.setGen(new PinGen(){
					@Override
					public long getDeviceId() {
						return term.pin;
					}
					@Override
					public short getDeviceType() {
						return term.modelID;
					}
					
				});
				ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent);
				if(resp.header.type == Type.ACK){
					jobject.setSuccess(true);
					switch(printType){
						case 1:
							jobject.initTip("操作成功, " + orderId + " 号账单总单打印成功.");
							break;
						case 2:
							jobject.initTip("操作成功, " + orderId + " 号账单明细单打印成功.");
							break;
						case 3:
							jobject.initTip("操作成功, " + orderId + " 号结账单打印成功.");
							break;
						case 4:
						case 5:
						case 7:
							jobject.initTip("操作成功, 交班对账信息打印成功.");
							break;
						case 6:
						case 8:
							jobject.initTip("操作成功, 日结信息打印成功.");
							break;
						default:
							jobject.initTip("操作成功, 打印成功.");
							break;
					}
				}else if(resp.header.type == Type.NAK){
					if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
						jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9999, "操作失败, " + orderId + "账单不存在, 请重新确认.");
					}else{
						jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9999, "操作失败, " + orderId + "号账单打印不成功, 请重新检查网络是否连通.");
					}
				}else{
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9999, "操作失败, 服务器请求不成功, 请重新检查网络是否连通.");
				}
			}
		}catch(NumberFormatException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 获取账单编号或餐台编号或打印类型信息失败.");
		}catch(BusinessException e){
			e.printStackTrace();
			if(e.getErrCode() == ProtocolError.TABLE_IDLE){		
				jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), "操作失败, " + tableID + "号餐台是空闲状态, 不存在此张餐台的账单信息, 请重新确认.");
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), "操作失败, 打印" + tableID + "号餐台的账单不成功.");
			}
		}catch(IOException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 服务器请求不成功, 请重新检查网络是否连通.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 未知错误, 请联系客服人员.");
		}finally{
			dbCon.disconnect();
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}

}
