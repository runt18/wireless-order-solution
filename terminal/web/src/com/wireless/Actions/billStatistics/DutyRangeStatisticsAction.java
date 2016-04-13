package com.wireless.Actions.billStatistics;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.ShiftGeneralDao;
import com.wireless.db.billStatistics.ShiftGeneralDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.PaymentGeneral;
import com.wireless.pojo.billStatistics.ShiftGeneral;
import com.wireless.pojo.billStatistics.ShiftGeneral.StaffPayment;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.DataPaging;

public class DutyRangeStatisticsAction extends DispatchAction {
	
	/**
	 * 当日交班记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward today(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		
		final JObject jObject = new JObject();
		try{
			jObject.setRoot(ShiftGeneralDao.getTodayShift(StaffDao.verify(Integer.parseInt(pin))));
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 当日交班树
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		StringBuilder jsonTree = new StringBuilder();
		JObject jobject = new JObject();
		List<ShiftGeneral> list = null;
		try{
			String pin = (String)request.getAttribute("pin");
			list = ShiftGeneralDao.getToday(StaffDao.verify(Integer.parseInt(pin)));
			
			for (int i = 0; i < list.size(); i++) {
				jsonTree.append(i > 0 ? "," : "");
				jsonTree.append("{");
				jsonTree.append("text:'" + DateUtil.format(list.get(i).getOnDuty()).substring(11) + " -- " + DateUtil.format(list.get(i).getOffDuty()).substring(11) + " (交班人：" +list.get(i).getStaffName()+")' ");
				jsonTree.append(",onDuty:'" + DateUtil.format(list.get(i).getOnDuty()) + "'");
				jsonTree.append(",offDuty:'" + DateUtil.format(list.get(i).getOffDuty()) + "'");
				jsonTree.append(",expanded : " + (list.get(i).getPayments().isEmpty() ? false : true));
				jsonTree.append(",expandable : true");
				jsonTree.append(",children:[");
				jsonTree.append(getChildren(list.get(i).getPayments()));
				jsonTree.append("]");
				jsonTree.append("}");
			}
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getMessage());
			e.printStackTrace();
			
		}catch(SQLException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getErrorCode(), e.getMessage());
			e.printStackTrace();
			
		}finally{
			response.getWriter().print("[" + jsonTree.toString() + "]");
		}
		return null;
	}
	
	private StringBuilder getChildren(List<StaffPayment> list) throws SQLException{
		StringBuilder jsb = new StringBuilder();
		
		for(int i = 0; i < list.size(); i++){
			if(i > 0){
				jsb.append(",");
			}
			jsb.append("{");
			jsb.append("text:'" + list.get(i).getStaffName() + " (应交款：<font style=\"color :green;font-weight:bolder\">"+ NumericUtil.float2String2(list.get(i).getTotalPrice())
						+"</font> ，实交款：<font style=\"color :red;font-weight:bolder\">"+ NumericUtil.float2String2(list.get(i).getActualPrice()) +"</font>)'");
			jsb.append(",expanded : " + (list.get(i).getPayments().isEmpty() ? true : false));
			jsb.append(",expandable : true");
			jsb.append(",icon : '../../images/user.png'");
			jsb.append(",children:[");
			jsb.append(getChildrenPayments(list.get(i).getPayments()));
			jsb.append("]");
			jsb.append("}");
		}
		
		return jsb;
	}
	
	private StringBuilder getChildrenPayments(List<PaymentGeneral> list) throws SQLException{
		StringBuilder jsb = new StringBuilder();
		
		for(int i = 0; i < list.size(); i++){
			if(i > 0){
				jsb.append(",");
			}
			jsb.append("{");
			jsb.append("leaf:true");
			jsb.append(",text:'" + list.get(i).getOnDuty().substring(11) + " -- " + list.get(i).getOffDuty().substring(11) + "'");
			jsb.append(",onDuty:'" + list.get(i).getOnDuty() + "'");
			jsb.append(",offDuty:'" + list.get(i).getOffDuty() + "'");
			jsb.append(",staffId:'" + list.get(i).getStaffId() + "'");
			jsb.append("}");
		}
		
		return jsb;
	}
	/**
	 * 历史交班记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward history(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final List<ShiftGeneral> list = ShiftGeneralDao.getByRange(staff, new ExtraCond(DateType.HISTORY).setRange(new DutyRange(onDuty, offDuty)));
			if(list != null){
				jObject.setTotalProperty(list.size());
				jObject.setRoot(DataPaging.getPagingData(list, Boolean.parseBoolean(isPaging), start, limit));
			}
			
		}catch(BusinessException | SQLException e){	
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}
