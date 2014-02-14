package com.wireless.Actions.billHistory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.util.DateType;

public class DailySettleStatDetailAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		List<Jsonable> list = new ArrayList<Jsonable>();
		DBCon dbCon = new DBCon();

/*		PrintWriter out = null;

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> rootMap = new HashMap<String, Object>();*/

		try {
			// 解决后台中文传到前台乱码
			
//			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal foodIDs : array
			 * "food1,food2,food3" dateBegin: dateEnd :
			 */

			String pin = (String)request.getAttribute("pin");

			
			dbCon.connect();
			// get the query condition
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");

			final ShiftDetail result = ShiftDao.getByRange(dbCon, 
										StaffDao.verify(dbCon, Integer.parseInt(pin)), 
										new DutyRange(onDuty, offDuty),
										DateType.HISTORY);

			/**
			 */

//			resultList.add(resultMap);
			dbCon.rs.close();
			
			Jsonable j = new Jsonable() {
				
				@Override
				public Map<String, Object> toJsonMap(int flag) {
					Map<String, Object> resultMap = new HashMap<String, Object>();
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
					return Collections.unmodifiableMap(resultMap);
				}
				
				@Override
				public List<Object> toJsonList(int flag) {
					return null;
				}
			};
			
			list.add(j);

		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(e);

		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);

		} finally {
			dbCon.disconnect();

			jobject.setRoot(list);
			
			response.getWriter().print(jobject.toString());
			
	/*		rootMap.put("root", resultList);

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = obj.toString();

			// System.out.println(outputJson);

			out.write(outputJson);*/
		}

		return null;
	}
}
