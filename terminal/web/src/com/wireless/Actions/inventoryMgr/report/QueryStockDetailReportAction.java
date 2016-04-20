package com.wireless.Actions.inventoryMgr.report;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.db.stockMgr.StockDetailReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.util.DataPaging;

public class QueryStockDetailReportAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final String beginDate = request.getParameter("beginDate");
		final String materialId = request.getParameter("materialId");
		final String materialCateId = request.getParameter("materialCateId");
		final String cateType = request.getParameter("cateType");
		final String deptOut = request.getParameter("deptOut");
		final String deptIn = request.getParameter("deptIn");
		final String supplier = request.getParameter("supplier");
		//String stockType = request.getParameter("stockType");
		final String subType = request.getParameter("subType");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final JObject jObject = new JObject();
		try{

			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final StockActionDetailDao.ExtraCond extraCond = new StockActionDetailDao.ExtraCond().addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT);
			
			extraCond.setOriDate(beginDate + "-01", beginDate + "-31");
			
			if(materialId != null && !materialId.isEmpty()){
				extraCond.setMaterial(Integer.parseInt(materialId));
			}
			
			if(materialCateId != null && !materialCateId.isEmpty()){
				extraCond.setMaterialCate(Integer.parseInt(materialCateId));
			}
			
			if(cateType != null && !cateType.isEmpty() && !cateType.equals("-1")){
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(subType != null && !subType.isEmpty()){
				extraCond.addSubType(StockAction.SubType.valueOf(Integer.parseInt(subType)));
			}
				
			if(supplier != null && !supplier.isEmpty() && !supplier.equals("-1")){
				extraCond.setSupplier(Integer.parseInt(supplier));
			}
			
			if(deptIn != null && !deptIn.isEmpty()){
				extraCond.setDeptIn(Integer.parseInt(deptIn));
			}

			if(deptOut != null && !deptOut.isEmpty()){
				extraCond.setDeptOut(Integer.parseInt(deptOut));
			}
			
			List<StockDetailReport> result = StockDetailReportDao.getByCond(staff, extraCond);
			jObject.setTotalProperty(result.size());

			StockDetailReport summary = new StockDetailReport();
			float totalStockInAmount = 0, totalStockInMoney = 0, totalStockOutAmount = 0, totalStockOutMoney = 0;
			for (StockDetailReport s : result) {
				if(s.getStockAction().getType() == StockAction.Type.STOCK_IN){
					totalStockInAmount += s.getStockActionDetail().getAmount();
					totalStockInMoney += s.getStockActionDetail().getAmount() * s.getStockActionDetail().getPrice();
				}
				if(s.getStockAction().getType() == StockAction.Type.STOCK_OUT){
					totalStockOutAmount += s.getStockActionDetail().getAmount();
					totalStockOutMoney += s.getStockActionDetail().getAmount() * s.getStockActionDetail().getPrice();
				}
			}
			summary.setSummary(true);
			summary.setTotalStockInAmount(totalStockInAmount);
			summary.setTotalStockInMoney(totalStockInMoney);
			summary.setTotalStockOutAmount(totalStockOutAmount);
			summary.setTotalStockOutMoney(totalStockOutMoney);

			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, Integer.parseInt(start), Integer.parseInt(limit));
			}
			
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
