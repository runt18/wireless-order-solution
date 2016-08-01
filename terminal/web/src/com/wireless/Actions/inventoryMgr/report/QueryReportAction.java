package com.wireless.Actions.inventoryMgr.report;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockReport;

public class QueryReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String beginDate = request.getParameter("beginDate");
		final String materialId = request.getParameter("materialId");
		final String deptId = request.getParameter("deptId");
		final String cateId = request.getParameter("cateId");
		final String cateType = request.getParameter("cateType");
		final JObject jObject = new JObject();
		try{

			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final StockReportDao.ExtraCond extraCond = new StockReportDao.ExtraCond().addExceptSubTypes(StockAction.SubType.DISTRIBUTION_APPLY);
			
			if(beginDate != null){
				extraCond.setRange(beginDate);
			}else{
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.MONTH, -1);
				extraCond.setRange(new SimpleDateFormat("yyyy-MM").format(c.getTime()));
			}
			
			if(materialId != null && !materialId.equals("-1") && !materialId.trim().isEmpty()){
				extraCond.setMaterial(Integer.parseInt(materialId));
			}
			
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extraCond.setDept(Integer.parseInt(deptId));
			}
			
			if(cateId != null && !cateId.isEmpty() && Integer.parseInt(cateId) >= 0){
				extraCond.setMaterialCate(Integer.parseInt(cateId));
			}
			
			if(cateType != null && !cateType.isEmpty() && Integer.parseInt(cateType) >= 0){
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				extraCond.setStart(Integer.parseInt(start));
				extraCond.setLimit(Integer.parseInt(limit));
			}
			
			List<StockReport> result = StockReportDao.getByCond(staff, extraCond);

			StockReport summary = new StockReport();
			for(StockReport report : result){
				summary.setFinalAmount(summary.getFinalAmount() + report.getFinalAmount());
				summary.setFinalMoney(summary.getFinalMoney() + report.getFinalMoney());
				summary.setPrimeAmount(summary.getPrimeAmount() + report.getPrimeAmount());
				summary.setPrimeMoney(summary.getPrimeMoney() + report.getPrimeMoney());
			}
			
			jObject.setTotalProperty(StockReportDao.getByCond(staff, extraCond.setIsOnlyAmount(true)).size());
			result.add(summary);
			
			jObject.setRoot(result);
			
		}catch(BusinessException | SQLException e){
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
