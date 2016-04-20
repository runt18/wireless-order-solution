package com.wireless.Actions.inventoryMgr.report;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.util.DataPaging;

public class QueryDeltaReportAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("beginDate");
		final String materialId = request.getParameter("materialId");
		final String cateType = request.getParameter("cateType");
		final String cateId = request.getParameter("cateId");
		final String deptId = request.getParameter("deptId");
		final JObject jObject = new JObject();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<StockReport> deltaReports = new ArrayList<StockReport>();
			StockReportDao.ExtraCond extraCond = new StockReportDao.ExtraCond();
			
			if(beginDate != null && !beginDate.isEmpty()){
				extraCond.setRange(beginDate);
			}
			
			if(materialId != null && !materialId.isEmpty()){
				extraCond.setMaterialCate(Integer.valueOf(materialId));
			}
			
			if(cateType != null && !cateType.isEmpty()){
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.valueOf(cateType)));
			}
			
			if(cateId != null && !cateId.isEmpty()){
				extraCond.setMaterialCate(Integer.valueOf(cateId));
			}
			
			if(Integer.valueOf(deptId) >= 0){
				extraCond.setDept(Integer.valueOf(deptId));
			}
			
			deltaReports = StockReportDao.getByCond(staff, extraCond);
			
			if(!start.isEmpty() && !limit.isEmpty()){
				deltaReports = DataPaging.getPagingData(deltaReports, true, start, limit);
			}
			
			jObject.setTotalProperty(deltaReports.size());
			jObject.setRoot(deltaReports);
		}catch(BusinessException e){
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
