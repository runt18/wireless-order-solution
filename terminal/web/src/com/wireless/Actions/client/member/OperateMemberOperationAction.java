package com.wireless.Actions.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcMemberStatisticsDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.SummaryByEachMember;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class OperateMemberOperationAction extends DispatchAction{

	public ActionForward getMemberOperationType(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		
		final List<Jsonable> operations = new ArrayList<>();
		
		for(final MemberOperation.OperationType operate : MemberOperation.OperationType.values()){
			operations.add(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("value", operate.getValue());
					jm.putString("name", operate.toString());
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
			});
			
		}
		
		jObject.setRoot(new Jsonable(){
			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putJsonableList("operateType", operations, 0);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jm, int flag) {
			}
			
		});
		
		response.getWriter().print(jObject.toString());
		
		return null;
	}
	
	/**
	 * 会员汇总
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getMemberSummary(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String memberTypeId = request.getParameter("memberTypeId");
		final String fuzzy = request.getParameter("fuzzy");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final JObject jObject = new JObject();
		
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));

			final MemberOperationDao.ExtraCond extraCond = new MemberOperationDao.ExtraCond(DateType.HISTORY).setCalcByDuty(true);
			
			if(branchId != null && !branchId.isEmpty() && !branchId.equals("-1")){
				extraCond.setBranch(Integer.parseInt(branchId));
			}
			
			if(onDuty != null && !onDuty.isEmpty() && offDuty != null && !offDuty.isEmpty()){
				extraCond.setOperateDate(new DutyRange(onDuty, offDuty));
			}
			
			if(memberTypeId != null && !memberTypeId.isEmpty() && Integer.valueOf(memberTypeId) > 0){
				extraCond.setMemberType(Integer.parseInt(memberTypeId));
			}
			
			if(fuzzy != null && !fuzzy.isEmpty()){
				extraCond.setFuzzy(fuzzy);
			}
			
			final List<SummaryByEachMember> list = CalcMemberStatisticsDao.calcByEachMember(staff, extraCond);
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jObject.setTotalProperty(list.size());
				
				SummaryByEachMember sm = new SummaryByEachMember();
				for(SummaryByEachMember s : list){
					sm.setChargeActual(sm.getChargeActual() + s.getChargeActual());
					sm.setChargeMoney(sm.getChargeMoney() + s.getChargeMoney());
					sm.setRefundActual(sm.getRefundActual() + s.getRefundActual());
					sm.setRefundMoney(sm.getRefundMoney() + s.getRefundMoney());
					sm.setConsumeBase(sm.getConsumeBase() + s.getConsumeBase());
					sm.setConsumeExtra(sm.getConsumeExtra() + s.getConsumeExtra());
					sm.setConsumeTotal(sm.getConsumeTotal() + s.getConsumeTotal());
					sm.setRemainingBalance(sm.getRemainingBalance() + s.getRemainingBalance());
					sm.setChangedPoint(sm.getChangedPoint() + s.getChangedPoint());
					sm.setRemainingPoint(sm.getRemainingPoint() + s.getRemainingPoint());
					sm.setDeltaBase(sm.getDeltaBase() + s.getDeltaBase());
					sm.setDeltaExtra(sm.getDeltaExtra() + s.getDeltaExtra());
					sm.setMember(new Member(0));
				}
				
				List<SummaryByEachMember> limitResult = DataPaging.getPagingData(list, true, start, limit);
				limitResult.add(sm);
				
				jObject.setRoot(limitResult);
			}else{
				jObject.setRoot(list);
			}
			
			
		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
		} catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		
		return null;
	}
	
	
	
	
	
	
	
}
