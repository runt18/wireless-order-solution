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
import com.wireless.db.stockMgr.CostAnalyzeReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.CostAnalyze;

public class QueryCostAnalyzeReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String beginDate = request.getParameter("beginDate");
			String endDate = "";
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<CostAnalyze> list = new ArrayList<CostAnalyze>();
			Calendar c = Calendar.getInstance();
			if(beginDate == null){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				//默认使用当前时间实时查询
				beginDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01";
				
				endDate = sdf.format(new Date());
				list = CostAnalyzeReportDao.getCostAnalyzes(staff, beginDate, endDate, null);
			}else{
				endDate = beginDate + "-31 23:59:59";
				list = CostAnalyzeReportDao.getCostAnalyzes(staff, beginDate + "-01", endDate, null);
			}
			jobject.setTotalProperty(list.size());
			
			if(!list.isEmpty()){
				CostAnalyze sum = new CostAnalyze();
				for (CostAnalyze costAnalyze : list) {
					sum.setPrimeMoney(sum.getPrimeMoney() + costAnalyze.getPrimeMoney());
					sum.setPickMaterialMoney(sum.getPickMaterialMoney() + costAnalyze.getPickMaterialMoney());
					sum.setStockInTransferMoney(sum.getStockInTransferMoney() + costAnalyze.getStockInTransferMoney());
					sum.setStockOutMoney(sum.getStockOutMoney() + costAnalyze.getStockOutMoney());
					sum.setStockOutTransferMoney(sum.getStockOutTransferMoney() + costAnalyze.getStockOutTransferMoney());
					sum.setEndMoney(sum.getEndMoney() + costAnalyze.getEndMoney());
					sum.setCostMoney(sum.getCostMoney() + costAnalyze.getCostMoney());
					sum.setSalesMoney(sum.getSalesMoney() + costAnalyze.getSalesMoney());
				}
				
				list.add(sum);
			}
			
			
			jobject.setRoot(list);
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
