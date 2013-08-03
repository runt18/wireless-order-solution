package com.wireless.Actions.billStatistics;

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

public class ShiftStatDetailAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();

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
			String StatisticsType = request.getParameter("StatisticsType");

			ShiftDetail resutl = null;
			if (StatisticsType.equals("Today")) {
				resutl = QueryShiftDao.exec(dbCon, 
											StaffDao.verify(dbCon, Integer.parseInt(pin)), 
											onDuty, offDuty,
											QueryShiftDao.QUERY_TODAY);
			} else if (StatisticsType.equals("History")) {
				resutl = QueryShiftDao.exec(dbCon, 
											StaffDao.verify(dbCon, Integer.parseInt(pin)),
											onDuty, offDuty,
											QueryShiftDao.QUERY_HISTORY);
			}

			/**
			 */
			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			resultMap.put("allBillCount", resutl.getOrderAmount());

			resultMap.put("cashBillCount", resutl.getCashAmount());
			resultMap.put("cashAmount", resutl.getCashTotalIncome());
			resultMap.put("cashActual", resutl.getCashActualIncome());

			resultMap.put("creditBillCount", resutl.getCreditCardAmount());
			resultMap.put("creditAmount", resutl.getCreditTotalIncome());
			resultMap.put("creditActual", resutl.getCreditActualIncome());

			resultMap.put("memberBillCount", resutl.getMemberCardAmount());
			resultMap.put("memberAmount", resutl.getMemberTotalIncome());
			resultMap.put("memberActual", resutl.getMemberActualIncome());

			resultMap.put("signBillCount", resutl.getSignAmount());
			resultMap.put("signAmount", resutl.getSignTotalIncome());
			resultMap.put("signActual", resutl.getSignActualIncome());

			resultMap.put("hangBillCount", resutl.getHangAmount());
			resultMap.put("hangAmount", resutl.getHangTotalIncome());
			resultMap.put("hangActual", resutl.getHangActualIncome());

			resultMap.put("discountAmount", resutl.getDiscountIncome());
			resultMap.put("discountBillCount", resutl.getDiscountAmount());

			resultMap.put("giftAmount", resutl.getGiftIncome());
			resultMap.put("giftBillCount", resutl.getGiftAmount());

			resultMap.put("returnAmount", resutl.getCancelIncome());
			resultMap.put("returnBillCount", resutl.getCancelAmount());

			resultMap.put("repayAmount", resutl.getPaidIncome());
			resultMap.put("repayBillCount", resutl.getPaidAmount());

			resultMap.put("serviceAmount", resutl.getServiceIncome());
			resultMap.put("serviceBillCount", resutl.getServiceAmount());
			
			resultMap.put("eraseAmount", resutl.getEraseIncome());
			resultMap.put("eraseBillCount", resutl.getEraseAmount());

			List<HashMap<String, Object>> deptList = new ArrayList<HashMap<String, Object>>();
			for (IncomeByDept deptIncome : resutl.getDeptIncome()) {
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
