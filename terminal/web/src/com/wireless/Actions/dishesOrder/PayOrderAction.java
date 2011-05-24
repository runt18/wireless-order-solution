package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqPayOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class PayOrderAction extends Action implements PinGen{
	
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

			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			_pin = Integer.parseInt(pin, 16);
			
			Order orderToPay = new Order();
			orderToPay.table_id = Short.parseShort(request.getParameter("tableID"));
			orderToPay.setActualPrice(Float.parseFloat(request.getParameter("actualPrice")));
			orderToPay.pay_type = Integer.parseInt(request.getParameter("payType"));
			orderToPay.discount_type = Integer.parseInt(request.getParameter("discountType"));
			orderToPay.member_id = request.getParameter("memberID");
			
			ReqPackage.setGen(this);
			byte printType = Reserved.PRINT_RECEIPT_2;
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqPayOrder(orderToPay, printType));
			
			if(resp.header.type == Type.ACK){
				jsonResp = jsonResp.replace("$(result)", "true");
				jsonResp = jsonResp.replace("$(value)", orderToPay.table_id + "号餐台结帐成功");
				
			}else if(resp.header.type == Type.NAK){
				jsonResp = jsonResp.replace("$(result)", "false");
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
					jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){					
					jsonResp = jsonResp.replace("$(value)", orderToPay.table_id + "号餐台信息不存在，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.TABLE_IDLE){
					jsonResp = jsonResp.replace("$(value)", orderToPay.table_id + "号餐台是空闲状态，可能已结帐，请重新确认");
					
				}else if(resp.header.reserved == ErrorCode.PRINT_FAIL){
					jsonResp = jsonResp.replace("$(value)", orderToPay.table_id + "号餐台结帐成功，但未能成功打印，请立刻补打结帐单并与相关人员确认");
					
				}else{
					jsonResp = jsonResp.replace("$(value)", orderToPay.table_id + "号餐台结帐失败，请重新确认");
				}
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderToPay.table_id + "号餐台结帐不成功，请重新确认");
			}
			
		}catch(IOException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "服务器请求不成功，请重新检查网络是否连通");
			
		}catch(NumberFormatException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "实收金额格式不正确，请检查后重新提交");
			
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
