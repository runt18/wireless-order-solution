package com.wireless.Actions.represent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class QueryWxCommisisonAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String dateBegin = request.getParameter("dateBegin");
		final String dateEnd = request.getParameter("dateEnd");
		final String minCommissionAmount = request.getParameter("minCommissionAmount");
		final String maxCommissionAmount = request.getParameter("maxCommissionAmount");
		final String branchId = request.getParameter("branchId");
		
		try {
			final Staff staff = StaffDao.verify(Integer.valueOf(pin));
			MemberOperationDao.ExtraCond extraCondToday = new MemberOperationDao.ExtraCond(DateType.TODAY).setChargeType(ChargeType.COMMISSION);
			MemberOperationDao.ExtraCond extraCondHistory = new MemberOperationDao.ExtraCond(DateType.HISTORY).setChargeType(ChargeType.COMMISSION);
			List<MemberOperation> operation = new ArrayList<>();
			
			if(branchId != null && !branchId.isEmpty() && Integer.valueOf(branchId) >= 0){
				extraCondToday.setBranch(Integer.valueOf(branchId));
				extraCondHistory.setBranch(Integer.valueOf(branchId));
			}
			
			if(dateBegin != null && !dateBegin.isEmpty() && dateEnd != null && !dateEnd.isEmpty()){
				extraCondToday.setOperateDate(new DutyRange(dateBegin, dateEnd));
				extraCondHistory.setOperateDate(new DutyRange(dateBegin, dateEnd));
			}
			
			if(minCommissionAmount != null && !minCommissionAmount.isEmpty()){
				extraCondToday.setMinChargeAmount(Float.valueOf(minCommissionAmount));
				extraCondHistory.setMinChargeAmount(Float.valueOf(minCommissionAmount));
			}
			
			if(maxCommissionAmount != null && !maxCommissionAmount.isEmpty()){
				extraCondToday.setMaxChargeAmount(Float.valueOf(maxCommissionAmount));
				extraCondHistory.setMaxChargeAmount(Float.valueOf(maxCommissionAmount));
			}
			
			operation = MemberOperationDao.getByCond(staff, extraCondToday, null);
			operation.addAll(MemberOperationDao.getByCond(staff, extraCondHistory, null));
			
			jObject.setRoot(operation);
		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
