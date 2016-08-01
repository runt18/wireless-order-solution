package com.wireless.Actions.inventoryMgr.stockAction;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.util.DataPaging;

public class QueryStockActionAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String stockType = request.getParameter("stockType");
		final String cateType = request.getParameter("cateType");
		final String dept = request.getParameter("dept");
		final String oriStockId = request.getParameter("oriStockId");
		final String status = request.getParameter("status");
		final String supplier = request.getParameter("supplier");
		final String subType = request.getParameter("subType");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String isHistory = request.getParameter("isHistory");
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String comment = request.getParameter("comment");
		final String isWithOutSum = request.getParameter("isWithOutSum");
		final String fuzzId = request.getParameter("fuzzId");
		final String cateId = request.getParameter("cateId");
		final String containsDetails = request.getParameter("containsDetails");
		final String isDistribution = request.getParameter("isDistribution");
		final JObject jObject = new JObject();
		try{

			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final StockActionDao.ExtraCond extraCond = new StockActionDao.ExtraCond();
			
			if(isDistribution == null || isDistribution.isEmpty()){
				extraCond.addExceptSubType(StockAction.SubType.DISTRIBUTION_APPLY)
						 .addExceptSubType(StockAction.SubType.DISTRIBUTION_SEND)
						 .addExceptSubType(StockAction.SubType.DISTRIBUTION_RECEIVE)
						 .addExceptSubType(StockAction.SubType.DISTRIBUTION_RETURN)
						 .addExceptSubType(StockAction.SubType.DISTRIBUTION_RECOVERY);
			}
			
//			long monthly = MonthlyBalanceDao.getCurrentMonthTimeByRestaurant(staff.getRestaurantId());
//			String curmonth = new SimpleDateFormat("yyyy-MM").format(monthly);
			if(isHistory != null && !isHistory.isEmpty() && Boolean.parseBoolean(isHistory)){
				extraCond.setHistory(true);
				if(beginDate != null && !beginDate.trim().isEmpty() && endDate != null && !endDate.isEmpty()){
					//extraCond += (" AND S.ori_stock_date >= '" + beginDate + "' AND S.ori_stock_date < '" + endDate + "'");
					extraCond.setOriDate(beginDate, endDate);
				}
			}else{
				// 只能查询当前会计月份数据
				//extraCond += (" AND S.ori_stock_date BETWEEN '" + curmonth + "-01' AND '" + curmonth + "-31 23:59:59' ");
				extraCond.setCurrentMonth(true);
			}

			if(id != null && !id.trim().isEmpty()){
				//extraCond += (" AND S.id = " + id);
				extraCond.setId(Integer.parseInt(id));
			}
			if(stockType != null && !stockType.trim().isEmpty() && !stockType.equals("-1")){
				//extraCond += (" AND S.type = " + stockType);
				extraCond.setType(StockAction.Type.valueOf(Integer.parseInt(stockType)));
				if(dept != null && !dept.trim().isEmpty() && !dept.equals("-1")){
					if(Integer.parseInt(stockType) == StockAction.Type.STOCK_IN.getVal()){
						//extraCond += (" AND S.dept_in = " + dept);
						extraCond.setDeptIn(Integer.parseInt(dept));
					}else if(Integer.parseInt(stockType) == StockAction.Type.STOCK_OUT.getVal()){
						//extraCond += (" AND S.dept_out = " + dept);
						extraCond.setDeptOut(Integer.parseInt(dept));
					}
				}
			}else{
				if(dept != null && !dept.trim().isEmpty() && !dept.equals("-1")){
					//extraCond += (" AND (S.dept_in = " + dept + " OR S.dept_out = " + dept + ")");
					extraCond.setDept(Integer.parseInt(dept));
				}
			}

			if(cateType != null && !cateType.trim().isEmpty() && !cateType.equals("-1")){
				//extraCond += (" AND S.cate_type = " + cateType);
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			if(oriStockId != null && !oriStockId.trim().isEmpty()){
				//extraCond += (" AND (S.ori_stock_id LIKE '%" + oriStockId.trim() + "%' OR S.id = '" + oriStockId.trim() + "')");
				extraCond.setOriId(oriStockId);
			}
			if(fuzzId != null && !fuzzId.trim().isEmpty()){
				extraCond.setFuzzId(fuzzId);	
			}
			if(status != null && !status.trim().isEmpty() && Integer.parseInt(status) >= 0){
				//extraCond += (" AND S.status = " + status.trim());
				extraCond.addStatus(StockAction.Status.valueOf(Integer.parseInt(status)));
			}
			if(supplier != null && !supplier.trim().equals("-1") && !supplier.trim().isEmpty() ){
				//extraCond += (" AND S.supplier_id = " + supplier.trim());
				extraCond.setSupplier(Integer.parseInt(supplier));
			}
			if(subType != null && !subType.trim().isEmpty() && !subType.equals("-1")){
				//extraCond += (" AND S.sub_type = " + subType.trim());
				extraCond.addSubType(StockAction.SubType.valueOf(Integer.parseInt(subType)));
			}
			if(comment != null && !comment.isEmpty()){
				extraCond.setComment(comment);
			}
			if(cateId != null && !cateId.isEmpty() && Integer.parseInt(cateId) > 0){
				extraCond.setCateId(Integer.parseInt(cateId));
			}
			if(containsDetails != null && !containsDetails.isEmpty() && Boolean.parseBoolean(containsDetails)){
				extraCond.setContainsDetail(true);
			}
			
			List<StockAction> root = StockActionDao.getByCond(staff, extraCond, " ORDER BY S.status, S.ori_stock_date ");
			
			//设置是否可反审核
			//最近盘点或月结时间
			long stockTakeOrBalanceTime = StockActionDao.getStockActionInsertTime(staff);
			for (StockAction stockAction : root) {
				if(stockAction.getStatus() == StockAction.Status.AUDIT || stockAction.getStatus() == StockAction.Status.RE_AUDIT){
					if(stockTakeOrBalanceTime > stockAction.getApproverDate()){
						stockAction.setStatus(StockAction.Status.FINAL);
					}
				}
			}
			
			jObject.setTotalProperty(root.size());
			List<StockAction> result = DataPaging.getPagingData(root, Boolean.parseBoolean(isPaging), start, limit);

			if(!root.isEmpty() && isWithOutSum == null){
				float price = 0, actualPrice = 0;
				for (StockAction stockAction : root) {
					price += stockAction.getPrice();
					actualPrice += stockAction.getActualPrice();
				}
				StockAction totalStockAction = new StockAction(0);
				totalStockAction.setAmount(0);
				totalStockAction.setCateType(1);
				totalStockAction.setRestaurantId(37);
				totalStockAction.setType(1);
				totalStockAction.setSubType(1);
				totalStockAction.setStatus(1);
				totalStockAction.setActualPrice(actualPrice);
				totalStockAction.setPrice(price);
				
				result.add(totalStockAction);
			}
			jObject.setRoot(result);
			
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{

			response.getWriter().print(jObject.toString());
		}
		return null;
	}

}
