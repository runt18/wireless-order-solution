package com.wireless.Actions.dailySettle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.ShiftDetail;

public class QueryDailySettleByNowAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
//		List<Jsonable> list = new ArrayList<Jsonable>(); 
		ShiftDetail resd = null;
		try{
			String pin = (String)request.getAttribute("pin");
			String queryType = request.getParameter("queryType");
			
			if(queryType == null)
				return null;
			
			if(Integer.valueOf(queryType) == 0){
				resd = ShiftDao.getCurrentShift(StaffDao.verify(Integer.parseInt(pin)));
			}else if(Integer.valueOf(queryType) == 1){
				resd = ShiftDao.getTodayDaily(StaffDao.verify(Integer.parseInt(pin)));
			}
			
/*	
			if(resd == null)
				return null;

			final ShiftDetail res = resd;
			
     		Jsonable j = new Jsonable() {
				
				@Override
				public Map<String, Object> toJsonMap(int flag) {
					Map<String, Object> resultMap = new HashMap<String, Object>();
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
					
					resultMap.put("couponAmount", res.getCouponIncome());
					resultMap.put("couponBillCount", res.getCouponAmount());

					List<Map<String, Object>> deptList = new ArrayList<Map<String, Object>>();
					for (IncomeByDept deptIncome : res.getDeptIncome()) {
						Map<String, Object> deptMap = new HashMap<String, Object>();
						deptMap.put("deptName", deptIncome.getDept().getName());
						deptMap.put("deptDiscount", deptIncome.getDiscount());
						deptMap.put("deptGift", deptIncome.getGift());
						deptMap.put("deptAmount", deptIncome.getIncome());
						deptList.add(deptMap);
					}
					resultMap.put("deptInfos", deptList);
					return Collections.unmodifiableMap(resultMap);
				}
				
				@Override
				public List<Object> toJsonList(int flag) {
					return null;
				}
			}; 
			list.add(j);
 */
			
			
			jobject.getOther().put("business", resd);
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
//			jobject.setRoot(list);
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
