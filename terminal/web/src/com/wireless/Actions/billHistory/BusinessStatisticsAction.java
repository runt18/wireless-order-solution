package com.wireless.Actions.billHistory;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryShift;
import com.wireless.db.VerifyPin;
import com.wireless.protocol.Terminal;

public class BusinessStatisticsAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DBCon dbCon = new DBCon();

		PrintWriter out = null;
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * pin = 1 & dateBegin='2012-5-1' & dateEnd='2012-5-5'
			 * "pin" : pin,
			 * "dateBegin" : businessStaticBeginDate,
			 * "dateEnd" : businessStaticEndDate,
			 * "StatisticsType" : "History"  
			 */
			dbCon.connect();
			
			String sql;
			
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("dateBegin"));
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("businessStaticEndDate"));
			
			Calendar c = Calendar.getInstance();
			c.setTime(dateBegin);
			
			while(dateBegin.compareTo(dateEnd) < 0){
				c.add(Calendar.DATE, 1);
				Date dateItemEnd = c.getTime();
				sql = " SELECT MIN(on_duty) AS on_duty, MAX(off_duty) AS off_duty FROM " +
					  Params.dbName + ".daily_settle_history " + 
					  " WHERE " +
					  " restaurant_id = " + term.restaurant_id +
					  " AND " +
					  " off_duty BETWEEN " + new SimpleDateFormat("yyyy-MM-dd").format(dateBegin) + " AND " + new SimpleDateFormat("yyyy-MM-dd").format(dateItemEnd);
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					String onDuty;
					java.sql.Timestamp onDutyTS = dbCon.rs.getTimestamp("on_duty");
					if(onDutyTS == null){
						onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateBegin); 
					}else{
						onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dbCon.rs.getTimestamp("on_duty").getTime()));						
					}
					
					String offDuty;
					java.sql.Timestamp offDutyTS = dbCon.rs.getTimestamp("on_duty");
					if(offDutyTS == null){
						offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateItemEnd); 
					}else{
						offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dbCon.rs.getTimestamp("off_duty").getTime()));
					}
					QueryShift.Result result = QueryShift.exec(dbCon, term, onDuty, offDuty, QueryShift.QUERY_HISTORY);
					
					System.out.print("日期:" + new SimpleDateFormat("yyyy-MM-dd").format(dateItemEnd));
					System.out.print("账单数:" + result.orderAmount);
					System.out.print("现金:" + result.cashIncome);
					System.out.print("刷卡:" + result.creditCardIncome);
					System.out.print("会员卡:" + result.memberCardIncome);
					System.out.print("签单:" + result.signIncome);
					System.out.print("挂账:" + result.hangIncome);
					System.out.print("折扣额:" + result.discountIncome);
					System.out.print("赠送额:" + result.giftIncome);
					System.out.print("退菜额:" + result.cancelIncome);
					System.out.print("反结帐额:" + result.paidIncome);
					System.out.print("服务费额:" + result.serviceIncome);
					float totalIncome = result.cashIncome + result.creditCardIncome + result.memberCardIncome + result.signIncome + result.hangIncome;
					System.out.print("金额:" + totalIncome);
					float totalActual = result.cashIncome2 + result.creditCardIncome2 + result.memberCardIncome2 + result.signIncome2 + result.hangIncome2;
					System.out.print("实收:" + totalActual);
					System.out.println();
				}
				
				
				dateBegin = c.getTime();
			}
			
		}catch(ParseException e){
			System.err.println("日期格式不正确");
			
		}catch(SQLException e){
			System.err.println("数据查询语句不正确");
			
		}finally {
			dbCon.disconnect();


			// System.out.println(outputJson);

			out.write("");
		}
		
		return null;
	}
}
