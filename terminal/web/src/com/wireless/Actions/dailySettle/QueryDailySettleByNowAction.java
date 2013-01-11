package com.wireless.Actions.dailySettle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.shift.QueryShiftDao;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.protocol.Terminal;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryDailySettleByNowAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		HashMap resultMap = new HashMap();
		QueryShiftDao.Result res = null;
		try{
			String pin = request.getParameter("pin");
			String queryType = request.getParameter("queryType");
			
			if(queryType == null)
				return null;
			
			if(Integer.valueOf(queryType) == 0)
				res = QueryShiftDao.execByNow(Long.valueOf(pin), Terminal.MODEL_STAFF);
			else if(Integer.valueOf(queryType) == 1)
				res = QueryShiftDao.execDailySettleByNow(Long.valueOf(pin));
			
			if(res == null)
				return null;
			
			resultMap.put("onDuty", res.onDuty);
			resultMap.put("offDuty", res.offDuty);
			
			resultMap.put("allBillCount", res.orderAmount);

			resultMap.put("cashBillCount", res.cashAmount);
			resultMap.put("cashAmount", res.cashIncome);
			resultMap.put("cashActual", res.cashIncome2);

			resultMap.put("creditBillCount", res.creditCardAmount);
			resultMap.put("creditAmount", res.creditCardIncome);
			resultMap.put("creditActual", res.creditCardIncome2);

			resultMap.put("memberBillCount", res.memeberCardAmount);
			resultMap.put("memberAmount", res.memberCardIncome);
			resultMap.put("memberActual", res.memberCardIncome2);

			resultMap.put("signBillCount", res.signAmount);
			resultMap.put("signAmount", res.signIncome);
			resultMap.put("signActual", res.signIncome2);

			resultMap.put("hangBillCount", res.hangAmount);
			resultMap.put("hangAmount", res.hangIncome);
			resultMap.put("hangActual", res.hangIncome2);

			resultMap.put("discountAmount", res.discountIncome);
			resultMap.put("discountBillCount", res.discountAmount);

			resultMap.put("giftAmount", res.giftIncome);
			resultMap.put("giftBillCount", res.giftAmount);

			resultMap.put("returnAmount", res.cancelIncome);
			resultMap.put("returnBillCount", res.cancelAmount);

			resultMap.put("repayAmount", res.paidIncome);
			resultMap.put("repayBillCount", res.paidAmount);

			resultMap.put("serviceAmount", res.serviceIncome);
			resultMap.put("serviceBillCount", res.serviceAmount);
			
			resultMap.put("eraseAmount", res.eraseIncome);
			resultMap.put("eraseBillCount", res.eraseAmount);

			List deptList = new ArrayList();
			for (IncomeByDept deptIncome : res.deptIncome) {
				HashMap deptMap = new HashMap();
				deptMap.put("deptName", deptIncome.getDept().name);
				deptMap.put("deptDiscount", deptIncome.getDiscount());
				deptMap.put("deptGift", deptIncome.getGift());
				deptMap.put("deptAmount", deptIncome.getIncome());
				deptList.add(deptMap);
			}
			resultMap.put("deptInfos", deptList);
			
			resultMap.put("success", true);
			
		}catch(Exception e){
			resultMap.put("success", false);
			e.printStackTrace();
		}finally{
			response.getWriter().print(JSONObject.fromObject(resultMap).toString());
		}
		
		return null;
	}

}
