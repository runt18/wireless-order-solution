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
import com.wireless.db.member.MemberDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.member.MemberStatistics;
import com.wireless.pojo.billStatistics.member.StatisticsByEachDay;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class QueryMemberStatisticsAction extends DispatchAction {
	
	/**
	 * 会员充值走势图
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward chargeStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBegin = request.getParameter("dateBegin");
		final String dateEnd = request.getParameter("dateEnd");
		final JObject jObject = new JObject();
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(branchId != null && !branchId.isEmpty()){
				extraCond.setBranch(Integer.parseInt(branchId));
			}

			final MemberStatistics memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {
				xAxis.add("\"" + e.getDate() + "\"");
				data.add(e.getCharge().getTotalAccountCharge());
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"avgMoney\" : " + memberStatistics.getAverageCharge() + ", \"avgCount\" : " + memberStatistics.getAverageChargeAmount() +  
					",\"ser\":[{\"name\":\"充值额\", \"data\" : " + data + "}]}";	
			
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartData);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
			
		}finally{
			
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 会员退款走势图
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward refundStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBegin = request.getParameter("dateBegin");
		final String dateEnd = request.getParameter("dateEnd");
		
		final JObject jObject = new JObject();
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(branchId != null && !branchId.isEmpty()){
				extraCond.setBranch(Integer.parseInt(branchId));
			}

			final MemberStatistics memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {
				xAxis.add("\"" + e.getDate() + "\"");
				data.add(e.getCharge().getTotalAccountRefund());
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"avgMoney\" : " + memberStatistics.getAverageRefund() + ", \"avgCount\" : " + memberStatistics.getAverageRefundAmount() +  
					",\"ser\":[{\"name\":\"退款额\", \"data\" : " + data + "}]}";	
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartData);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
			
		}finally{
			
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 会员消费走势图
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward consumeStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBegin = request.getParameter("dateBegin");
		final String dateEnd = request.getParameter("dateEnd");		
		final JObject jObject = new JObject();
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));

			final CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(branchId != null && !branchId.isEmpty()){
				extraCond.setBranch(Integer.parseInt(branchId));
			}
			
			final MemberStatistics memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {
				xAxis.add("\""+e.getDate()+"\"");
				data.add(e.getConsumption().getTotalConsume());
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"avgMoney\" : " + memberStatistics.getAverageConsume() + ", \"avgCount\" : " + memberStatistics.getAverageConsumeAmount() +  
					",\"ser\":[{\"name\":\"消费额\", \"data\" : " + data + "}]}";	
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartData);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
			
		}finally{
			
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
	
	/**
	 * 会员开卡走势图
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward createdStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBegin = request.getParameter("dateBegin");
		final String dateEnd = request.getParameter("dateEnd");
		final JObject jObject = new JObject();
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			if(branchId != null && !branchId.isEmpty()){
				extraCond.setBranch(Integer.parseInt(branchId));
			}

			final MemberStatistics memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<>();
			List<Integer> data = new ArrayList<>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {
				xAxis.add("\""+e.getDate()+"\"");
				data.add(e.getCreateMembers().size());
			}
			
			final String chartData = "{\"xAxis\":" + xAxis + ",\"avgCount\" : " + memberStatistics.getAverageCreated() +  
					",\"ser\":[{\"name\":\"开卡数\", \"data\" : " + data + "}]}";	
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartData);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}		
	
	/**
	 * 获取开卡会员的数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward createdMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBegin = request.getParameter("dateBegin");
		final String dateEnd = request.getParameter("dateEnd");
		final String memberType = request.getParameter("memberType");
		final String memberCardOrMobileOrName = request.getParameter("memberCardOrMobileOrName");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final JObject jObject = new JObject();
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final MemberDao.ExtraCond extraCond = new MemberDao.ExtraCond();
			
			if(branchId != null && !branchId.isEmpty()){
				extraCond.setBranch(Integer.parseInt(branchId));
			}
			
			if(memberType != null && !memberType.trim().isEmpty() && !memberType.equals("-1"))
				extraCond.setMemberType(Integer.parseInt(memberType));
			if(dateBegin != null && !dateBegin.isEmpty()){
				extraCond.setCreateRange(new DutyRange(dateBegin, dateEnd));
			}
			
			if(memberCardOrMobileOrName != null && !memberCardOrMobileOrName.trim().isEmpty()){
				extraCond.setFuzzyName(memberCardOrMobileOrName);
			}
			
			List<Member> list = MemberDao.getByCond(staff, extraCond, null);
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				list = DataPaging.getPagingData(list, true, start, limit);
			}
			jObject.setTotalProperty(list.size());
			jObject.setRoot(list);
			
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
}
