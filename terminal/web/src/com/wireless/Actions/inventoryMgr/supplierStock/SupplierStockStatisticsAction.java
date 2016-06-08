package com.wireless.Actions.inventoryMgr.supplierStock;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.SupplierStockDao;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate.Type;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.SupplierStock;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.util.DataPaging;

public class SupplierStockStatisticsAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final String beginDate = request.getParameter("dateBegin");
		final String endDate = request.getParameter("dateEnd");
		final String supplierId = request.getParameter("supplierId");
		final String cateType = request.getParameter("cateType");
		final String cateId = request.getParameter("cateId");
		final String pin = (String)request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit"); 
		final JObject jObject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			SupplierStockDao.ExtraCond extraCond = new SupplierStockDao.ExtraCond();
			
			if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				extraCond.setRange(beginDate, endDate);
			}
			
			if(supplierId != null && !supplierId.isEmpty() && Integer.parseInt(supplierId) > 0){
				extraCond.setSupplierId(Integer.parseInt(supplierId));
			}
			
			if(cateType != null && !cateType.isEmpty() && Integer.parseInt(cateType) > 0){
				extraCond.setCateType(Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(cateId != null && !cateId.isEmpty() && Integer.parseInt(cateId) > 0){
				extraCond.setCate(Integer.parseInt(cateId));
			}
			
			List<SupplierStock> result = SupplierStockDao.getByCond(staff, extraCond);
			SupplierStock sum = new SupplierStock();
			sum.setSupplier(new Supplier());
			
			
			/**
			 * 汇总
			 */
			for(SupplierStock supplierStock : result){
				sum.setStockInAmount(sum.getStockInAmount() + supplierStock.getStockInAmount());
				sum.setStockInMoney(sum.getStockInMoney() + supplierStock.getStockInMoney());
				sum.setStockOutAmount(sum.getStockOutAmount() + supplierStock.getStockOutAmount());
				sum.setStockOutMoney(sum.getStockOutMoney() + supplierStock.getStockOutMoney());
				sum.setTotalMoney(sum.getTotalMoney() + supplierStock.getTotalMoney());
			}
			
			/**
			 * 分页
			 */
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, start, limit);
			}
			
			if(result.size() > 0){
				result.add(sum);
			}
			
			jObject.setRoot(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			response.getWriter().print(jObject);
		}
		
		return null;
	}
}
