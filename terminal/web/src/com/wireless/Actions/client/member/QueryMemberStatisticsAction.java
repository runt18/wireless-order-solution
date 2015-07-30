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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.member.MemberStatistics;
import com.wireless.pojo.billStatistics.member.StatisticsByEachDay;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class QueryMemberStatisticsAction  extends DispatchAction {
	
	/**
	 * history
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
			
			chartData = "{\"xAxis\":" + xAxis + ",\"ser\":[{\"name\":\"充值额\", \"data\" : " + data + "}]}";	
			
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

}
