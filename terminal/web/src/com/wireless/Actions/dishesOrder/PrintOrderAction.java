package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqPrintOrder2;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class PrintOrderAction extends Action implements PinGen{

	private int _pin = 0;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & orderID=654 & printSync=1 & printOrder=1 & printDetail=0 & printReceipt=0
			 * pin : the pin the this terminal
			 * orderID : the order id to print
			 * printSync : 1 means print in sync, 0 means print in async
			 * printOrder : 1 means to print the order, 0 or null means NOT
			 * printDetail : 1 means to print the order detail, 0 or null means NOT
			 * printReceipt : 1 means to print the receipt, 0 or null means NOT
			 */
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			_pin = Integer.parseInt(pin, 16);
			
			int orderID = Integer.parseInt(request.getParameter("orderID"));
			
			byte conf = 0;
			
			String param = request.getParameter("prinSync");
			if(param != null){
				if(Byte.parseByte(param) == 0){
					conf &= ~Reserved.PRINT_SYNC;
				}else{
					conf |= Reserved.PRINT_SYNC;
				}
			}else{
				conf |= Reserved.PRINT_SYNC;
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
			
			ReqPackage.setGen(this);
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqPrintOrder2(conf, orderID));
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印成功");
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印不成功，请重新检查网络是否连通");
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderID + "号账单打印不成功，请重新检查网络是否连通");
			}
			
		}catch(IOException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "服务器请求不成功，请重新检查网络是否连通");
			
		}finally{
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}
	
	@Override
	public int getDeviceId() {
		// TODO Auto-generated method stub
		return _pin;
	}

	@Override
	public short getDeviceType() {
		// TODO Auto-generated method stub
		return Terminal.MODEL_STAFF;
	}

}
