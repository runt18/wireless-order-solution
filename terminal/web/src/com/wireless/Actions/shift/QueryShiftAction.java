package com.wireless.Actions.shift;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.QueryShift;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Shift;
import com.wireless.protocol.Terminal;

public class QueryShiftAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x01
			 */
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			
			Shift shift = QueryShift.exec(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
			
			/**
			 * The json to shift record like below
			 * ["开始日期","结帐日期","账单数","现金金额","现金实收","刷卡金额",
			 * "刷卡实收","会员卡金额","会员卡实收","签单金额","签单实收",
			 * "挂账金额","挂账实收","实收金额","折扣金额","赠送金额"]
			 */
			
			String jsonOrder = "[\"$(on_duty)\",\"$(off_duty)\",\"$(orderAmount)\",\"$(cash)\",\"$(cash_2)\",\"$(credit_card)\",\"$(credit_card_2)\"," +
								"\"$(member)\",\"$(member_2)\",\"$(sign)\",\"$(sign_2)\",\"$(hang)\",\"$(hang_2)\"," +
								"\"$(totalActual)\",\"$(totalDiscount)\",\"$(totalGifted)\"]";
			jsonOrder = jsonOrder.replace("$(on_duty)", shift.onDuty);
			jsonOrder = jsonOrder.replace("$(off_duty)", shift.offDuty);
			jsonOrder = jsonOrder.replace("$(orderAmount)", Integer.toString(shift.orderAmount));
			jsonOrder = jsonOrder.replace("$(cash)", shift.totalCash.toString());
			jsonOrder = jsonOrder.replace("$(cash_2)", shift.totalCash2.toString());
			jsonOrder = jsonOrder.replace("$(credit_card)", shift.totalCreditCard.toString());
			jsonOrder = jsonOrder.replace("$(credit_card_2)", shift.totalCreditCard2.toString());
			jsonOrder = jsonOrder.replace("$(member)", shift.totalMemberCard.toString());
			jsonOrder = jsonOrder.replace("$(member_2)", shift.totalMemberCard2.toString());
			jsonOrder = jsonOrder.replace("$(sign)", shift.totalSign.toString());
			jsonOrder = jsonOrder.replace("$(sign_2)", shift.totalSign2.toString());
			jsonOrder = jsonOrder.replace("$(hang)", shift.totalHang.toString());
			jsonOrder = jsonOrder.replace("$(hang_2)", shift.totalHang2.toString());
			jsonOrder = jsonOrder.replace("$(totalActual)", shift.totalActual.toString());
			jsonOrder = jsonOrder.replace("$(totalDiscount)", shift.totalDiscount.toString());
			jsonOrder = jsonOrder.replace("$(totalGifted)", shift.totalGift.toString());
			
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", jsonOrder);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");		
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");	
				
			}else if(e.errCode == ErrorCode.TERMINAL_EXPIRED){
				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");	
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "没有获取到当日账单信息，请重新确认");	
			}
		
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			dbCon.disconnect();
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
}
