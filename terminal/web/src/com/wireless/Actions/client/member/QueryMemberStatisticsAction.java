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

public class QueryMemberStatisticsAction  extends DispatchAction {
	
	/**
	 * 会员充值报表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward chargeStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jObject = new JObject();
		try{
			
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			
			CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			String chartData = null ;
			
			final MemberStatistics memberStatistics;
			
			memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {

					xAxis.add("\""+e.getDate()+"\"");
					data.add(e.getCharge().getTotalAccountCharge());
			}
			
			chartData = "{\"xAxis\":" + xAxis + ",\"avgMoney\" : " + memberStatistics.getAverageCharge() + ", \"avgCount\" : " + memberStatistics.getAverageChargeAmount() +  
					",\"ser\":[{\"name\":\"充值额\", \"data\" : " + data + "}]}";	
			
			final String chartDatas = chartData;
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartDatas);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException e){
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
	 * 退款
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward refundStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jObject = new JObject();
		try{
			
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			
			CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			String chartData = null ;
			
			final MemberStatistics memberStatistics;
			
			memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {

					xAxis.add("\""+e.getDate()+"\"");
					data.add(e.getCharge().getTotalAccountRefund());
			}
			
			chartData = "{\"xAxis\":" + xAxis + ",\"avgMoney\" : " + memberStatistics.getAverageRefund() + ", \"avgCount\" : " + memberStatistics.getAverageRefundAmount() +  
					",\"ser\":[{\"name\":\"退款额\", \"data\" : " + data + "}]}";	
			
			final String chartDatas = chartData;
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartDatas);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException e){
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
	 * 消费
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward consumeStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jObject = new JObject();
		try{
			
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			
			CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			String chartData = null ;
			
			final MemberStatistics memberStatistics;
			
			memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {

					xAxis.add("\""+e.getDate()+"\"");
					data.add(e.getConsumption().getTotalConsume());
			}
			
			chartData = "{\"xAxis\":" + xAxis + ",\"avgMoney\" : " + memberStatistics.getAverageConsume() + ", \"avgCount\" : " + memberStatistics.getAverageConsumeAmount() +  
					",\"ser\":[{\"name\":\"消费额\", \"data\" : " + data + "}]}";	
			
			final String chartDatas = chartData;
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartDatas);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException e){
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
	 * 开卡走势图
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward createdStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jObject = new JObject();
		try{
			
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			
			CalcMemberStatisticsDao.ExtraCond extraCond = new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY);
			
			String chartData = null ;
			
			final MemberStatistics memberStatistics;
			
			memberStatistics = CalcMemberStatisticsDao.calcStatisticsByEachDay(staff, new DutyRange(dateBegin, dateEnd), extraCond);
			
			List<String> xAxis = new ArrayList<>();
			List<Integer> data = new ArrayList<>();
			for (StatisticsByEachDay e : memberStatistics.getStatistics()) {

					xAxis.add("\""+e.getDate()+"\"");
					data.add(e.getCreateMembers().size());
			}
			
			chartData = "{\"xAxis\":" + xAxis + ",\"avgCount\" : " + memberStatistics.getAverageCreated() +  
					",\"ser\":[{\"name\":\"开卡数\", \"data\" : " + data + "}]}";	
			
			final String chartDatas = chartData;
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("businessChart", chartDatas);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(SQLException e){
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
	 * 开卡会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward createdMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jobject = new JObject();
		try{
			
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			String memberType = request.getParameter("memberType");
			String memberCardOrMobileOrName = request.getParameter("memberCardOrMobileOrName");
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			
			MemberDao.ExtraCond extraCond = new MemberDao.ExtraCond();
			if(memberType != null && !memberType.trim().isEmpty() && !memberType.equals("-1"))
				extraCond.setMemberType(Integer.parseInt(memberType));
			if(dateBegin != null && !dateBegin.isEmpty()){
				extraCond.setCreateRange(new DutyRange(dateBegin, dateEnd));
			}
			if(memberCardOrMobileOrName != null && !memberCardOrMobileOrName.trim().isEmpty()){
				extraCond.setFuzzyName(memberCardOrMobileOrName);
			}
			List<Member> list = MemberDao.getByCond(staff, extraCond, null);
			list = DataPaging.getPagingData(list, true, start, limit);
			
			jobject.setTotalProperty(list.size());
			jobject.setRoot(list);
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	
}
