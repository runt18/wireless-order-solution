package com.wireless.Actions.inventoryMgr.stockAction;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.system.SystemDao;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryStockActionAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<StockAction> root = null;
		String isHistory = request.getParameter("isHistory");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			String stockType = request.getParameter("stockType");
			String cateType = request.getParameter("cateType");
			String dept = request.getParameter("dept");
			String oriStockId = request.getParameter("oriStockId");
			String status = request.getParameter("status");
			String supplier = request.getParameter("supplier");
			String subType = request.getParameter("subType");
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			String extraCond = "", orderClause = "";
			String curmonth = new SimpleDateFormat("yyyy-MM").format(SystemDao.getCurrentMonth(term));
			if(isHistory == null || !Boolean.valueOf(isHistory)){
				// 只能查询当前会计月份数据
				extraCond += (" AND S.ori_stock_date BETWEEN '" + curmonth + "-01' AND '" + curmonth + "-31' ");
			}else{
				extraCond += (" AND S.ori_stock_date < '" + curmonth + "'" );
			}
			if(beginDate != null && !beginDate.trim().isEmpty()){
				extraCond += (" AND S.ori_stock_date >= '" + beginDate + "' AND S.ori_stock_date < '" + endDate + "'");
			}
			if(id != null && !id.trim().isEmpty()){
				extraCond += (" AND S.id = " + id);
			}
			if(stockType != null && !stockType.trim().isEmpty() && !stockType.equals("-1")){
				extraCond += (" AND S.type = " + stockType);
				if(dept != null && !dept.trim().isEmpty() && !dept.equals("-1")){
					if(stockType.equals("1")){
						extraCond += (" AND S.dept_in = " + dept);
					}else if(stockType.equals("2")){
						extraCond += (" AND S.dept_out = " + dept);
					}
				}
			}
			if(cateType != null && !cateType.trim().isEmpty() && !cateType.equals("-1")){
				extraCond += (" AND S.cate_type = " + cateType);
			}
			if(oriStockId != null && !oriStockId.trim().isEmpty()){
				extraCond += (" AND (S.ori_stock_id LIKE '%" + oriStockId.trim() + "%' OR S.id = '" + oriStockId.trim() + "')");
			}
			if(status != null && !status.trim().isEmpty()){
				extraCond += (" AND S.status = " + status.trim());
			}
			if(supplier != null && !supplier.trim().equals("-1") && !supplier.trim().isEmpty() ){
				extraCond += (" AND S.supplier_id = " + supplier.trim());
			}
			if(subType != null && !subType.trim().isEmpty() && !subType.equals("-1")){
				extraCond += (" AND S.sub_type = " + subType.trim());
			}
			orderClause += (" ORDER BY S.status ");
			root = StockActionDao.getStockAndDetail(term, extraCond, orderClause);

			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			if(!root.isEmpty()){
				jobject.setTotalProperty(root.size());
				float price = 0, actualPrice = 0;
				for (StockAction stockAction : root) {
					price += stockAction.getPrice();
					actualPrice += stockAction.getActualPrice();
				}
				StockAction totalStockAction = new StockAction();
				totalStockAction.setAmount(0);
				totalStockAction.setCateType(1);
				totalStockAction.setId(0);
				totalStockAction.setRestaurantId(37);
				totalStockAction.setType(1);
				totalStockAction.setSubType(1);
				totalStockAction.setStatus(1);
				totalStockAction.setActualPrice(actualPrice);
				totalStockAction.setPrice(price);
				
				root = DataPaging.getPagingData(root, isPaging, start, limit);
				root.add(totalStockAction);
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
