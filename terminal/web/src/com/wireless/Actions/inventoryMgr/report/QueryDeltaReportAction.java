package com.wireless.Actions.inventoryMgr.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockDeltaReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockTakeDetail;

public class QueryDeltaReportAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			String beginDate = request.getParameter("beginDate");
			String endDate = "";
			String materialId = request.getParameter("materialId");
			String cateType = request.getParameter("cateType");
			String cateId = request.getParameter("cateId");
			String deptId = request.getParameter("deptId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extra = "";
			String orderClause = "LIMIT " + start +", " + limit;
			List<StockTakeDetail> deltaReports = new ArrayList<StockTakeDetail>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if(beginDate == null){
				
				//默认使用当前时间实时查询
				Calendar c = Calendar.getInstance();
				beginDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01";
				
				endDate = sdf.format(new Date());
				
			}else{
				endDate = beginDate + "-31";
				beginDate += "-01";
				
			}
			if(cateType != null){
				if(!cateType.trim().isEmpty()){
					extra += " AND MC.type = " + cateType;
				}
				if(!cateId.trim().isEmpty()){
					extra += " AND M.cate_id = " + cateId;
				}
				if(!materialId.equals("-1") && !materialId.trim().isEmpty()){
					extra += " AND M.material_id = " + materialId;
				}
			}
			if(deptId == null){
				deptId = "-1";
			}

			int count = StockDeltaReportDao.deltaReportCount(staff, beginDate, endDate, deptId, extra);

			deltaReports = StockDeltaReportDao.deltaReport(staff, beginDate, endDate, deptId, extra, orderClause);
			jobject.setTotalProperty(count);
			jobject.setRoot(deltaReports);
		}catch(BusinessException e){
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
