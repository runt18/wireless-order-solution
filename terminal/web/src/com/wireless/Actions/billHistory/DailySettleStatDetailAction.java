package com.wireless.Actions.billHistory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class DailySettleStatDetailAction extends Action {
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");

		try {

			// get the query condition
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final ShiftDetail result = ShiftDao.getByRange(staff, new DutyRange(onDuty, offDuty), new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY));

			final List<Jsonable> list = new ArrayList<Jsonable>();
			list.add(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("allBillCount", result.getOrderAmount());

					jm.putJsonable(result.getIncomeByPay(), flag);
					
					jm.putFloat("discountAmount", result.getDiscountIncome());
					jm.putInt("discountBillCount", result.getDiscountAmount());

					jm.putFloat("giftAmount", result.getGiftIncome());
					jm.putInt("giftBillCount", result.getGiftAmount());

					jm.putFloat("returnAmount", result.getCancelIncome());
					jm.putInt("returnBillCount", result.getCancelAmount());

					jm.putFloat("repayAmount", result.getPaidIncome());
					jm.putInt("repayBillCount", result.getPaidAmount());

					jm.putFloat("serviceAmount", result.getServiceIncome());
					jm.putInt("serviceBillCount", result.getServiceAmount());
					
					jm.putFloat("eraseAmount", result.getEraseIncome());
					jm.putInt("eraseBillCount", result.getEraseAmount());

					List<Jsonable> deptList = new ArrayList<Jsonable>(result.getDeptIncome().size());
					for (final IncomeByDept deptIncome : result.getDeptIncome()) {
						deptList.add(new Jsonable(){
							@Override
							public JsonMap toJsonMap(int flag) {
								JsonMap jm = new JsonMap();
								jm.putString("deptName", deptIncome.getDept().getName());
								jm.putFloat("deptDiscount", deptIncome.getDiscount());
								jm.putFloat("deptGift", deptIncome.getGift());
								jm.putFloat("deptAmount", deptIncome.getIncome());
								return jm;
							}

							@Override
							public void fromJsonMap(JsonMap jsonMap, int flag) {
								
							};
						
						});
					}
					jm.putJsonableList("deptInfos", deptList, 0);

					jm.putString("message", "normal");
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
			jObject.setRoot(list);

		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
			
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);

		} finally {
			response.getWriter().print(jObject.toString());
		}

		return null;
	}
}
