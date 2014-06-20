package com.wireless.Actions.billStatistics;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeTrendByDept;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil.Pattern;
import com.wireless.util.DateType;

public class SalesSubStatisticsChartAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		String onDuty = request.getParameter("dateBeg");
		String offDuty = request.getParameter("dateEnd");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		String region = request.getParameter("region");
		String deptId = request.getParameter("deptId");
		
		List<IncomeTrendByDept> incomesByEachDay;
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY);
			if(opening != null && !opening.isEmpty()){
				HourRange hr = new HourRange(opening, ending, Pattern.HOUR);
				extraCond.setHourRange(hr);
			}
			if(region != null && !region.equals("-1")){
				extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
				
			}

			if(deptId != null && !deptId.equals("-1")){
				extraCond.setDept(Department.DeptId.valueOf(Integer.parseInt(deptId)));
				
			}
			incomesByEachDay = CalcBillStatisticsDao.calcIncomeTrendByDept(staff, new DutyRange(onDuty, offDuty), extraCond);
			
			
			
			List<String> xAxis = new ArrayList<String>();
			List<Float> data = new ArrayList<Float>();
			float totalMoney = 0;
			int count = 0;
			for (IncomeTrendByDept e : incomesByEachDay) {
				xAxis.add("\'"+ e.getRange().getOffDutyFormat() +"\'");
				data.add(e.getDeptIncome().getIncome());
				totalMoney += e.getDeptIncome().getIncome();
				count ++ ;
			}
			final String chartData = "{\"xAxis\":" + xAxis + ",\"totalMoney\" : " + totalMoney + ",\"avgMoney\" : " + Math.round((totalMoney/count)*100)/100 + 
					",\"ser\":[{\"name\":\'营业额\', \"data\" : " + data + "}]}";
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("chart", chartData);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			
			});

		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);			
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
