package com.wireless.Actions.billStatistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.dbReflect.OrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;

public class ShiftStatDetailAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List resultList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal foodIDs : array
			 * "food1,food2,food3" dateBegin: dateEnd :
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String StatisticsType = request.getParameter("StatisticsType");

			QueryShiftDao.Result resutl = null;
			if (StatisticsType.equals("Today")) {
				resutl = QueryShiftDao.exec(dbCon, Long.parseLong(pin),
						Terminal.MODEL_STAFF, onDuty, offDuty,
						QueryShiftDao.QUERY_TODAY);
			} else if (StatisticsType.equals("History")) {
				resutl = QueryShiftDao.exec(dbCon, Long.parseLong(pin),
						Terminal.MODEL_STAFF, onDuty, offDuty,
						QueryShiftDao.QUERY_HISTORY);
			}

			/**
			 */
			HashMap resultMap = new HashMap();

			resultMap.put("allBillCount", resutl.orderAmount);

			resultMap.put("cashBillCount", resutl.cashAmount);
			resultMap.put("cashAmount", resutl.cashIncome);
			resultMap.put("cashActual", resutl.cashIncome2);

			resultMap.put("creditBillCount", resutl.creditCardAmount);
			resultMap.put("creditAmount", resutl.creditCardIncome);
			resultMap.put("creditActual", resutl.creditCardIncome2);

			resultMap.put("memberBillCount", resutl.memeberCardAmount);
			resultMap.put("memberAmount", resutl.memberCardIncome);
			resultMap.put("memberActual", resutl.memberCardIncome2);

			resultMap.put("signBillCount", resutl.signAmount);
			resultMap.put("signAmount", resutl.signIncome);
			resultMap.put("signActual", resutl.signIncome2);

			resultMap.put("hangBillCount", resutl.hangAmount);
			resultMap.put("hangAmount", resutl.hangIncome);
			resultMap.put("hangActual", resutl.hangIncome2);

			resultMap.put("discountAmount", resutl.discountIncome);
			resultMap.put("discountBillCount", resutl.discountAmount);

			resultMap.put("giftAmount", resutl.giftIncome);
			resultMap.put("giftBillCount", resutl.giftAmount);

			resultMap.put("returnAmount", resutl.cancelIncome);
			resultMap.put("returnBillCount", resutl.cancelAmount);

			resultMap.put("repayAmount", resutl.paidIncome);
			resultMap.put("repayBillCount", resutl.paidAmount);

			resultMap.put("serviceAmount", resutl.serviceIncome);
			resultMap.put("serviceBillCount", resutl.serviceAmount);
			
			resultMap.put("eraseAmount", resutl.eraseIncome);
			resultMap.put("eraseBillCount", resutl.eraseAmount);

			QueryShiftDao.DeptIncome[] deptIncomes = resutl.deptIncome;
			List deptList = new ArrayList();
			for (int i = 0; i < deptIncomes.length; i++) {
				HashMap deptMap = new HashMap();
				deptMap.put("deptName", deptIncomes[i].dept.name);
				deptMap.put("deptDiscount", deptIncomes[i].discount);
				deptMap.put("deptGift", deptIncomes[i].gift);
				deptMap.put("deptAmount", deptIncomes[i].income);
				deptList.add(deptMap);
			}
			resultMap.put("deptInfos", deptList);

			resultMap.put("message", "normal");

			resultList.add(resultMap);
			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");

			} else {
				resultMap.put("message", "没有获取到信息，请重新确认");
			}
			resultList.add(resultMap);
			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} catch (IOException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} finally {
			dbCon.disconnect();

			rootMap.put("root", resultList);

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = obj.toString();

			// System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}
