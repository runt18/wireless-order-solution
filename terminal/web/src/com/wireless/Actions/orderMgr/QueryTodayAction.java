package com.wireless.Actions.orderMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class QueryTodayAction extends Action {
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
			 * e.g. pin=0x1
			 * pin : the pin the this terminal
			 * restaurantID : the today order for this restaurant to query
			 */
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
			
			/**
			 * Select all the paid orders
			 */
			String sql = "SELECT * FROM " + Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id + 
						 " AND total_price IS NOT NULL";
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			
			StringBuffer value = new StringBuffer();	
			int nCount = 0;
			while(dbCon.rs.next()){
				// the string is separated by comma
				if(nCount != 0){
					value.append("，");
				}
				/**
				 * The json to each order looks below
				 * ["账单号","台号","日期","类型","结帐方式","金额","实收"]
				 */
				String jsonOrder = "[\"$(order_id)\",\"$(table_id)\",\"$(order_date)\",\"$(order_cate)\",\"$(pay_manner)\",\"$(total_price)\",\"$(actual_income)\"]";
				jsonOrder = jsonOrder.replace("$(order_id)", Long.toString(dbCon.rs.getLong("id")));
				jsonOrder = jsonOrder.replace("$(table_id)", Integer.toString(dbCon.rs.getInt("table_id")));
				jsonOrder = jsonOrder.replace("$(order_date)", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getDate("order_date")));
				jsonOrder = jsonOrder.replace("$(order_cate)", Util.toOrderCate(dbCon.rs.getShort("category")));
				jsonOrder = jsonOrder.replace("$(pay_manner)", Util.toPayManner(dbCon.rs.getShort("type")));
				jsonOrder = jsonOrder.replace("$(total_price)", Float.toString(dbCon.rs.getFloat("total_price")));
				jsonOrder = jsonOrder.replace("$(actual_income)", Float.toString(dbCon.rs.getFloat("total_price_2")));
				// put each json order info to the value
				value.append(jsonOrder);
				nCount++;
			}
			if(nCount == 0){
				jsonResp = jsonResp.replace("$(value)", "");
			}else{
				jsonResp = jsonResp.replace("$(value)", value);
			}
			dbCon.rs.close();
			
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
			
		}finally{
			dbCon.disconnect();
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
	
	
}
