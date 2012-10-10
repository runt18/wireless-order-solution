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
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
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
					
			QueryShiftDao.Result result = QueryShiftDao.execByNow(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			/**
			 * The json to shift record like below
			 * ["开始日期","结帐日期","账单数","现金金额","现金实收","刷卡金额",
			 * "刷卡实收","会员卡金额","会员卡实收","签单金额","签单实收",
			 * "挂账金额","挂账实收","实收金额","折扣金额","赠送金额"]
			 */
			
			String jsonOrder = "[\"$(on_duty)\",\"$(off_duty)\",\"$(orderAmount)\",\"$(cash)\",\"$(cash_2)\",\"$(credit_card)\",\"$(credit_card_2)\"," +
								"\"$(member)\",\"$(member_2)\",\"$(sign)\",\"$(sign_2)\",\"$(hang)\",\"$(hang_2)\"," +
								"\"$(totalActual)\",\"$(totalDiscount)\",\"$(totalGifted)\"]";
			jsonOrder = jsonOrder.replace("$(on_duty)", result.onDuty);
			jsonOrder = jsonOrder.replace("$(off_duty)", result.offDuty);
			jsonOrder = jsonOrder.replace("$(orderAmount)", Integer.toString(result.orderAmount));
			jsonOrder = jsonOrder.replace("$(cash)", new Float(result.cashIncome).toString());
			jsonOrder = jsonOrder.replace("$(cash_2)", new Float(result.cashIncome2).toString());
			jsonOrder = jsonOrder.replace("$(credit_card)", new Float(result.creditCardIncome).toString());
			jsonOrder = jsonOrder.replace("$(credit_card_2)", new Float(result.creditCardIncome2).toString());
			jsonOrder = jsonOrder.replace("$(member)", new Float(result.memberCardIncome).toString());
			jsonOrder = jsonOrder.replace("$(member_2)", new Float(result.memberCardIncome2).toString());
			jsonOrder = jsonOrder.replace("$(sign)", new Float(result.signIncome).toString());
			jsonOrder = jsonOrder.replace("$(sign_2)", new Float(result.signIncome2).toString());
			jsonOrder = jsonOrder.replace("$(hang)", new Float(result.hangIncome).toString());
			jsonOrder = jsonOrder.replace("$(hang_2)", new Float(result.hangIncome2).toString());
			jsonOrder = jsonOrder.replace("$(totalActual)", new Float(result.totalActual).toString());
			jsonOrder = jsonOrder.replace("$(totalDiscount)", new Float(result.discountIncome).toString());
			jsonOrder = jsonOrder.replace("$(totalGifted)", new Float(result.giftIncome).toString());
			
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
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
}
