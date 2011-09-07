package com.wireless.Actions.shift;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
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
			
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
			
			/**
			 * Get the latest off duty date and make it as the on duty date to this duty shift
			 */
			String onDuty;
			String sql = "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id +
						 " ORDER BY off_duty desc LIMIT 1";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("off_duty"));
			}else{
				onDuty = "2011-07-30 00:00:00";
			}
			dbCon.rs.close();
			
			/**
			 * Make the current date as the off duty date
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			String offDuty = sdf.format(System.currentTimeMillis());
			
			/**
			 * Get the amount the order within this shift
			 */
			int orderAmount = 0;
			sql = "SELECT COUNT(*) FROM " + Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id +
				  " AND total_price IS NOT NULL" +
				  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				orderAmount = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			
			String prefix = "SELECT SUM(total_price), SUM(total_price_2) FROM " + Params.dbName + 
							".order WHERE restaurant_id=" + term.restaurant_id +
							" AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
							" AND type=";
			/**
			 * Get the total cash income within this shirt
			 */
			float totalCash = 0;
			float totalCash_2 = 0;
			sql = prefix + Order.MANNER_CASH;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalCash = dbCon.rs.getFloat(1);
				totalCash_2 = dbCon.rs.getFloat(2);
			}
			dbCon.rs.close();
			
			/**
			 * Get the total credit card income within this shift
			 */
			float totalCreditCard = 0;
			float totalCreditCard_2 = 0;
			sql = prefix + Order.MANNER_CREDIT_CARD;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalCreditCard = dbCon.rs.getFloat(1);
				totalCreditCard_2 = dbCon.rs.getFloat(2);
			}
			dbCon.rs.close();
			
			/**
			 * Get the total member card income within this shift
			 */
			float totalMemberCard = 0;
			float totalMemberCard_2 = 0;
			sql = prefix + Order.MANNER_MEMBER;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalMemberCard = dbCon.rs.getFloat(1);
				totalMemberCard_2 = dbCon.rs.getFloat(2);
			}
			dbCon.rs.close();
			
			/**
			 * Get the total sign income within this shift
			 */
			float totalSign = 0;
			float totalSign_2 = 0;
			sql = prefix + Order.MANNER_SIGN;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalSign = dbCon.rs.getFloat(1);
				totalSign_2 = dbCon.rs.getFloat(2);
			}
			dbCon.rs.close();
			
			/**
			 * Get the total hang income within this shift
			 */
			float totalHang = 0;
			float totalHang_2 = 0;
			sql = prefix + Order.MANNER_HANG;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalHang = dbCon.rs.getFloat(1);
				totalHang_2 = dbCon.rs.getFloat(2);
			}
			dbCon.rs.close();
			
			float totalIncome = totalCash_2 + totalCreditCard_2 + totalMemberCard_2 + totalSign_2 + totalHang_2;			
			
			/**
			 * Calculate the price to all gifted food within this shift
			 */
			float totalGift = 0;
			sql = "SELECT SUM(unit_price * order_count * discount + taste_price) FROM " + Params.dbName + ".order_food WHERE order_id IN(" +
				  "SELECT id FROM " +Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id + 
				  " AND total_price IS NOT NULL" +
				  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "')" +
				  " AND (food_status & " + Food.GIFT + ") <> 0"; 
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalGift = dbCon.rs.getFloat(1);
			}
			dbCon.rs.close();
			
			/**
			 * Calculate the price to all discount food within this shift
			 */
			float totalDiscount = 0;
			sql = "SELECT SUM(unit_price * order_count * (1-discount) + taste_price) FROM " + Params.dbName + ".order_food WHERE order_id iN(" +
				  "SELECT id FROM " +Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id + 
				  " AND total_price IS NOT NULL" +
				  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "')" +
				  " AND discount < 1.00";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				totalDiscount = dbCon.rs.getFloat(1);
			}
			dbCon.rs.close();
			
			totalGift = (float)Math.round(totalGift * 100) / 100;
			totalDiscount = (float)Math.round(totalDiscount * 100) / 100;
			
			/**
			 * The json to shift record like below
			 * ["开始日期","结帐日期","账单数","现金金额","现金实收","刷卡金额",
			 * "刷卡实收","会员卡金额","会员卡实收","签单金额","签单实收",
			 * "挂账金额","挂账实收","实收金额","折扣金额","赠送金额"]
			 */
			
			String jsonOrder = "[\"$(on_duty)\",\"$(off_duty)\",\"$(orderAmount)\",\"$(cash)\",\"$(cash_2)\",\"$(credit_card)\",\"$(credit_card_2)\"," +
								"\"$(member)\",\"$(member_2)\",\"$(sign)\",\"$(sign_2)\",\"$(hang)\",\"$(hang_2)\"," +
								"\"$(totalActual)\",\"$(totalDiscount)\",\"$(totalGifted)\"]";
			jsonOrder = jsonOrder.replace("$(on_duty)", onDuty);
			jsonOrder = jsonOrder.replace("$(off_duty)", offDuty);
			jsonOrder = jsonOrder.replace("$(orderAmount)", Integer.toString(orderAmount));
			jsonOrder = jsonOrder.replace("$(cash)", Float.toString(totalCash));
			jsonOrder = jsonOrder.replace("$(cash_2)", Float.toString(totalCash_2));
			jsonOrder = jsonOrder.replace("$(credit_card)", Float.toString(totalCreditCard));
			jsonOrder = jsonOrder.replace("$(credit_card_2)", Float.toString(totalCreditCard_2));
			jsonOrder = jsonOrder.replace("$(member)", Float.toString(totalMemberCard));
			jsonOrder = jsonOrder.replace("$(member_2)", Float.toString(totalMemberCard_2));
			jsonOrder = jsonOrder.replace("$(sign)", Float.toString(totalSign));
			jsonOrder = jsonOrder.replace("$(sign_2)", Float.toString(totalSign_2));
			jsonOrder = jsonOrder.replace("$(hang)", Float.toString(totalHang));
			jsonOrder = jsonOrder.replace("$(hang_2)", Float.toString(totalHang_2));
			jsonOrder = jsonOrder.replace("$(totalActual)", Float.toString(totalIncome));
			jsonOrder = jsonOrder.replace("$(totalDiscount)", Float.toString(totalDiscount));
			jsonOrder = jsonOrder.replace("$(totalGifted)", Float.toString(totalGift));
			
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
