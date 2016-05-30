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
import com.wireless.db.stockMgr.CostAnalyzeReportDao.ExtraCond;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.CostAnalyze;

public class QueryCostAnalyzeReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		final JObject jobject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String deptId = request.getParameter("deptId");
		String beginDate = request.getParameter("beginDate");
		try{
			String endDate = "";
			List<CostAnalyze> list = new ArrayList<CostAnalyze>();
			Calendar c = Calendar.getInstance();
			final ExtraCond extraCond = new CostAnalyzeReportDao.ExtraCond();
			
			if(deptId != null && !deptId.isEmpty() && Integer.parseInt(deptId) >= 0){
				extraCond.setDeptId(Integer.parseInt(deptId));
			}
			
			if(beginDate == null){
				//默认使用当前时间实时查询
				beginDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01";
				
				endDate = sdf.format(new Date());
				list = CostAnalyzeReportDao.getByCond(staff, extraCond.setDateRange(beginDate, endDate), null);
			}else{
				endDate = beginDate + "-31 23:59:59";
				list = CostAnalyzeReportDao.getByCond(staff, extraCond.setDateRange(beginDate + "-01", endDate), null);
			}
			
			jobject.setTotalProperty(list.size() + 1);
			
			if(!list.isEmpty()){
				final CostAnalyze sum = new CostAnalyze();
				for (CostAnalyze costAnalyze : list) {
					//期初
					sum.setPrimeMoney(sum.getPrimeMoney() + costAnalyze.getPrimeMoney());
					//采购
					sum.setPickMaterialMoney(sum.getPickMaterialMoney() + costAnalyze.getPickMaterialMoney());
					//领料
					sum.setStockInTransferMoney(sum.getStockInTransferMoney() + costAnalyze.getStockInTransferMoney());
					//其他入库
					sum.setStockSpillMoney(sum.getStockSpillMoney() + costAnalyze.getStockSpillMoney());
					//盘盈
					sum.setStockTakeMoreMoney(sum.getStockTakeMoreMoney() + costAnalyze.getStockTakeMoreMoney());
					//退货
					sum.setStockOutMoney(sum.getStockOutMoney() + costAnalyze.getStockOutMoney());
					//退料
					sum.setStockOutTransferMoney(sum.getStockOutTransferMoney() + costAnalyze.getStockOutTransferMoney());
					//其他出库
					sum.setStockDamageMoney(sum.getStockDamageMoney() + costAnalyze.getStockDamageMoney());
					//盘亏
					sum.setStockTakeLessMoney(sum.getStockTakeLessMoney() + costAnalyze.getStockTakeLessMoney());
					//期末
					sum.setEndMoney(sum.getEndMoney() + costAnalyze.getEndMoney());
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
