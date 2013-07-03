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
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			String extraCond = "", orderClause = "";
			
			// 只能查询当前会计月份数据
			String curmonth = new SimpleDateFormat("yyyy-MM").format(SystemDao.getCurrentMonth(term));
			extraCond += (" AND S.ori_stock_date BETWEEN '" + curmonth + "-01' AND '" + curmonth + "-31' ");
						
			if(id != null && !id.trim().isEmpty()){
				extraCond += (" AND S.id = " + id);
			}
			if(stockType != null && !stockType.trim().isEmpty()){
				extraCond += (" AND S.type = " + stockType);
				if(dept != null && !dept.trim().isEmpty() && !dept.equals("-1")){
					if(stockType.equals("1")){
						extraCond += (" AND S.dept_in = " + dept);
					}else if(stockType.equals("2")){
						extraCond += (" AND S.dept_out = " + dept);
					}
				}
			}
			if(cateType != null && !cateType.trim().isEmpty()){
				extraCond += (" AND S.cate_type = " + cateType);
			}
			if(oriStockId != null && !oriStockId.trim().isEmpty()){
				extraCond += (" AND S.ori_stock_id LIKE '%" + oriStockId.trim() + "%' ");
			}
			if(status != null && !status.trim().isEmpty()){
				extraCond += (" AND S.status = " + status.trim());
			}
			
			orderClause += (" ORDER BY S.status ");
			root = StockActionDao.getStockAndDetail(term, extraCond, orderClause);
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
