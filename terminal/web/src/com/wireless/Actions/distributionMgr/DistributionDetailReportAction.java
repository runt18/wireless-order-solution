package com.wireless.Actions.distributionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distributionMgr.DistributionDetailReportDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.distributionMgr.DistributionDetailReport;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;

public class DistributionDetailReportAction extends DispatchAction{
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String associateId = request.getParameter("associateId");
		final String subType = request.getParameter("subType");
		final String materialId = request.getParameter("materialId");
		final String stockOutRestaurant = request.getParameter("stockOutRestaurant");
		final String stockInRestaurant = request.getParameter("stockInRestaurant");
		final String stockType = request.getParameter("stockType");
		final String comment = request.getParameter("comment");
		final String materialType = request.getParameter("materialType");
		final String materialCate = request.getParameter("materialCate");
		final String fuzzyId = request.getParameter("fuzzyId");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try {
			DistributionDetailReportDao.ExtraCond extraCond = new DistributionDetailReportDao.ExtraCond().addExceptSubTypes(StockAction.SubType.DISTRIBUTION_APPLY);
			
			if(fuzzyId != null && !fuzzyId.isEmpty()){
				extraCond.setFuzzyId(Integer.parseInt(fuzzyId));
			}
			
			if(materialCate != null && !materialCate.isEmpty()){
				extraCond.setMaterialCateId(Integer.parseInt(materialCate));
			}
			
			if(materialType != null && !materialType.isEmpty() && Integer.parseInt(materialType) > 0){
				extraCond.setMaterialType(MaterialCate.Type.valueOf(Integer.parseInt(materialType)));
			}
			
			if(stockType != null && !stockType.isEmpty() && Integer.parseInt(stockType) > 0){
				extraCond.addType(StockAction.Type.valueOf(Integer.parseInt(stockType)));
			}
			
			if(comment != null && !comment.isEmpty()){
				extraCond.setComment(comment);
			}
			
			if(beginDate != null && !beginDate.isEmpty()){
				extraCond.setBeginDate(beginDate);
			}
			
			if(endDate != null && !endDate.isEmpty()){
				extraCond.setEndDate(endDate);
			}
			
			if(associateId != null && !associateId.isEmpty()){
				extraCond.setAssociateId(Integer.parseInt(associateId));
			}
			
			if(subType != null && !subType.isEmpty() && Integer.parseInt(subType) > 0){
				extraCond.addSubType(StockAction.SubType.valueOf(Integer.parseInt(subType)));
			}
			
			if(materialId != null && !materialId.isEmpty()){
				extraCond.setMaterialId(Integer.parseInt(materialId));
			}
			
			if(stockInRestaurant != null && !stockInRestaurant.isEmpty() && Integer.parseInt(stockInRestaurant) > 0){
				extraCond.setStockInRestaurant(Integer.parseInt(stockInRestaurant));
			}
			
			if(stockOutRestaurant != null && !stockOutRestaurant.isEmpty() && Integer.parseInt(stockOutRestaurant) > 0){
				extraCond.setStockOutRestaurant(Integer.parseInt(stockOutRestaurant));
			}
			jObject.setTotalProperty(DistributionDetailReportDao.getByCond(staff, extraCond.setIsOnlyAmount(true), null).size());
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				extraCond.setLimit(Integer.parseInt(start), Integer.parseInt(limit));
			}
			
			List<DistributionDetailReport> result = DistributionDetailReportDao.getByCond(staff, extraCond.setIsOnlyAmount(false), null);
			if(!result.isEmpty()){
				result.add(DistributionDetailReportDao.getByCond(staff, extraCond.setIsOnlySum(true), null).get(0));
			}
			jObject.setRoot(result);
			
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
