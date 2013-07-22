package com.wireless.Actions.inventoryMgr.stockTake;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockTakeDao;
import com.wireless.db.system.SystemDao;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryStockTakeAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<StockTake> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = request.getParameter("pin");
			String status = request.getParameter("status");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			String extraCond = "", orderClause = "";
			if(status != null){
				extraCond += " AND ST.status = " + status;
			}
			// 只能查询当前会计月份数据
			String curmonth = new SimpleDateFormat("yyyy-MM").format(SystemDao.getCurrentMonth(term));
			extraCond += (" AND ST.start_date BETWEEN '" + curmonth + "-01' AND '" + curmonth + "-31' ");
			
			orderClause += (" ORDER BY ST.status, ST.start_date ");
			root = StockTakeDao.getStockTakesAndDetail(term, extraCond, orderClause);
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
