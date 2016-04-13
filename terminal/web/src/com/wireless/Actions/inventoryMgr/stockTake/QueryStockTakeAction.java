package com.wireless.Actions.inventoryMgr.stockTake;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.stockMgr.StockTakeDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.util.DataPaging;

public class QueryStockTakeAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<StockTake> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = (String)request.getAttribute("pin");
			String status = request.getParameter("status");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "", orderClause = "";
			if(status != null){
				extraCond += " AND ST.status = " + status;
			}
			// 只能查询当前会计月份数据
			String curmonth = new SimpleDateFormat("yyyy-MM").format(MonthlyBalanceDao.getCurrentMonthTimeByRestaurant(staff.getRestaurantId()));
			extraCond += (" AND ST.start_date BETWEEN '" + curmonth + "-01' AND '" + curmonth + "-31 23:59:59' ");
			
			orderClause += (" ORDER BY ST.status, ST.start_date ");
			root = StockTakeDao.getStockTakesAndDetail(staff, extraCond, orderClause);
			
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, Boolean.parseBoolean(isPaging), start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
