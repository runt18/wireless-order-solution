package com.wireless.Actions.billHistory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;

public class DailySettleStatDetailAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> rootMap = new HashMap<String, Object>();

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
			// get the query condition
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");

			ShiftDetail result = null;
			result = QueryShiftDao.exec(dbCon, 
										StaffDao.verify(dbCon, Integer.parseInt(pin)), 
										onDuty, offDuty,
										QueryShiftDao.QUERY_HISTORY);

			/**
			 */
			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			resultMap.put("allBillCount", result.getOrderAmount());

			resultMap.put("cashBillCount", result.getCashAmount());
			resultMap.put("cashAmount", result.getCashTotalIncome());
			resultMap.put("cashActual", result.getCashActualIncome());

			resultMap.put("creditBillCount", result.getCreditCardAmount());
			resultMap.put("creditAmount", result.getCreditTotalIncome());
			resultMap.put("creditActual", result.getCreditActualIncome());

			resultMap.put("memberBillCount", result.getMemberCardAmount());
			resultMap.put("memberAmount", result.getMemberTotalIncome());
			resultMap.put("memberActual", result.getMemberActualIncome());

			resultMap.put("signBillCount", result.getSignAmount());
			resultMap.put("signAmount", result.getSignTotalIncome());
			resultMap.put("signActual", result.getSignActualIncome());

			resultMap.put("hangBillCount", result.getHangAmount());
			resultMap.put("hangAmount", result.getHangTotalIncome());
			resultMap.put("hangActual", result.getHangActualIncome());

			resultMap.put("discountAmount", result.getDiscountIncome());
			resultMap.put("discountBillCount", result.getDiscountAmount());

			resultMap.put("giftAmount", result.getGiftIncome());
			resultMap.put("giftBillCount", result.getGiftAmount());

			resultMap.put("returnAmount", result.getCancelIncome());
			resultMap.put("returnBillCount", result.getCancelAmount());

			resultMap.put("repayAmount", result.getPaidIncome());
			resultMap.put("repayBillCount", result.getPaidAmount());

			resultMap.put("serviceAmount", result.getServiceIncome());
			resultMap.put("serviceBillCount", result.getServiceAmount());
			
			resultMap.put("eraseAmount", result.getEraseIncome());
			resultMap.put("eraseBillCount", result.getEraseAmount());

			List<HashMap<String, Object>> deptList = new ArrayList<HashMap<String, Object>>();
			for (IncomeByDept deptIncome : result.getDeptIncome()) {
				HashMap<String, Object> deptMap = new HashMap<String, Object>();
				deptMap.put("deptName", deptIncome.getDept().getName());
				deptMap.put("deptDiscount", deptIncome.getDiscount());
				deptMap.put("deptGift", deptIncome.getGift());
				deptMap.put("deptAmount", deptIncome.getIncome());
				deptList.add(deptMap);
			}
			resultMap.put("deptInfos", deptList);

			resultMap.put("message", "normal");

			resultList.add(resultMap);
			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");

			} else {
				resultMap.put("message", "没有获取到信息，请重新确认");
			}
			resultList.add(resultMap);
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);

		} catch (IOException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);

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
