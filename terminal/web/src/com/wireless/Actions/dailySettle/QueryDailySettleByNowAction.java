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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryDailySettleByNowAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		HashMap resultMap = new HashMap();
		ShiftDetail res = null;
		try{
			String pin = (String)request.getAttribute("pin");
			String queryType = request.getParameter("queryType");
			
			if(queryType == null)
				return null;
			
			if(Integer.valueOf(queryType) == 0){
				res = QueryShiftDao.execByNow(StaffDao.verify(Integer.parseInt(pin)));
			}else if(Integer.valueOf(queryType) == 1){
				res = QueryShiftDao.execDailySettleByNow(StaffDao.verify(Integer.parseInt(pin)));
			}
			
			if(res == null)
				return null;
			
			resultMap.put("onDuty", res.getOnDuty());
			resultMap.put("offDuty", res.getOffDuty());
			
			resultMap.put("allBillCount", res.getOrderAmount());

			resultMap.put("cashBillCount", res.getCashAmount());
			resultMap.put("cashAmount", res.getCashTotalIncome());
			resultMap.put("cashActual", res.getCashActualIncome());

			resultMap.put("creditBillCount", res.getCreditCardAmount());
			resultMap.put("creditAmount", res.getCreditTotalIncome());
			resultMap.put("creditActual", res.getCreditActualIncome());

			resultMap.put("memberBillCount", res.getMemberCardAmount());
			resultMap.put("memberAmount", res.getMemberTotalIncome());
			resultMap.put("memberActual", res.getMemberActualIncome());

			resultMap.put("signBillCount", res.getSignAmount());
			resultMap.put("signAmount", res.getSignTotalIncome());
			resultMap.put("signActual", res.getSignActualIncome());

			resultMap.put("hangBillCount", res.getHangAmount());
			resultMap.put("hangAmount", res.getHangTotalIncome());
			resultMap.put("hangActual", res.getHangActualIncome());

			resultMap.put("discountAmount", res.getDiscountIncome());
			resultMap.put("discountBillCount", res.getDiscountAmount());

			resultMap.put("giftAmount", res.getGiftIncome());
			resultMap.put("giftBillCount", res.getGiftAmount());

			resultMap.put("returnAmount", res.getCancelIncome());
			resultMap.put("returnBillCount", res.getCancelAmount());

			resultMap.put("repayAmount", res.getPaidIncome());
			resultMap.put("repayBillCount", res.getPaidAmount());

			resultMap.put("serviceAmount", res.getServiceIncome());
			resultMap.put("serviceBillCount", res.getServiceAmount());
			
			resultMap.put("eraseAmount", res.getEraseIncome());
			resultMap.put("eraseBillCount", res.getEraseAmount());

			List deptList = new ArrayList();
			for (IncomeByDept deptIncome : res.getDeptIncome()) {
				HashMap deptMap = new HashMap();
				deptMap.put("deptName", deptIncome.getDept().getName());
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
