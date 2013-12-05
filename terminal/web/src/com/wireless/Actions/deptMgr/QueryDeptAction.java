package com.wireless.Actions.deptMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.QueryIncomeStatisticsDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryDeptAction extends DispatchAction{

	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		List<String> xz = new ArrayList<String>();
		xz.add("\'one\'");
		xz.add("\'two\'");
		xz.add("\'thrid\'");
		xz.add("\'four\'");
		xz.add("\'fire\'");
		xz.add("\'six\'");
		xz.add("\'seven\'");
//		String xz1 = "['one', 'tow', 'thrid']";
//		String xdata1 = "[{name: 'good', data: [9, 78, 89]}]";

		
		List<Integer> data1 = new ArrayList<Integer>();
		data1.add(2);
		data1.add(4);
		data1.add(7);
		data1.add(18);
		data1.add(56);
		data1.add(78);
		data1.add(90);

		
		List<Integer> data2 = new ArrayList<Integer>();
		data2.add(9);
		data2.add(18);
		data2.add(89);
		
		Map<String, List<Integer>> xdata = new HashMap<String, List<Integer>>();
		
		xdata.put("good", data1);
		xdata.put("bed", data2);
		
		
		
		

		
		
		
		

//---------------------------	
		
		String pin = (String)request.getAttribute("pin");
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		incomesByEachDay.addAll(QueryIncomeStatisticsDao.getIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), "2013-11-22 00:00:00", "2013-11-29 23:59:59"));
		
		List<String> xAxis = new ArrayList<String>();
		List<Float> data = new ArrayList<Float>();
		for (IncomeByEachDay e : incomesByEachDay) {
			xAxis.add("\'"+e.getDate()+"\'");
			data.add(e.getIncomeByPay().getTotalActual());
		}
		
		String str = "";
		str += "{\"title\":\"实收总额\",\"xAxis\":"+xAxis+",\"ser\":{\"name\":\'统计\', \"data\" : "+data+"}}";
		response.getWriter().print(str.toString());
		return null;
	}
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Department> root = null;
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "", orderClause = " ORDER BY dept_id ";
			root = DepartmentDao.getDepartments(staff, extraCond, orderClause);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
