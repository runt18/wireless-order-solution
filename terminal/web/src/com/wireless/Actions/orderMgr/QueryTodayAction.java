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
			 * 1st example, filter the order whose id equals 321 
			 * pin=0x1 & type=1 & ope=1 & value=321
			 * 2nd example, filter the order date greater than or equal 2011-7-14 14:30:00
			 * pin=0x1 & type=1 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal
			 * type : the type is one of the values below.
			 * 		  0 - 全部显示
			 *  	  1 - 按账单号
			 *  	  2 - 按台号
			 *  	  3 - 按日期
			 *  	  4 - 按类型
			 *  	  5 - 按结帐方式
			 *  	  6 - 按金额
			 *   	  7 - 按实收
			 * ope : the operator is one of the values below.
			 * 		  1 - 等于
			 * 		  2 - 大于等于
			 * 		  3 - 小于等于
			 * value : the value to search, the content is depending on the type
			 */
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
			
			//get the type to filter
			int type = Integer.parseInt(request.getParameter("type"));
			
			//get the operator to filter
			int opeType = Integer.parseInt(request.getParameter("ope"));
			String ope = null;
			if(opeType == 1){
				ope = "=";
			}else if(opeType == 2){
				ope = ">=";
			}else if(opeType == 3){
				ope = "<=";
			}else{
				ope = "=";
			}
			
			//get the value to filter
			String filterVal = request.getParameter("value");
			
			//combine the operator and filter value
			String filterCondition = null;
			
			if(type == 1){
				//按账单号
				filterCondition = " AND id" + ope + filterVal;
			}else if(type == 2){
				//按台号
				filterCondition = " AND table_id" + ope + filterVal;
			}else if(type == 3){
				//按日期
				filterCondition = " AND order_date" + ope + "'" + filterVal + "'"; 
			}else if(type == 4){
				//按类型
				filterCondition = " AND category" + ope + filterVal;
			}else if(type == 5){
				//按结帐方式
				filterCondition = " AND type" + ope + filterVal;
			}else if(type == 6){
				//按金额
				filterCondition = " AND total_price" + ope + filterVal;
			}else if(type == 7){
				//按实收
				filterCondition = " AND total_price_2" + ope + filterVal;
			}else{
				filterCondition = "";
			}
			
			/**
			 * Select all the today orders matched the conditions below.
			 * 1 - belong to this restaurant
			 * 2 - has been paid
			 * 3 - match extra filter condition
			 */
			String sql = "SELECT * FROM " + Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id + 
						 " AND total_price IS NOT NULL" + 
						 filterCondition;
			
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
				 * The json to each order looks like below
				 * ["账单号","台号","日期","类型","结帐方式","金额","实收"]
				 */
				String jsonOrder = "[\"$(order_id)\",\"$(table_id)\",\"$(order_date)\",\"$(order_cate)\",\"$(pay_manner)\",\"$(total_price)\",\"$(actual_income)\"]";
				jsonOrder = jsonOrder.replace("$(order_id)", Long.toString(dbCon.rs.getLong("id")));
				jsonOrder = jsonOrder.replace("$(table_id)", Integer.toString(dbCon.rs.getInt("table_id")));
				jsonOrder = jsonOrder.replace("$(order_date)", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("order_date")));
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
